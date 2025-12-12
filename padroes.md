# Padrões de Projeto Aplicados no QNota

Este documento descreve os padrões de projeto aplicados no sistema QNota, explicando como foram implementados no contexto do domínio de gestão de simulados e rankings. Os padrões foram escolhidos para resolver problemas específicos do domínio, mantendo o código flexível, manutenível e aderente aos princípios SOLID.

---

## 1. Padrão Decorator

### Contexto no Domínio
O padrão Decorator foi aplicado para adicionar funcionalidade de **auditoria** ao `SimuladoRepositorio` sem modificar sua implementação base. No contexto do QNota, simulados são entidades críticas que passam por diversos estados (EM_EDICAO → FINALIZADO) e precisam de rastreabilidade para fins de conformidade e debugging.

### Por que este padrão faz sentido no domínio?
- **Requisitos de auditoria**: Em sistemas educacionais, é fundamental rastrear quem criou, modificou ou excluiu simulados
- **Separação de responsabilidades**: A lógica de persistência não deve misturar-se com lógica de auditoria
- **Extensibilidade futura**: Outros comportamentos transversais podem ser adicionados (cache, validação adicional, notificações) sem modificar o repositório base
- **Open/Closed Principle**: O repositório original permanece fechado para modificação, mas aberto para extensão

### Classes Criadas/Modificadas

#### `SimuladoRepositorioDecorator` (CRIADA)
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/simulado/SimuladoRepositorioDecorator.java`
- **Responsabilidade**: Envolve um `SimuladoRepositorio` real (alvo) e adiciona comportamento de auditoria antes de delegar as operações
- **Implementa**: Interface `SimuladoRepositorio` (mantém o mesmo contrato)
- **Padrão**: Decorator clássico - envolve um objeto e adiciona comportamento sem alterar sua interface
- **Diferença para Proxy**: Decorator adiciona funcionalidade; Proxy controla acesso (lazy loading, segurança)

#### `SimuladoAuditoria` (CRIADA - Interface)
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/simulado/SimuladoAuditoria.java`
- **Responsabilidade**: Define o contrato para registrar eventos de auditoria
- **Métodos**: 
  - `registrarSalvar(Simulado s)`
  - `registrarLeitura(SimuladoId id)`
  - `registrarRemocao(SimuladoId id)`

#### `SimuladoAuditoriaConsole` (CRIADA - Implementação)
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/simulado/SimuladoAuditoriaConsole.java`
- **Responsabilidade**: Implementação concreta que registra eventos no console com timestamp
- **Implementação atual**: Usa `System.out.printf` para logging simples
- **Extensibilidade**: Pode ser substituída por implementação que grava em banco, arquivo ou serviço externo

### Como Foi Aplicado

O decorator intercepta as seguintes operações:
- `salvar(Simulado s)`: Registra auditoria antes de delegar ao repositório alvo
- `porId(SimuladoId id)`: Registra auditoria antes de delegar ao repositório alvo
- `remover(SimuladoId id)`: Registra auditoria antes de delegar ao repositório alvo

### Configuração no Spring Boot
No `AplicacaoBackend.java`, o decorator é configurado como bean, envolvendo o repositório JPA:

```java
@Bean
public SimuladoRepositorio simuladoRepositorio(SimuladoRepositorioImpl repositorio) {
    var auditoria = new SimuladoAuditoriaConsole();
    return new SimuladoRepositorioDecorator(repositorio, auditoria);
}
```

### Benefícios Específicos no Domínio QNota
- **Separação de responsabilidades**: A lógica de auditoria não polui o repositório de infraestrutura (`SimuladoRepositorioImpl`)
- **Testabilidade**: O `SimuladoServico` pode ser testado com ou sem auditoria, usando diferentes implementações
- **Extensibilidade**: Outros decorators podem ser empilhados (ex.: cache → auditoria → repositório)
- **Transparência**: O `SimuladoServico` recebe um `SimuladoRepositorio` e não precisa saber se é decorado ou não
- **Conformidade**: Facilita auditoria de conformidade (LGPD, requisitos educacionais) sem alterar código de negócio

---

## 2. Padrão Template Method

### Contexto no Domínio
O padrão Template Method foi aplicado para definir o **fluxo de finalização de simulados**, uma operação crítica no QNota que envolve múltiplas etapas sequenciais e validações de regras de negócio (RN-16: todas as notas devem estar lançadas antes da finalização).

### Por que este padrão faz sentido no domínio?
- **Algoritmo fixo com variações**: O fluxo de finalização tem etapas obrigatórias (carregar, validar, salvar, notificar) que devem sempre ocorrer na mesma ordem
- **Extensibilidade controlada**: Permite criar variações de finalização (ex.: com validações extras, com aprovação manual) sem duplicar o fluxo base
- **Garantia de consistência**: Evita que implementações esqueçam etapas críticas como notificação de observers ou validação de pré-condições
- **Pontos de extensão**: Ganchos (hooks) permitem customização antes/depois da finalização sem quebrar o fluxo principal

### Classes Criadas/Modificadas

#### `FinalizacaoSimuladoTemplate` (CRIADA - Classe Abstrata)
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/simulado/FinalizacaoSimuladoTemplate.java`
- **Responsabilidade**: Define o **template method** `finalizar(SimuladoId)` com o algoritmo completo:
  
  **Fluxo Fixo (não pode ser alterado)**:
  1. `carregarSimulado(id)` - busca o simulado (abstrato)
  2. `validarJaFinalizado(simulado)` - verifica se já está finalizado (concreto)
  3. `validarPreCondicoes(id, simulado)` - valida RN-16 e outras regras (abstrato)
  4. `antesDeFinalizar(simulado, id)` - hook opcional (concreto, vazio)
  5. `aplicarFinalizacao(simulado)` - muda estado para FINALIZADO (concreto)
  6. `salvar(simulado)` - persiste (abstrato)
  7. `notificarObservers(id)` - notifica observers registrados (concreto)
  8. `aposFinalizar(simulado, id)` - hook opcional (concreto, vazio)

- **Métodos abstratos**: Devem ser implementados pelas subclasses
- **Métodos concretos**: Implementação padrão que pode ser sobrescrita se necessário
- **Papel duplo**: Também atua como **Subject** no padrão Observer (mantém lista de observers)

#### `FinalizacaoSimuladoPadrao` (CRIADA - Implementação Concreta)
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/simulado/FinalizacaoSimuladoPadrao.java`
- **Responsabilidade**: Implementação concreta padrão do template
- **Implementações fornecidas**:
  - `carregarSimulado(id)`: Delega para `simuladoRepo.porId(id)`
  - `validarPreCondicoes(id, s)`: Aplica **RN-16** (todas as notas lançadas) via `simuladoRepo.todasNotasLancadas(id)`
  - `salvar(s)`: Delega para `simuladoRepo.salvar(s)`
- **Não sobrescreve**: Hooks opcionais (usa implementação vazia do template)

### Como Foi Aplicado

O método `finalizar(SimuladoId id)` em `FinalizacaoSimuladoTemplate` é declarado como `final`, garantindo que o fluxo não pode ser alterado. Subclasses implementam apenas os métodos abstratos:

```java
public abstract class FinalizacaoSimuladoTemplate {
    public final void finalizar(SimuladoId id) {
        // Fluxo fixo em 8 passos
        Simulado simulado = carregarSimulado(id);
        validarJaFinalizado(simulado);
        validarPreCondicoes(id, simulado);
        antesDeFinalizar(simulado, id);
        aplicarFinalizacao(simulado);
        salvar(simulado);
        notificarObservers(id);
        aposFinalizar(simulado, id);
    }
    
    protected abstract Simulado carregarSimulado(SimuladoId id);
    protected abstract void validarPreCondicoes(SimuladoId id, Simulado s);
    protected abstract void salvar(Simulado s);
}
```

### Uso no SimuladoServico (MODIFICADO)
O `SimuladoServico` foi modificado para delegar a finalização ao template:

**Antes** (lógica inline):
```java
public void finalizar(SimuladoId id) {
    var s = repo.porId(id);
    if (!repo.todasNotasLancadas(id)) throw new IllegalStateException("RN-16");
    s.finalizar();
    repo.salvar(s);
    rankingServico.congelar(id); // acoplamento direto
}
```

**Depois** (usando Template Method):
```java
public void finalizar(SimuladoId id) {
    finalizacaoTemplate.finalizar(id); // delega para o template
}
```

### Benefícios Específicos no Domínio QNota
- **Consistência**: Impossível esquecer etapas (validação, notificação de observers) pois o fluxo é fixo
- **Extensibilidade**: Fácil criar variações:
  - `FinalizacaoSimuladoComAprovacao` (requer aprovação do coordenador)
  - `FinalizacaoSimuladoComNotificacao` (envia emails após finalizar)
- **Manutenibilidade**: Se a RN-16 mudar ou novas validações forem adicionadas, apenas a classe concreta muda
- **Testabilidade**: Pode-se criar implementações mock do template para testes

---

## 3. Padrão Strategy

### Contexto no Domínio
O padrão Strategy foi aplicado para encapsular diferentes **algoritmos de cálculo de ranking**. No QNota, o ranking é calculado com base nas notas dos alunos nas disciplinas do simulado, mas o algoritmo pode variar (média ponderada, média aritmética, mediana, etc.) dependendo de decisões pedagógicas ou configurações futuras.

### Por que este padrão faz sentido no domínio?
- **Requisitos mutáveis**: Instituições de ensino frequentemente mudam critérios de avaliação e cálculo de rankings
- **Múltiplos algoritmos**: Diferentes tipos de simulados podem usar diferentes estratégias de cálculo
- **Testabilidade**: Facilita testar `RankingServico` com algoritmos mock sem depender de lógica de cálculo complexa
- **Single Responsibility**: `RankingServico` orquestra o fluxo de recalculo; a Strategy implementa o algoritmo matemático
- **Open/Closed**: Novos algoritmos podem ser adicionados sem modificar `RankingServico`

### Classes Criadas/Modificadas

#### `CalculoRankingStrategy` (CRIADA - Interface)
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/ranking/CalculoRankingStrategy.java`
- **Responsabilidade**: Define o contrato para algoritmos de cálculo de ranking
- **Método principal**: 
  ```java
  List<Ranking.Linha> calcular(List<Aluno> alunos, Map<Integer, Double> pesos);
  ```
- **Entrada**: 
  - `alunos`: Lista de alunos da turma
  - `pesos`: Mapa disciplinaId → peso (valores somam 10)
- **Saída**: Lista de `Ranking.Linha` com `aluno`, `media` e `posicao`

#### `CalculoRankingMediaPonderada` (CRIADA - Estratégia Concreta)
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/ranking/CalculoRankingMediaPonderada.java`
- **Responsabilidade**: Implementa o algoritmo de **média ponderada** usado atualmente no QNota
- **Algoritmo**:
  1. Para cada aluno, calcula média ponderada via `NotaServico.calcularMediaPonderada(aluno, pesos)`
  2. Ordena alunos por média (decrescente) e desempata por data de nascimento (mais velho primeiro)
  3. Atribui posições sequenciais (1, 2, 3, ...)
- **Dependência**: Injeta `NotaServico` para buscar notas e calcular médias
- **Outras estratégias possíveis** (não implementadas ainda):
  - `CalculoRankingMediaAritmetica`: Ignora pesos
  - `CalculoRankingMediana`: Usa mediana em vez de média
  - `CalculoRankingPorMelhorNota`: Ranking baseado na melhor nota individual

#### `RankingServico` (MODIFICADO)
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/ranking/RankingServico.java`
- **Modificação**: Agora recebe `CalculoRankingStrategy` via construtor (inversão de dependência)
- **Responsabilidade**: Orquestra o recalculo e congelamento de rankings, **mas não implementa o cálculo**
- **Uso da strategy**:
  ```java
  var linhas = calculoRanking.calcular(alunos, pesos);
  ```

### Como Foi Aplicado

**Antes** (lógica de cálculo dentro do serviço):
```java
public class RankingServico {
    public List<Ranking.Linha> recalcular(SimuladoId id) {
        // ... lógica de cálculo de média ponderada inline
        var ordenados = alunos.stream()
            .map(a -> calcularMedia(a, pesos)) // cálculo inline
            .sorted(...)
            .toList();
        // ... atribuir posições
    }
}
```

**Depois** (usando Strategy):
```java
public class RankingServico {
    private final CalculoRankingStrategy calculoRanking;
    
    public RankingServico(..., CalculoRankingStrategy calculoRanking) {
        this.calculoRanking = calculoRanking;
    }
    
    public List<Ranking.Linha> recalcular(SimuladoId id) {
        var pesos = simuladoRepo.pesosDoSimulado(id);
        var alunos = alunoRepo.porTurma(simulado.getTurma());
        
        // Delega o cálculo para a strategy
        var linhas = calculoRanking.calcular(alunos, pesos);
        
        rankingRepo.limpar(id);
        rankingRepo.salvarPosicoes(id, linhas);
        return linhas;
    }
}
```

### Configuração no Spring Boot
No `AplicacaoBackend.java`, a strategy concreta é injetada como bean:

```java
@Bean
public CalculoRankingStrategy calculoRankingStrategy(NotaServico notaServico) {
    return new CalculoRankingMediaPonderada(notaServico);
}

@Bean
public RankingServico rankingServico(..., CalculoRankingStrategy strategy) {
    return new RankingServico(..., strategy);
}
```

### Benefícios Específicos no Domínio QNota
- **Flexibilidade pedagógica**: Coordenadores podem escolher diferentes critérios de ranking sem alterar código
- **Testabilidade**: Testes do `RankingServico` podem usar strategy mock que retorna rankings fixos
- **Extensibilidade**: Novos algoritmos (mediana, média geométrica, ranking por faixas) são fáceis de adicionar
- **Separação de responsabilidades**: `RankingServico` cuida da orquestração; Strategy cuida do cálculo matemático
- **Configuração dinâmica**: No futuro, a strategy pode ser escolhida por configuração ou por tipo de simulado

---

## 4. Padrão Observer

### Contexto no Domínio
O padrão Observer foi aplicado para permitir que componentes reajam à **finalização de simulados** de forma desacoplada. No QNota, quando um simulado é finalizado, o sistema precisa **congelar o ranking** (RN-102: após finalização, o ranking não pode mais ser recalculado). Sem o Observer, haveria acoplamento direto entre `SimuladoServico` e `RankingServico`.

### Por que este padrão faz sentido no domínio?
- **Desacoplamento**: `SimuladoServico` não deve conhecer `RankingServico` ou outras reações à finalização
- **Extensibilidade**: Novos comportamentos podem reagir à finalização (envio de email, geração de relatório, notificação de alunos) sem modificar o código de finalização
- **Single Responsibility**: Cada observer tem UMA responsabilidade específica
- **Event-driven**: A finalização de simulado é um **evento de domínio** que dispara reações em cascata
- **Conformidade com DDD**: Agregados não devem referenciar diretamente outros agregados; comunicação via eventos é preferível

### Classes Criadas/Modificadas

#### `SimuladoObserver` (CRIADA - Interface)
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/simulado/SimuladoObserver.java`
- **Responsabilidade**: Define o contrato para observadores de eventos relacionados a Simulado
- **Método principal**:
  ```java
  void aoFinalizarSimulado(SimuladoId id);
  ```
- **Extensível**: Pode adicionar outros métodos para outros eventos:
  - `aoEditarDisciplinas(SimuladoId id)`
  - `aoExcluirSimulado(SimuladoId id)`

#### `FinalizacaoSimuladoTemplate` (MODIFICADA - Subject)
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/simulado/FinalizacaoSimuladoTemplate.java`
- **Papel duplo**: Template Method (define fluxo) + Subject do Observer (notifica observers)
- **Modificação**: Adicionada lista de observers e métodos de gerenciamento:
  ```java
  private final List<SimuladoObserver> observers = new ArrayList<>();
  
  public void registrarObserver(SimuladoObserver observer) {
      observers.add(observer);
  }
  
  protected void notificarObservers(SimuladoId id) {
      for (SimuladoObserver observer : observers) {
          observer.aoFinalizarSimulado(id);
      }
  }
  ```
- **Notificação**: Ocorre no passo 7 do template method, após salvar o simulado finalizado

#### `RankingServico` (MODIFICADO - Observer Concreto)
- **Localização**: `dominio-principal/src/main/java/dev/com/qnota/dominio/principal/ranking/RankingServico.java`
- **Modificação**: Agora implementa `SimuladoObserver`
- **Implementação**:
  ```java
  public class RankingServico implements SimuladoObserver {
      @Override
      public void aoFinalizarSimulado(SimuladoId id) {
          congelar(id); // RN-102: Congelar ranking após finalização
      }
  }
  ```
- **Responsabilidade**: Reage à finalização de simulado congelando o ranking automaticamente
- **Outros observers possíveis** (não implementados):
  - `NotificacaoEmailObserver`: Envia emails aos alunos
  - `RelatorioObserver`: Gera relatório PDF do simulado
  - `LogObserver`: Registra evento de finalização em log estruturado

### Como Foi Aplicado

**Antes** (acoplamento direto):
```java
public class SimuladoServico {
    private final RankingServico rankingServico; // acoplamento!
    
    public void finalizar(SimuladoId id) {
        // ... validações e finalização
        rankingServico.congelar(id); // chamada direta!
    }
}
```
❌ **Problema**: `SimuladoServico` precisa conhecer `RankingServico` e futuros serviços que reajam à finalização

**Depois** (usando Observer):
```java
// 1. RankingServico implementa SimuladoObserver
public class RankingServico implements SimuladoObserver {
    @Override
    public void aoFinalizarSimulado(SimuladoId id) {
        congelar(id); // RN-102
    }
}

// 2. Template notifica observers após finalização
public abstract class FinalizacaoSimuladoTemplate {
    private final List<SimuladoObserver> observers = new ArrayList<>();
    
    public final void finalizar(SimuladoId id) {
        // ... passos 1-6
        notificarObservers(id); // passo 7
    }
}

// 3. Registro do observer na configuração
public class SimuladoServico {
    public SimuladoServico(..., RankingServico rankingServico) {
        var template = new FinalizacaoSimuladoPadrao(repo);
        template.registrarObserver(rankingServico); // registro
        this.finalizacaoTemplate = template;
    }
}
```
✅ **Benefício**: `SimuladoServico` não conhece `RankingServico` diretamente; apenas registra observers

### Registro no Spring Boot
No `AplicacaoBackend.java`:
```java
@Bean
public SimuladoServico simuladoServico(..., RankingServico rankingServico) {
    var template = new FinalizacaoSimuladoPadrao(simuladoRepo);
    template.registrarObserver(rankingServico); // RN-102
    // template.registrarObserver(emailObserver); // futuro
    // template.registrarObserver(relatorioObserver); // futuro
    return new SimuladoServico(..., template);
}
```

### Benefícios Específicos no Domínio QNota
- **Desacoplamento total**: `SimuladoServico` não conhece `RankingServico`; comunicação via evento
- **Extensibilidade sem modificação**: Novos observers (email, relatório, log) podem ser adicionados sem alterar código de finalização
- **Testabilidade**: `SimuladoServico` pode ser testado sem `RankingServico` real (sem observers ou com mocks)
- **Responsabilidade única**: Cada observer cuida de UMA reação ao evento de finalização
- **Event-driven design**: Alinhado com arquiteturas orientadas a eventos (base para futura migração para eventos assíncronos)

---

## Integração dos Padrões no Fluxo de Finalização

Os quatro padrões trabalham **em conjunto** no fluxo de finalização de simulados, demonstrando como padrões complementares criam um design coeso:

### Fluxo Completo: Finalizar Simulado

```
[Cliente] 
   ↓ chama finalizar(id)
[SimuladoServico] 
   ↓ delega para
[FinalizacaoSimuladoTemplate] ← Template Method (define fluxo fixo)
   ↓ passo 1: carregarSimulado()
[SimuladoRepositorioDecorator] ← Decorator (adiciona auditoria)
   ↓ registra leitura e delega
[SimuladoRepositorioImpl] (JPA)
   ↓ retorna Simulado
[FinalizacaoSimuladoTemplate]
   ↓ passo 2-5: validar e finalizar
   ↓ passo 6: salvar()
[SimuladoRepositorioDecorator] ← Decorator (registra salvamento)
   ↓ passo 7: notificarObservers()
[RankingServico] ← Observer (reage ao evento)
   ↓ congelar(id)
   ↓ recalcular() se necessário
   ↓ usa CalculoRankingStrategy ← Strategy (algoritmo de cálculo)
[CalculoRankingMediaPonderada]
   ↓ retorna linhas ordenadas
[RankingRepositorio]
   ↓ salva ranking congelado
✓ Finalização completa
```

### Justificativa da Integração

1. **Template Method + Observer**: O template garante que observers sejam notificados no momento certo (após salvar, antes dos hooks finais)

2. **Observer + Strategy**: O `RankingServico` (observer) usa a Strategy para recalcular rankings quando notificado

3. **Decorator + Template**: Todas as operações do repositório são auditadas, incluindo as feitas durante o template de finalização

4. **Strategy + Template**: Ambos encapsulam algoritmos, mas com propósitos diferentes:
   - Template: Fluxo de **orquestração** (ordem de passos)
   - Strategy: **Cálculo matemático** (como calcular ranking)

### Benefícios da Integração no QNota

| Aspecto | Sem Padrões | Com Padrões |
|---------|-------------|-------------|
| **Extensibilidade** | Alterar código existente para cada novo requisito | Adicionar novas classes (Strategy, Observer) sem modificar código |
| **Testabilidade** | Testes complexos com múltiplas dependências | Testes unitários com mocks de Strategy e Observers |
| **Manutenibilidade** | Lógica espalhada em múltiplos lugares | Cada padrão encapsula uma responsabilidade específica |
| **Auditoria** | Código de auditoria misturado com negócio | Decorator adiciona auditoria transparentemente |
| **Acoplamento** | `SimuladoServico` conhece `RankingServico` | Desacoplamento via Observer |

### Conformidade com Princípios SOLID

- **S (Single Responsibility)**: Cada classe tem UMA responsabilidade
  - `FinalizacaoSimuladoTemplate`: Define fluxo de finalização
  - `CalculoRankingMediaPonderada`: Calcula ranking
  - `SimuladoRepositorioDecorator`: Adiciona auditoria
  - `RankingServico` (Observer): Reage a finalização

- **O (Open/Closed)**: Aberto para extensão, fechado para modificação
  - Novos algoritmos de ranking: Criar nova Strategy
  - Novas reações à finalização: Criar novo Observer
  - Nova forma de finalização: Criar nova subclasse do Template

- **L (Liskov Substitution)**: Implementações podem ser substituídas
  - `CalculoRankingMediaPonderada` pode ser substituída por outra Strategy
  - `SimuladoRepositorioDecorator` pode ser substituído pelo repositório base

- **I (Interface Segregation)**: Interfaces específicas e focadas
  - `SimuladoObserver`: Interface simples com 1 método
  - `CalculoRankingStrategy`: Interface focada em cálculo

- **D (Dependency Inversion)**: Depender de abstrações, não implementações
  - `RankingServico` depende de `CalculoRankingStrategy` (abstração), não da implementação concreta
  - `SimuladoServico` depende de `FinalizacaoSimuladoTemplate` (abstração)

### Conclusão

Os quatro padrões aplicados no QNota não são "decoração" ou "over-engineering". Cada um resolve um problema real do domínio:

- **Decorator**: Auditoria sem poluir código de persistência
- **Template Method**: Fluxo de finalização consistente e extensível
- **Strategy**: Algoritmos de ranking flexíveis e testáveis
- **Observer**: Reações à finalização desacopladas e extensíveis

Juntos, criam uma arquitetura que é **fácil de estender**, **fácil de testar** e **fácil de manter**, alinhada com os princípios de Clean Architecture e Domain-Driven Design.

