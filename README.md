# 🎓 QNOTA - Plataforma Acadêmica de Gestão de Simulados

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=for-the-badge&logo=spring)
![React](https://img.shields.io/badge/React-18-blue?style=for-the-badge&logo=react)
![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue?style=for-the-badge&logo=typescript)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue?style=for-the-badge&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?style=for-the-badge&logo=docker)
![Maven](https://img.shields.io/badge/Maven-3.8+-red?style=for-the-badge&logo=apache-maven)

**Sistema completo de gestão acadêmica para criação, gerenciamento e análise de simulados com rankings automatizados**

[🚀 Começar](#-como-rodar-o-projeto) • [📚 Documentação](#-documentação) • [🏗️ Arquitetura](#️-arquitetura-em-camadas) • [🧪 Testes](#-como-rodar-os-testes)

</div>

---

## 📋 Sobre o Projeto

**QNOTA** é uma plataforma acadêmica completa desenvolvida seguindo os princípios de **Domain-Driven Design (DDD)** e **Arquitetura em Camadas**. O sistema permite que coordenadores e professores gerenciem simulados, turmas, alunos e rankings de forma automatizada e eficiente.

### ✨ Principais Funcionalidades

- 📊 **Gestão de Simulados**: Criação e gerenciamento de simulados com múltiplas disciplinas e pesos configuráveis
- 🎯 **Sistema de Rankings**: Cálculo automático de rankings baseado em médias ponderadas
- 👥 **Gestão de Usuários**: Coordenadores, professores e alunos com perfis distintos
- 📝 **Lançamento de Notas**: Interface intuitiva para registro e acompanhamento de notas
- 📈 **Análise de Desempenho**: Visualização de rankings e estatísticas por turma e simulado
- 🔄 **Validações de Negócio**: Regras complexas garantindo integridade dos dados

### 🎯 Objetivos do Projeto

Este projeto foi desenvolvido como parte de um trabalho acadêmico focado em:

- **Arquitetura de Software**: Implementação de arquitetura em camadas com separação clara de responsabilidades
- **Padrões de Projeto**: Aplicação prática de Design Patterns (Strategy, Template Method, Observer, Decorator)
- **BDD (Behavior-Driven Development)**: Documentação viva dos requisitos através de cenários Gherkin/Cucumber
- **Clean Code**: Código limpo, testável e manutenível seguindo boas práticas
- **Full Stack Development**: Desenvolvimento completo de backend e frontend

---

## 🛠️ Stack Tecnológica

### Backend
- **Java 17+** - Linguagem principal
- **Spring Boot 3.x** - Framework para aplicações Java
- **Spring Data JPA** - Persistência de dados
- **PostgreSQL 17** - Banco de dados relacional
- **Flyway** - Versionamento e migração de banco de dados
- **Maven** - Gerenciamento de dependências e build
- **Cucumber** - Testes BDD e documentação viva

### Frontend
- **React 18** - Biblioteca JavaScript para interfaces
- **TypeScript** - Tipagem estática
- **Vite** - Build tool e dev server
- **Tailwind CSS** - Framework CSS utilitário

### DevOps & Ferramentas
- **Docker & Docker Compose** - Containerização e orquestração
- **Git** - Controle de versão

---

## 🏗️ Arquitetura em Camadas

O projeto segue uma arquitetura em camadas bem definida, garantindo separação de responsabilidades e facilitando manutenção e testes:

```
┌─────────────────────────────────────────────────────┐
│         Apresentação Frontend (React)               │
│              localhost:3000                         │
│         TypeScript + Tailwind CSS                   │
└─────────────────────────────────────────────────────┘
                      ↓ HTTP REST
┌─────────────────────────────────────────────────────┐
│    Apresentação Backend (Spring Boot + Controllers) │
│              localhost:8080                         │
│         REST API + DTOs de Apresentação             │
└─────────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────┐
│         Aplicação (Interfaces de Consulta)          │
│              Resumos, DTOs, Casos de Uso            │
└─────────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────┐
│    Domínio Principal (Entidades, Serviços, RNs)    │
│   Padrões: Strategy, Template, Observer, Decorator │
│         Regras de Negócio + Validações             │
└─────────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────┐
│      Infraestrutura (JPA, Repositórios, Flyway)    │
│             PostgreSQL (Docker)                     │
│              localhost:5433                         │
│         Migrações Automáticas                       │
└─────────────────────────────────────────────────────┘
```

### 📦 Estrutura Modular (Maven Multi-Módulo)

```
qnota/
├── dominio-principal/           # 🎯 Núcleo do negócio
│   ├── Entidades de domínio
│   ├── Serviços de domínio
│   ├── Regras de negócio (RN-*)
│   └── Testes BDD (Cucumber/Gherkin)
│
├── aplicacao/                   # 🔄 Casos de uso
│   └── Interfaces de consulta, DTOs, Resumos
│
├── infraestrutura/              # 💾 Persistência
│   ├── Implementações JPA
│   ├── Repositórios concretos
│   └── Migrações Flyway
│
├── apresentacao-backend/         # 🌐 API REST
│   └── Controllers, Configuração Spring Boot
│
└── apresentacao-frontend/       # 🎨 Interface do usuário
    └── React + TypeScript + Vite
```

---

## 🚀 Como Rodar o Projeto

### Pré-requisitos

Certifique-se de ter instalado as seguintes ferramentas:

| Ferramenta | Versão Mínima | Como Verificar |
|------------|---------------|----------------|
| **Java JDK** | 17+ | `java -version` |
| **Maven** | 3.8+ | `mvn -version` |
| **Docker** | Latest | `docker --version` |
| **Docker Compose** | Latest | `docker compose version` |
| **Node.js** | 18+ | `node --version` |
| **npm** | 9+ | `npm --version` |

### 📥 1. Clonar o Repositório

```bash
git clone <url-do-repositorio>
cd qnota
```

### 🔨 2. Compilar o Projeto

Compile todos os módulos Maven:

```bash
mvn clean install -DskipTests
```

⏱️ **Tempo estimado:** ~30 segundos

**O que acontece:**
- Compila os módulos: `dominio-principal`, `aplicacao`, `infraestrutura`, `apresentacao-backend`, `apresentacao-frontend`
- Gera os JARs em `target/` de cada módulo

### 🐳 3. Subir o Banco de Dados (PostgreSQL)

O projeto usa PostgreSQL em container Docker:

```bash
docker compose up -d
```

**Verificar se subiu:**

```bash
docker ps
```

Você deve ver o container `qnota-postgres` rodando na porta `5433`.

**Configuração do banco:**
- **Host:** `localhost`
- **Porta:** `5433`
- **Database:** `qnota`
- **Usuário:** `qnota`
- **Senha:** `qnota`

**Migrações Flyway:**  
As migrações (`V1__*.sql`, `V2__*.sql`) rodam automaticamente quando o backend iniciar.

### ⚙️ 4. Rodar o Backend (Spring Boot)

#### Opção 1: Via Maven (Recomendado)

```bash
cd apresentacao-backend
mvn test-compile exec:java "-Dexec.mainClass=dev.com.qnota.BackendDesenvolvimentoAplicacao" "-Dexec.classpathScope=test"
```

#### Opção 2: Via IDE (IntelliJ/Eclipse)

1. Abra a classe `apresentacao-backend/src/test/java/dev/com/qnota/BackendDesenvolvimentoAplicacao.java`
2. Clique com botão direito → **Run** (ou **Debug**)

⏱️ **Tempo de inicialização:** ~7-10 segundos

**Verificar se subiu:**

```bash
# PowerShell
Invoke-RestMethod -Uri http://localhost:8080/backend/coordenador/pesquisa

# Linux/Mac
curl http://localhost:8080/backend/coordenador/pesquisa
```

Deve retornar um JSON (pode estar vazio `[]` se não houver dados).

**Configurações:**
- **Porta:** `8080`
- **Perfil ativo:** `desenvolvimento`
- **Logs:** Console do terminal/IDE

### 🎨 5. Rodar o Frontend (React + Vite)

Em **outro terminal**:

```bash
cd apresentacao-frontend
npm install
npm run dev
```

⏱️ **Tempo estimado:**
- `npm install`: ~30 segundos (primeira vez)
- `npm run dev`: ~2 segundos

**Verificar se subiu:**

Você deve ver:

```
VITE v5.4.21  ready in 1407 ms

➜  Local:   http://localhost:3000/
➜  Network: use --host to expose
```

**Acesse:** [http://localhost:3000](http://localhost:3000)

### ✅ Resumo dos Serviços Rodando

| Serviço | URL | Porta | Status |
|---------|-----|-------|--------|
| **Frontend** | http://localhost:3000 | 3000 | 🟢 Vite Dev Server |
| **Backend** | http://localhost:8080 | 8080 | 🟢 Spring Boot |
| **PostgreSQL** | localhost:5433 | 5433 | 🟢 Docker Container |

---

## 🧪 Como Rodar os Testes

### Testes de Domínio (Cucumber BDD)

```bash
mvn test -pl dominio-principal
```

**O que testa:**
- Cenários Gherkin (`.feature`)
- Regras de negócio (RN-*)
- Entidades de domínio
- Comportamentos esperados do sistema

### Todos os Testes (Exceto Infraestrutura)

```bash
mvn test
```

**Observação:** Os testes de infraestrutura foram removidos intencionalmente.

---

## 🎯 Endpoints Principais da API

| Endpoint | Método | Descrição |
|----------|--------|-----------|
| `/backend/coordenador/pesquisa` | GET | Lista todos os coordenadores |
| `/backend/coordenador/cadastrar` | POST | Cadastra novo coordenador |
| `/backend/professor/pesquisa` | GET | Lista todos os professores |
| `/backend/aluno/pesquisa` | GET | Lista todos os alunos |
| `/backend/aluno/criacao` | GET | Dados para formulário de criação |
| `/backend/turma/pesquisa` | GET | Lista todas as turmas |
| `/backend/simulado/pesquisa` | GET | Lista todos os simulados |
| `/backend/simulado/criacao` | GET | Dados para formulário de criação |
| `/backend/ranking/pesquisa` | GET | Lista rankings de simulados |
| `/backend/nota/pesquisa` | GET | Lista todas as notas |

**Base URL:** `http://localhost:8080`

---

## 🔑 Conceitos-Chave do Domínio

### Entidades Principais

- **Simulado**: Avaliação composta por múltiplas disciplinas com pesos configuráveis
- **Ranking**: Ordenação automática de alunos por média ponderada em um simulado
- **Nota**: Valor atribuído a um aluno em uma disciplina específica de um simulado
- **Turma**: Conjunto de alunos sob responsabilidade de um professor
- **Disciplina**: Matéria com área de conhecimento associada

### Regras de Negócio Principais

| Código | Descrição |
|--------|-----------|
| **RN-12** | Simulado deve ter pelo menos 2 disciplinas |
| **RN-13** | Pesos das disciplinas devem somar exatamente 10 |
| **RN-16** | Todas as notas devem estar lançadas antes de finalizar simulado |
| **RN-52** | Máximo de 3 simulados em edição por turma |
| **RN-98/99** | Recalcular ranking automaticamente quando disciplinas forem editadas |
| **RN-102** | Congelar ranking após finalização do simulado |

---

## 📚 Documentação

### 📖 Documentação Técnica

- **Cenários Gherkin (Living Documentation):**  
  `./dominio-principal/src/test/resources/dev/com/qnota/dominio/principal/*.feature`

- **StoryMap e Descrição Detalhada:**  
  Diretório `./Documentacao/` contém:
  - [Descricao-Detalhada-Qnota.pdf](https://github.com/user-attachments/files/23062028/Descricao-Detalhada-Qnota.pdf)
  - `Storymap aqui!.pdf`

- **Context Mapper (limites/contextos):**  
  `./qnota.cml`

- **Padrões de Projeto Aplicados:**  
  `./padroes.md` (Decorator, Strategy, Template Method, Observer)

### 🎨 Protótipo

- [Figma - Navegável QNota para Coordenador](https://www.figma.com/make/wx4obguhFd80NeadgO1Knm/Naveg%C3%A1vel-QNota-para-Coordenador?node-id=0-1&p=f&t=vP0KSof6mrg4P5Wd-0&fullscreen=1)

### 📊 Apresentação

- [Apresentação Qnota.pdf](https://github.com/user-attachments/files/23062053/Apresentacao.Qnota.pdf)

---

## 🛑 Como Parar os Serviços

### Parar Backend e Frontend
Pressione `Ctrl+C` nos terminais onde estão rodando.

### Parar o Banco de Dados

```bash
docker compose down
```

---

## 🔧 Troubleshooting (Erros Comuns)

### ❌ Erro: `Cannot load driver class: org.postgresql.Driver`

**Causa:** Dependência do PostgreSQL não encontrada.

**Solução:**

```bash
mvn clean install -DskipTests
```

---

### ❌ Erro: `Failed to configure a DataSource: 'url' attribute is not specified`

**Causa:** Backend rodando sem o perfil `desenvolvimento`.

**Solução:** Use a classe `BackendDesenvolvimentoAplicacao` ou ative o perfil manualmente:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=desenvolvimento -pl apresentacao-backend
```

---

### ❌ Erro: `ERR_CONNECTION_REFUSED` ao acessar http://localhost:3000

**Causa:** Frontend não está rodando.

**Solução:**

```bash
cd apresentacao-frontend
npm run dev
```

---

### ❌ Erro: `Port 8080 is already in use`

**Causa:** Outro processo está usando a porta 8080.

**Solução (PowerShell):**

```powershell
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
```

**Solução (Linux/Mac):**

```bash
lsof -ti:8080 | xargs kill -9
```

---

### ❌ Erro: `Connection to localhost:5433 refused`

**Causa:** Container PostgreSQL não está rodando.

**Solução:**

```bash
docker compose up -d
docker ps  # verificar se está UP
```

---

### ❌ Erro: `Flyway migration failed`

**Causa:** Esquema do banco está inconsistente.

**Solução:** Recriar o banco do zero:

```bash
docker compose down -v  # remove volumes
docker compose up -d
```

---

### ❌ Erro: Dependências do frontend não encontradas

**Causa:** `node_modules` não foi instalado ou está corrompido.

**Solução:**

```bash
cd apresentacao-frontend
rm -rf node_modules package-lock.json
npm install
```

---

## 📁 Estrutura Detalhada do Projeto

```
qnota/
├── dominio-principal/           # Entidades, serviços de domínio, regras de negócio
│   ├── src/main/java/          # Código de domínio
│   └── src/test/resources/     # *.feature (Gherkin/Cucumber)
│
├── aplicacao/                   # Interfaces de consulta, resumos, DTOs de aplicação
│   └── src/main/java/
│
├── infraestrutura/              # Implementações JPA, repositórios concretos
│   ├── src/main/java/
│   └── src/main/resources/     # Migrações Flyway (V1__*.sql)
│
├── apresentacao-backend/        # REST Controllers, configuração Spring Boot
│   ├── src/main/java/          # Controllers, DTOs de apresentação
│   ├── src/main/resources/     # application.properties (vazio)
│   └── src/test/
│       ├── java/               # BackendDesenvolvimentoAplicacao
│       └── resources/          # application-desenvolvimento.properties
│
├── apresentacao-frontend/       # React + TypeScript + Vite + Tailwind CSS
│   ├── src/
│   ├── package.json
│   └── vite.config.ts
│
├── docker-compose.yml           # PostgreSQL container
├── padroes.md                   # Documentação dos padrões de projeto
├── TESTE.md                     # Casos de teste
└── README.md                    # Este arquivo
```

---

## 🎓 Aprendizados e Diferenciais

Este projeto demonstra a aplicação prática de:

- ✅ **Domain-Driven Design (DDD)**: Modelagem rica do domínio com entidades, value objects e serviços
- ✅ **Arquitetura em Camadas**: Separação clara de responsabilidades entre camadas
- ✅ **Design Patterns**: Implementação de Strategy, Template Method, Observer e Decorator
- ✅ **BDD com Cucumber**: Documentação viva dos requisitos através de cenários Gherkin
- ✅ **Clean Architecture**: Independência do framework e facilidade de testes
- ✅ **API RESTful**: Design de APIs seguindo boas práticas REST
- ✅ **Containerização**: Uso de Docker para ambiente de desenvolvimento consistente
- ✅ **Versionamento de Banco**: Migrações automatizadas com Flyway

---

## 📄 Licença

Este projeto foi desenvolvido para fins acadêmicos.

---

<div align="center">

**Desenvolvido com ❤️ para demonstrar boas práticas de desenvolvimento de software**

⭐ Se este projeto foi útil, considere dar uma estrela!

</div>
