# QNOTA

Monorepo do **QNOTA** — plataforma acadêmica organizada em módulos (Domínio, Aplicação, Infraestrutura e Camadas de Apresentação).  
O repositório é **multi-módulo Maven** e usa **BDD (Cucumber)** como documentação viva dos requisitos.

> ### 📚 Onde está a documentação?
>
> - **Cenários Gherkin (Living Documentation):**  
>   `./dominio-principal/src/test/resources/dev/com/qnota/dominio/principal/*.feature`
> - **StoryMap e Descrição detalhada do domínio (versão “em texto” logo abaixo):**
>   `Eles estão também no diretório "Documentacao" do projeto
>   `[Descricao-Detalhada-Qnota.pdf](https://github.com/user-attachments/files/23062028/Descricao-Detalhada-Qnota.pdf)` e `Storymap aqui!.pdf`
> - **Context Mapper (limites/contextos):**  
>   `./qnota.cml`

---

## Sumário

- [Arquitetura geral](#arquitetura-geral)
- [Módulos](#módulos)
- [Protótipo](#protótipo)
- [Requisitos](#requisitos)
- [Como buildar](#como-buildar)
- [Como executar os testes](#como-executar-os-testes)
- [Fluxo de desenvolvimento](#fluxo-de-desenvolvimento)
- [Padrões e decisões do domínio](#padrões-e-decisões-do-domínio)
- [Estrutura de pastas (alto nível)](#estrutura-de-pastas-alto-nível)
- [StoryMap (texto)](#storymap-texto)
- [Descrição detalhada do domínio (texto)](#descrição-detalhada-do-domínio-texto)
- [Glossário — Linguagem onipresente](#glossário--linguagem-onipresente)
- [Contribuindo](#contribuindo)
- [Licença](#licença)

---

## Arquitetura geral

- **DDD** no módulo de domínio:
  - **Entidades** com invariantes locais.
  - **Serviços de aplicação** que orquestram regras entre agregados.
  - **Repositórios** como **interfaces** (persistência plugável).
- **Documentação por comportamento (BDD)**:
  - Cada regra relevante aparece como **cenário Gherkin**.
  - Os testes invocam serviços e validam persistência via repositório em memória.
- **Monorepo Maven**:
  - Um `pom.xml` na raiz agrega os módulos e padroniza plugins/versões.

---

## Módulos

> Os nomes a seguir refletem as pastas de primeiro nível. Consulte o `pom.xml` raiz para a lista efetiva de módulos.

- `dominio-principal/` — núcleo de regras (aluno, responsável, turma, simulado, nota, disciplina, ranking, etc.).  
  Inclui **Cucumber + JUnit 5** e um **repositório em memória** somente para testes.
- `aplicacao/` — casos de uso/orquestrações (ports de entrada/saída).
- `infraestrutura/` — persistência, mensageria e integrações (ex.: JPA).
- `apresentacao-backend/` — camada de transporte (REST/GraphQL/gRPC).
- `apresentacao-frontend/` — SPA (quando aplicável).
- `Documentação/` — PDFs e materiais auxiliares (o *source of truth* comportamental continua sendo os `.feature`).

---

## Protótipo

- Link: https://www.figma.com/make/wx4obguhFd80NeadgO1Knm/Naveg%C3%A1vel-QNota-para-Coordenador?node-id=0-1&p=f&t=vP0KSof6mrg4P5Wd-0&fullscreen=1

---

## Apresentação

- Pdf: [Apresentação Qnota.pdf](https://github.com/user-attachments/files/23062053/Apresentacao.Qnota.pdf)


---

## Requisitos

- **Java 17+**
- **Maven 3.9+**

---

## Como buildar

Build do monorepo:

```bash
mvn -T1C clean verify
# ou
mvn test
