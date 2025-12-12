# QNOTA

Monorepo do **QNOTA** — plataforma acadêmica organizada em módulos (Domínio, Aplicação, Infraestrutura e Camadas de Apresentação).  
O repositório é **multi-módulo Maven** e usa **BDD (Cucumber)** como documentação viva dos requisitos.

---

## 📚 Documentação

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

---

## 🎨 Protótipo

- [Figma - Navegável QNota para Coordenador](https://www.figma.com/make/wx4obguhFd80NeadgO1Knm/Naveg%C3%A1vel-QNota-para-Coordenador?node-id=0-1&p=f&t=vP0KSof6mrg4P5Wd-0&fullscreen=1)

---

## 📊 Apresentação

- [Apresentação Qnota.pdf](https://github.com/user-attachments/files/23062053/Apresentacao.Qnota.pdf)

---

## 🚀 Como Rodar o Projeto

### Pré-requisitos

Certifique-se de ter instalado:

- **Java 17+** (JDK)
- **Maven 3.8+**
- **Docker** e **Docker Compose**
- **Node.js 18+** e **npm** (para o frontend)

Verifique as versões:

```bash
java -version
mvn -version
docker --version
node --version
npm --version
```

---

### 1️⃣ Clonar o Repositório

```bash
git clone <url-do-repositorio>
cd qnota
```

---

### 2️⃣ Compilar o Projeto

Compile todos os módulos Maven:

```bash
mvn clean install -DskipTests
```

**Tempo estimado:** ~30 segundos

**O que acontece:**
- Compila os módulos: `dominio-principal`, `aplicacao`, `infraestrutura`, `apresentacao-backend`, `apresentacao-frontend`
- Gera os JARs em `target/` de cada módulo

---

### 3️⃣ Subir o Banco de Dados (PostgreSQL)

O projeto usa PostgreSQL em container Docker:

```bash
docker compose up -d
```

**Verificar se subiu:**

```bash
docker ps
```

Você deve ver algo como:

```
CONTAINER ID   IMAGE         COMMAND                  STATUS         PORTS                    NAMES
abc123...      postgres:17   "docker-entrypoint..."   Up 5 seconds   0.0.0.0:5433->5432/tcp   qnota-postgres
```

**Configuração do banco:**
- Host: `localhost`
- Porta: `5433`
- Database: `qnota`
- Usuário: `qnota`
- Senha: `qnota`

**Migrações Flyway:**  
As migrações (`V1__*.sql`, `V2__*.sql`) rodam automaticamente quando o backend iniciar.

---

### 4️⃣ Rodar o Backend (Spring Boot)

#### Opção 1: Via Maven (Recomendado para Desenvolvimento)

```bash
cd apresentacao-backend
mvn test-compile exec:java "-Dexec.mainClass=dev.com.qnota.BackendDesenvolvimentoAplicacao" "-Dexec.classpathScope=test"
```

#### Opção 2: Via IDE (IntelliJ/Eclipse)

1. Abra a classe `apresentacao-backend/src/test/java/dev/com/qnota/BackendDesenvolvimentoAplicacao.java`
2. Clique com botão direito → **Run** (ou **Debug**)

**Tempo de inicialização:** ~7-10 segundos

**Verificar se subiu:**

```bash
# PowerShell
Invoke-RestMethod -Uri http://localhost:8080/backend/coordenador/pesquisa

# Linux/Mac
curl http://localhost:8080/backend/coordenador/pesquisa
```

Deve retornar um JSON (pode estar vazio `[]` se não houver dados).

**Porta:** `8080`  
**Perfil ativo:** `desenvolvimento`  
**Logs:** Console do terminal/IDE

---

### 5️⃣ Rodar o Frontend (React + Vite)

Em **outro terminal**:

```bash
cd apresentacao-frontend
npm install
npm run dev
```

**Tempo estimado:**
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

---

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

### Todos os Testes (Exceto Infraestrutura)

```bash
mvn test
```

**Observação:** Os testes de infraestrutura foram removidos intencionalmente.

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

## 📁 Estrutura do Projeto

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

## 🏗️ Arquitetura em Camadas

```
┌─────────────────────────────────────────────────────┐
│         Apresentação Frontend (React)               │
│              localhost:3000                         │
└─────────────────────────────────────────────────────┘
                      ↓ HTTP REST
┌─────────────────────────────────────────────────────┐
│    Apresentação Backend (Spring Boot + Controllers) │
│              localhost:8080                         │
└─────────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────┐
│         Aplicação (Interfaces de Consulta)          │
│              Resumos, DTOs                          │
└─────────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────┐
│    Domínio Principal (Entidades, Serviços, RNs)    │
│   Padrões: Strategy, Template, Observer, Decorator │
└─────────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────────┐
│      Infraestrutura (JPA, Repositórios, Flyway)    │
│             PostgreSQL (Docker)                     │
│              localhost:5433                         │
└─────────────────────────────────────────────────────┘
```

---

## 🎯 Endpoints Principais do Backend

| Endpoint | Método | Descrição |
|----------|--------|-----------|
| `/backend/coordenador/pesquisa` | GET | Lista coordenadores |
| `/backend/coordenador/cadastrar` | POST | Cadastra coordenador |
| `/backend/professor/pesquisa` | GET | Lista professores |
| `/backend/aluno/pesquisa` | GET | Lista alunos |
| `/backend/aluno/criacao` | GET | Dados para formulário de criação |
| `/backend/turma/pesquisa` | GET | Lista turmas |
| `/backend/simulado/pesquisa` | GET | Lista simulados |
| `/backend/simulado/criacao` | GET | Dados para formulário de criação |
| `/backend/ranking/pesquisa` | GET | Lista rankings |
| `/backend/nota/pesquisa` | GET | Lista todas as notas |

**Base URL:** `http://localhost:8080`

---

## 🔑 Conceitos-Chave do Domínio

- **Simulado**: Avaliação composta por múltiplas disciplinas com pesos
- **Ranking**: Ordenação de alunos por média ponderada em um simulado
- **Nota**: Valor atribuído a um aluno em uma disciplina de um simulado
- **Turma**: Conjunto de alunos sob responsabilidade de um professor
- **Disciplina**: Matéria com área de conhecimento associada

### Regras de Negócio Principais

- **RN-12**: Simulado deve ter pelo menos 2 disciplinas
- **RN-13**: Pesos das disciplinas devem somar 10
- **RN-16**: Todas as notas devem estar lançadas antes de finalizar simulado
- **RN-52**: Máximo de 3 simulados em edição por turma
- **RN-98/99**: Recalcular ranking quando disciplinas forem editadas
- **RN-102**: Congelar ranking após finalização do simulado

---

