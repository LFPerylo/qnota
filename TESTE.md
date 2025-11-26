# Guia de Testes - QNota

Este guia mostra como testar se todas as correções estão funcionando corretamente.

## 📋 Pré-requisitos

- Docker Desktop instalado e rodando
- Maven instalado
- Java 17+ instalado

## 🧪 Teste 1: Verificar estrutura de migrações

### 1.1 Verificar se as migrações estão na infraestrutura

```powershell
# Verificar estrutura
Get-ChildItem -Recurse -Path "infraestrutura\src\main\resources\db\migration" | Select-Object Name
```

**Resultado esperado:**
- `V1__QNota_criacao.sql`
- `V2__QNota_Povoamento.sql`

### 1.2 Verificar que NÃO há migrações no backend

```powershell
# Deve retornar vazio ou erro
Test-Path "apresentacao-backend\src\main\resources\db\migration"
```

**Resultado esperado:** `False` (diretório não existe)

## 🐳 Teste 2: Testar Docker Compose com PostgreSQL

### 2.1 Subir o PostgreSQL

```powershell
cd qnota
docker-compose up -d
```

**Verificar se o container está rodando:**
```powershell
docker ps | Select-String "qnota-postgres"
```

### 2.2 Verificar logs do PostgreSQL

```powershell
docker logs qnota-postgres
```

**Resultado esperado:** Mensagens de inicialização do PostgreSQL sem erros

### 2.3 Testar conexão com o banco

```powershell
docker exec -it qnota-postgres psql -U qnota -d qnota -c "\dt"
```

**Resultado esperado:** Lista de tabelas (vazia inicialmente, ou com tabelas se já rodou as migrações)

## 🚀 Teste 3: Testar Flyway com PostgreSQL

### 3.1 Rodar a aplicação com profile PostgreSQL

```powershell
cd apresentacao-backend
mvn spring-boot:run -Dspring-boot.run.profiles=postgresql
```

**OU** se preferir compilar primeiro:

```powershell
mvn clean package
java -jar target/qnota-apresentacao-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=postgresql
```

### 3.2 Verificar logs do Flyway

Procure por mensagens como:
```
Flyway migration successful
```

**Verificar se as migrações rodaram:**
```powershell
docker exec -it qnota-postgres psql -U qnota -d qnota -c "SELECT * FROM flyway_schema_history;"
```

**Resultado esperado:** 
- 2 registros (V1 e V2)
- Status: `Success`

### 3.3 Verificar se as tabelas foram criadas

```powershell
docker exec -it qnota-postgres psql -U qnota -d qnota -c "\dt qnota.*"
```

**Resultado esperado:** Lista com todas as tabelas:
- coordenadores
- responsaveis
- professores
- areas_conhecimento
- disciplinas
- turmas
- alunos
- aluno_responsaveis
- simulados
- simulado_disciplinas
- notas_do_aluno
- justificativas
- rankings
- ranking_linhas

### 3.4 Verificar se os dados foram populados

```powershell
docker exec -it qnota-postgres psql -U qnota -d qnota -c "SELECT COUNT(*) FROM qnota.coordenadores;"
docker exec -it qnota-postgres psql -U qnota -d qnota -c "SELECT COUNT(*) FROM qnota.alunos;"
```

**Resultado esperado:** Contagens > 0

## 🧹 Teste 4: Testar com H2 (modo desenvolvimento)

### 4.1 Rodar sem profile (usa H2 por padrão)

```powershell
mvn spring-boot:run
```

**Verificar logs:** Deve mostrar Flyway rodando as migrações no H2

### 4.2 Verificar que arquivos .db não são versionados

```powershell
git status | Select-String "\.db"
```

**Resultado esperado:** Nenhum arquivo .db aparecendo (ou apenas como "untracked")

## ✅ Checklist Final

- [ ] Migrações estão apenas em `infraestrutura/src/main/resources/db/migration/`
- [ ] Migrações seguem padrão Flyway (V1__, V2__)
- [ ] Docker Compose sobe PostgreSQL sem erros
- [ ] Flyway executa migrações no PostgreSQL
- [ ] Tabelas são criadas corretamente
- [ ] Dados são populados (V2)
- [ ] Arquivos .mv.db e .trace.db não aparecem no `git status`
- [ ] Aplicação roda com H2 (sem profile)
- [ ] Aplicação roda com PostgreSQL (profile postgresql)

## 🐛 Troubleshooting

### Erro: "Port 5432 already in use"
```powershell
# Verificar o que está usando a porta
netstat -ano | findstr :5432
# Parar o processo ou mudar a porta no docker-compose.yml
```

### Erro: "Flyway migration failed"
- Verificar logs: `docker logs qnota-postgres`
- Verificar se o schema `qnota` existe: `\dn` no psql
- Limpar e recriar: `docker-compose down -v` e `docker-compose up -d`

### Erro: "Connection refused"
- Verificar se Docker está rodando
- Verificar se container está ativo: `docker ps`
- Verificar healthcheck: `docker inspect qnota-postgres | Select-String "Health"`

