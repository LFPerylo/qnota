# Padrões de Projeto Aplicados no QNota

Este documento descreve os padrões de projeto aplicados no sistema QNota, explicando como foram implementados no contexto do domínio de gestão de simulados e rankings.

---

## 1. Padrão Decorator

### Contexto no Domínio
O padrão Decorator foi aplicado para adicionar funcionalidade de **auditoria** ao `SimuladoRepositorio` sem modificar sua implementação base. Isso permite rastrear operações de salvamento, leitura e remoção de simulados de forma transparente.

### Classes Envolvidas

#### `SimuladoRepositorioDecorator`
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/simulado/SimuladoRepositorioDecorator.java`
- **Responsabilidade**: Envolve um `SimuladoRepositorio` real e adiciona comportamento de auditoria antes e/ou depois das operações
- **Por que Decorator?**: Permite adicionar responsabilidades (auditoria) dinamicamente sem alterar a estrutura do repositório base, mantendo o mesmo contrato da interface

#### `SimuladoAuditoria` e `SimuladoAuditoriaConsole`
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/simulado/`
- **Responsabilidade**: Define a interface e implementação concreta para registrar eventos de auditoria
- **Implementação atual**: `SimuladoAuditoriaConsole` registra as operações no console

### Como Foi Aplicado

O decorator intercepta as seguintes operações:
- `salvar(Simulado s)`: Registra auditoria antes de delegar ao repositório alvo
- `porId(SimuladoId id)`: Registra auditoria antes de delegar ao repositório alvo
- `remover(SimuladoId id)`: Registra auditoria antes de delegar ao repositório alvo

### Configuração
No `AplicacaoBackend.java`, o decorator é configurado envolvendo o repositório real:

```java
SimuladoRepositorio decorator = new SimuladoRepositorioDecorator(repositorio, simuladoAuditoria);
```

### Benefícios no Domínio
- **Separação de responsabilidades**: A lógica de auditoria não polui o repositório base
- **Extensibilidade**: Pode-se adicionar outros decorators (ex.: cache, logging) sem modificar código existente
- **Transparência**: O `SimuladoServico` não precisa saber que está usando um decorator

---

## 2. Padrão Template Method

### Contexto no Domínio
O padrão Template Method foi aplicado para definir o **fluxo de finalização de simulados**, garantindo que todas as etapas sejam executadas na ordem correta, enquanto permite variações na implementação de algumas etapas.

### Classes Envolvidas

#### `FinalizacaoSimuladoTemplate`
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/simulado/FinalizacaoSimuladoTemplate.java`
- **Responsabilidade**: Define o esqueleto do algoritmo de finalização com os passos:
  1. Carregar o simulado
  2. Validar se já está finalizado
  3. Validar pré-condições (RN-16: todas as notas lançadas)
  4. Executar gancho antes da finalização (opcional)
  5. Alterar estado para FINALIZADO
  6. Salvar o simulado
  7. Notificar observers
  8. Executar gancho após finalização (opcional)

#### `FinalizacaoSimuladoPadrao`
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/simulado/FinalizacaoSimuladoPadrao.java`
- **Responsabilidade**: Implementação concreta do template, fornecendo:
  - Carregamento do simulado via repositório
  - Validação da RN-16 (todas as notas devem estar lançadas)
  - Persistência do simulado finalizado

### Como Foi Aplicado

O método `finalizar(SimuladoId id)` é o **template method** que define o fluxo fixo, enquanto métodos abstratos como `carregarSimulado()`, `validarPreCondicoes()` e `salvar()` são implementados pelas subclasses.

### Uso no Domínio
O `SimuladoServico` delega a finalização para o template:

```java
public void finalizar(SimuladoId id) {
    finalizacaoTemplate.finalizar(id);
}
```

### Benefícios no Domínio
- **Consistência**: Garante que todas as finalizações sigam o mesmo fluxo
- **Extensibilidade**: Permite criar variações (ex.: `FinalizacaoSimuladoComValidacaoExtra`) sem duplicar código
- **Manutenibilidade**: Mudanças no fluxo geral são feitas em um único lugar

---

## 3. Padrão Strategy

### Contexto no Domínio
O padrão Strategy foi aplicado para encapsular diferentes **algoritmos de cálculo de ranking**. Atualmente, o sistema usa média ponderada, mas o padrão permite adicionar outras estratégias (ex.: média aritmética, mediana) sem modificar o código que usa o cálculo.

### Classes Envolvidas

#### `CalculoRankingStrategy`
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/ranking/CalculoRankingStrategy.java`
- **Responsabilidade**: Interface que define o contrato para cálculo de ranking
- **Método**: `calcular(List<Aluno> alunos, Map<Integer, Double> pesos)`

#### `CalculoRankingMediaPonderada`
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/ranking/CalculoRankingMediaPonderada.java`
- **Responsabilidade**: Implementação concreta que calcula:
  - Média ponderada por disciplina
  - Ordenação decrescente por média
  - Desempate pela data de nascimento
  - Geração de posições no ranking

#### `RankingServico`
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/ranking/RankingServico.java`
- **Responsabilidade**: Usa a strategy para calcular o ranking, sem conhecer os detalhes do algoritmo

### Como Foi Aplicado

O `RankingServico` recebe a strategy via construtor e a utiliza no método `recalcular()`:

```java
var linhas = calculoRanking.calcular(alunos, pesos);
```

### Configuração
No `AplicacaoBackend.java`, a strategy é injetada como bean:

```java
@Bean
public CalculoRankingStrategy calculoRankingStrategy(NotaServico notaServico) {
    return new CalculoRankingMediaPonderada(notaServico);
}
```

### Benefícios no Domínio
- **Flexibilidade**: Fácil trocar o algoritmo de cálculo sem modificar `RankingServico`
- **Testabilidade**: Pode-se criar mocks ou implementações de teste da strategy
- **Extensibilidade**: Novos algoritmos podem ser adicionados criando novas implementações da interface

---

## 4. Padrão Observer

### Contexto no Domínio
O padrão Observer foi aplicado para permitir que componentes reajam à **finalização de simulados** de forma desacoplada. Quando um simulado é finalizado, o sistema precisa congelar o ranking (RN-102), e o Observer permite que isso aconteça automaticamente sem acoplar o `SimuladoServico` ao `RankingServico`.

### Classes Envolvidas

#### `SimuladoObserver`
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/simulado/SimuladoObserver.java`
- **Responsabilidade**: Interface que define o contrato para observadores de eventos de simulado
- **Método**: `aoFinalizarSimulado(SimuladoId id)`

#### `FinalizacaoSimuladoTemplate` (Subject)
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/simulado/FinalizacaoSimuladoTemplate.java`
- **Responsabilidade**: Mantém uma lista de observers e os notifica após a finalização bem-sucedida
- **Métodos**:
  - `registrarObserver(SimuladoObserver observer)`: Registra um observer
  - `notificarObservers(SimuladoId id)`: Notifica todos os observers registrados

#### `RankingServico` (Observer)
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/ranking/RankingServico.java`
- **Responsabilidade**: Implementa `SimuladoObserver` e reage à finalização congelando o ranking
- **Implementação**: 
  ```java
  @Override
  public void aoFinalizarSimulado(SimuladoId id) {
      congelar(id); // RN-102
  }
  ```

### Como Foi Aplicado

1. O `FinalizacaoSimuladoTemplate` atua como **Subject**, mantendo uma lista de observers
2. Após finalizar o simulado com sucesso, o template chama `notificarObservers(id)`
3. O `RankingServico` é registrado como observer no `SimuladoServico`:
   ```java
   var template = new FinalizacaoSimuladoPadrao(repo);
   template.registrarObserver(rankingServico);
   ```

### Benefícios no Domínio
- **Desacoplamento**: O `SimuladoServico` não precisa conhecer o `RankingServico`
- **Extensibilidade**: Novos observers podem ser adicionados (ex.: notificação por email, geração de relatório) sem modificar o código de finalização
- **Responsabilidade única**: Cada observer tem uma responsabilidade específica (congelar ranking, enviar notificação, etc.)

---

## Integração dos Padrões

Os padrões trabalham em conjunto no fluxo de finalização de simulados:

1. **Template Method** (`FinalizacaoSimuladoTemplate`) define o fluxo de finalização
2. **Observer** (`SimuladoObserver`) permite que `RankingServico` reaja à finalização
3. **Strategy** (`CalculoRankingStrategy`) é usada pelo `RankingServico` para calcular o ranking
4. **Decorator** (`SimuladoRepositorioDecorator`) adiciona auditoria a todas as operações do repositório

Essa combinação demonstra como padrões de projeto podem ser aplicados de forma complementar para criar um design flexível e manutenível.

