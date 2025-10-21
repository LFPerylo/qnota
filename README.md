# QNOTA

Monorepo do QNOTA — uma plataforma acadêmica organizada em **módulos** (domínio, aplicação, infraestrutura e camadas de apresentação).  
O repositório é *multi-módulo Maven* e utiliza **BDD** (Cucumber) como documentação viva dos requisitos.

> 📚 **Documentação oficial**
>
> - **Cenários Gherkin (living documentation):**  
>   `dominio-principal/src/test/resources/dev/com/qnota/dominio/principal/*.feature`
> - **Notas e materiais adicionais:**  
>   `Documentação/`
> - **Context Mapper:** `qnota.cml`

---

## Sumário

- [Arquitetura geral](#arquitetura-geral)
- [Módulos](#módulos)
- [Requisitos](#requisitos)
- [Como buildar](#como-buildar)
- [Como executar os testes](#como-executar-os-testes)
- [Fluxo de desenvolvimento](#fluxo-de-desenvolvimento)
- [Padrões e decisões do domínio](#padrões-e-decisões-do-domínio)
- [Estrutura de pastas (alto nível)](#estrutura-de-pastas-alto-nível)
- [Contribuindo](#contribuindo)
- [Licença](#licença)

---

## Arquitetura geral

- **DDD** no módulo de domínio, com:
  - **Entidades** que mantêm invariantes locais,
  - **Serviços de aplicação** que orquestram regras entre agregados,
  - **Repositórios** como **interfaces** (persistência plugável).
- **Documentação por BDD**: cada regra de negócio relevante aparece como cenário Gherkin; os testes executam os serviços e validam o estado via repositório.
- **Monorepo Maven**: um `pom.xml` raiz agrega os módulos e padroniza plugins/versões.

---

## Módulos

> Os nomes a seguir refletem as pastas de primeiro nível. Alguns podem ser *placeholders* para futuras integrações — consulte o `pom.xml` raiz para a lista efetiva de módulos.

- **`dominio-principal/`**  
  Núcleo de regras do subdomínio principal (aluno, responsável, turma, simulado, etc.).  
  - Entidades + Serviços + Contratos de repositório  
  - Testes BDD com Cucumber + JUnit 5  
  - **Repositorio em memória** para execução de cenários (somente `test`)

- **`aplicacao/`**  
  Casos de uso/orquestrações de aplicação (separado do domínio puro). Pode expor *ports* de entrada/saída.

- **`infraestrutura/`**  
  Adaptações de persistência, mensageria, integrações externas, etc. (ex.: implementação JPA dos repositórios).

- **`apresentacao-backend/`**  
  Endpoints/transport (REST/GraphQL/gRPC) sobre os serviços da aplicação.

- **`apresentacao-frontend/`**  
  Interface web (SPA). **Se presente**, ver seção de *Requisitos* para Node/NPM.

- **`Documentação/`**  
  Materiais de apoio (especificações, diagramas, guias).  
  > Os **.feature** continuam sendo a fonte de verdade do comportamento.

---

## Requisitos

- **Java 17+**
- **Maven 3.9+**

---

## Como buildar

Build completo do monorepo:

```bash
mvn -T1C clean verify
