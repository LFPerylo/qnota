-- =========================================
-- QNOTA - ESQUEMA RELACIONAL (MySQL 8.0+)
-- Sem triggers; com coluna gerada p/ 1 principal/aluno
-- =========================================
CREATE DATABASE IF NOT EXISTS qnota CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE qnota;

-- COORDENADORES
CREATE TABLE IF NOT EXISTS coordenadores (
  id                 INT AUTO_INCREMENT PRIMARY KEY,
  nome               VARCHAR(255) NOT NULL,
  enderecoEletronico VARCHAR(255) NOT NULL UNIQUE,
  senhaHash          VARCHAR(255) NOT NULL,
  ativo              TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- RESPONSAVEIS
CREATE TABLE IF NOT EXISTS responsaveis (
  id                 INT AUTO_INCREMENT PRIMARY KEY,
  nome               VARCHAR(255) NOT NULL,
  cpf                VARCHAR(14)  NOT NULL UNIQUE,
  enderecoEletronico VARCHAR(255) NOT NULL,
  status             ENUM('ATIVO','INADIMPLENTE','INATIVO') NOT NULL DEFAULT 'ATIVO'
) ENGINE=InnoDB;

-- PROFESSORES
CREATE TABLE IF NOT EXISTS professores (
  id                 INT AUTO_INCREMENT PRIMARY KEY,
  nome               VARCHAR(255) NOT NULL,
  cpf                VARCHAR(14)  NOT NULL UNIQUE,
  enderecoEletronico VARCHAR(255) NOT NULL,
  especialidades     JSON NULL
) ENGINE=InnoDB;

-- AREAS / DISCIPLINAS
CREATE TABLE IF NOT EXISTS areas_conhecimento (
  id   INT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(255) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS disciplinas (
  id             INT AUTO_INCREMENT PRIMARY KEY,
  nome           VARCHAR(255) NOT NULL,
  versao         INT NOT NULL DEFAULT 1,
  idVersaoOrigem INT NULL,
  ativo          TINYINT(1) NOT NULL DEFAULT 1,
  area_id        INT NOT NULL,
  CONSTRAINT fk_disc_area FOREIGN KEY (area_id) REFERENCES areas_conhecimento(id),
  CONSTRAINT ux_disc_nome_area_versao UNIQUE (nome, area_id, versao)
) ENGINE=InnoDB;

-- TURMAS
CREATE TABLE IF NOT EXISTS turmas (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  nome         VARCHAR(255) NOT NULL,
  anoLetivo    INT NOT NULL,
  ativo        TINYINT(1) NOT NULL DEFAULT 1,
  professor_id INT NOT NULL,
  CONSTRAINT fk_turma_prof FOREIGN KEY (professor_id) REFERENCES professores(id),
  CONSTRAINT ux_turma_nome_ano UNIQUE (nome, anoLetivo)
) ENGINE=InnoDB;

-- ALUNOS
CREATE TABLE IF NOT EXISTS alunos (
  id             INT AUTO_INCREMENT PRIMARY KEY,
  nome           VARCHAR(255) NOT NULL,
  dataNascimento DATE NOT NULL,
  ativo          TINYINT(1) NOT NULL DEFAULT 1,
  turma_id       INT NOT NULL,
  CONSTRAINT fk_aluno_turma FOREIGN KEY (turma_id) REFERENCES turmas(id)
) ENGINE=InnoDB;

-- VÍNCULO ALUNO-RESPONSAVEL (N:N) com “um principal por aluno” SEM TRIGGER
CREATE TABLE IF NOT EXISTS aluno_responsaveis (
  responsavel_id INT NOT NULL,
  principal      TINYINT(1) NOT NULL DEFAULT 0,
  aluno_id       INT NOT NULL,
  -- coluna gerada: igual a aluno_id se principal=1, senão NULL
  principal_aluno_id INT GENERATED ALWAYS AS (IF(principal=1, aluno_id, NULL)) VIRTUAL,
  PRIMARY KEY (responsavel_id, aluno_id),
  CONSTRAINT fk_ar_resp  FOREIGN KEY (responsavel_id) REFERENCES responsaveis(id) ON DELETE RESTRICT,
  CONSTRAINT fk_ar_aluno FOREIGN KEY (aluno_id)       REFERENCES alunos(id)       ON DELETE CASCADE,
  INDEX ix_ar_aluno (aluno_id),
  UNIQUE KEY ux_ar_um_principal_por_aluno (principal_aluno_id)
) ENGINE=InnoDB;

-- SIMULADOS
CREATE TABLE IF NOT EXISTS simulados (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  dataAplicacao DATE NOT NULL,
  status        ENUM('EM_EDICAO','FINALIZADO') NOT NULL DEFAULT 'EM_EDICAO',
  turma_id      INT NOT NULL,
  CONSTRAINT fk_sim_turma FOREIGN KEY (turma_id) REFERENCES turmas(id),
  INDEX ix_sim_turma (turma_id)
) ENGINE=InnoDB;

-- DISCIPLINAS DO SIMULADO (com pesos)
CREATE TABLE IF NOT EXISTS simulado_disciplinas (
  simulado_id   INT NOT NULL,
  disciplina_id INT NOT NULL,
  peso          DOUBLE NOT NULL,
  PRIMARY KEY (simulado_id, disciplina_id),
  CONSTRAINT fk_sd_sim FOREIGN KEY (simulado_id)   REFERENCES simulados(id)   ON DELETE CASCADE,
  CONSTRAINT fk_sd_dis FOREIGN KEY (disciplina_id) REFERENCES disciplinas(id)
) ENGINE=InnoDB;

-- NOTAS DO ALUNO
CREATE TABLE IF NOT EXISTS notas_do_aluno (
  aluno_id       INT NOT NULL,
  simulado_id    INT NOT NULL,
  disciplina_id  INT NOT NULL,
  valor          DOUBLE NOT NULL,
  dataLancamento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (aluno_id, simulado_id, disciplina_id),
  CONSTRAINT fk_nota_aluno      FOREIGN KEY (aluno_id)      REFERENCES alunos(id)      ON DELETE CASCADE,
  CONSTRAINT fk_nota_simulado   FOREIGN KEY (simulado_id)   REFERENCES simulados(id)   ON DELETE CASCADE,
  CONSTRAINT fk_nota_disciplina FOREIGN KEY (disciplina_id) REFERENCES disciplinas(id),
  INDEX ix_nota_sim (simulado_id),
  INDEX ix_nota_disc (disciplina_id)
) ENGINE=InnoDB;

-- JUSTIFICATIVAS (histórico)
CREATE TABLE IF NOT EXISTS justificativas (
  id            VARCHAR(50) PRIMARY KEY,   -- gere UUID pela aplicação
  aluno_id      INT NOT NULL,
  simulado_id   INT NOT NULL,
  disciplina_id INT NOT NULL,
  professor_id  INT NOT NULL,
  notaAnterior  DOUBLE NOT NULL,
  notaCorrigida DOUBLE NOT NULL,
  texto         TEXT   NOT NULL,
  dataHora      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_just_aluno      FOREIGN KEY (aluno_id)      REFERENCES alunos(id)      ON DELETE CASCADE,
  CONSTRAINT fk_just_simulado   FOREIGN KEY (simulado_id)   REFERENCES simulados(id)   ON DELETE CASCADE,
  CONSTRAINT fk_just_disciplina FOREIGN KEY (disciplina_id) REFERENCES disciplinas(id),
  CONSTRAINT fk_just_prof       FOREIGN KEY (professor_id)  REFERENCES professores(id),
  INDEX ix_just_nota (aluno_id, simulado_id, disciplina_id)
) ENGINE=InnoDB;

-- RANKINGS
CREATE TABLE IF NOT EXISTS rankings (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  congelado   TINYINT(1) NOT NULL DEFAULT 0,
  simulado_id INT NOT NULL UNIQUE,
  CONSTRAINT fk_rank_sim FOREIGN KEY (simulado_id) REFERENCES simulados(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ranking_linhas (
  ranking_id INT NOT NULL,
  aluno_id   INT NOT NULL,
  media      DOUBLE NOT NULL,
  posicao    INT NOT NULL,
  PRIMARY KEY (ranking_id, aluno_id),
  CONSTRAINT fk_rl_rank  FOREIGN KEY (ranking_id) REFERENCES rankings(id) ON DELETE CASCADE,
  CONSTRAINT fk_rl_aluno FOREIGN KEY (aluno_id)   REFERENCES alunos(id),
  CONSTRAINT ux_rl_rank_pos UNIQUE (ranking_id, posicao)
) ENGINE=InnoDB;
