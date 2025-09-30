
DROP DATABASE IF EXISTS qnota;
CREATE DATABASE qnota;
USE qnota;

-- ================================
-- PROFESSOR E ESPECIALIDADES
-- ================================
CREATE TABLE professor (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    ativo BOOLEAN DEFAULT TRUE
);

CREATE TABLE area_conhecimento (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE professor_area (
    id_professor INT NOT NULL,
    id_area INT NOT NULL,
    PRIMARY KEY (id_professor, id_area),
    FOREIGN KEY (id_professor) REFERENCES professor(id),
    FOREIGN KEY (id_area) REFERENCES area_conhecimento(id)
);

-- ================================
-- RESPONSÁVEIS
-- ================================
CREATE TABLE responsavel (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    status ENUM('ATIVO', 'INADIMPLENTE', 'INATIVO') NOT NULL DEFAULT 'ATIVO'
);

-- ================================
-- TURMAS
-- ================================
CREATE TABLE turma (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    ano_letivo INT NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    id_professor INT NOT NULL,
    UNIQUE (nome, ano_letivo),
    FOREIGN KEY (id_professor) REFERENCES professor(id)
);

-- ================================
-- ALUNOS
-- ================================
CREATE TABLE aluno (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    data_nascimento DATE NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    id_turma INT NOT NULL,
    UNIQUE (nome, data_nascimento, id_turma),
    FOREIGN KEY (id_turma) REFERENCES turma(id)
);

CREATE TABLE aluno_responsavel (
    id_aluno INT,
    id_responsavel INT,
    grau_parentesco VARCHAR(50),
    principal BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (id_aluno, id_responsavel),
    FOREIGN KEY (id_aluno) REFERENCES aluno(id),
    FOREIGN KEY (id_responsavel) REFERENCES responsavel(id)
);

-- ================================
-- DISCIPLINAS
-- ================================
CREATE TABLE disciplina (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    id_area INT NOT NULL,
    versao INT NOT NULL DEFAULT 1,
    id_versao_origem INT,
    ativo BOOLEAN DEFAULT TRUE,
    UNIQUE (nome, id_area, versao),
    FOREIGN KEY (id_area) REFERENCES area_conhecimento(id)
);

-- ================================
-- SIMULADOS
-- ================================
CREATE TABLE simulado (
    id INT AUTO_INCREMENT PRIMARY KEY,
    data_aplicacao DATE NOT NULL,
    status ENUM('EM_EDICAO', 'FINALIZADO') NOT NULL DEFAULT 'EM_EDICAO',
    id_turma INT NOT NULL,
    FOREIGN KEY (id_turma) REFERENCES turma(id)
);

CREATE TABLE simulado_disciplina (
    id_simulado INT,
    id_disciplina INT,
    peso DECIMAL(4,2) NOT NULL,
    PRIMARY KEY (id_simulado, id_disciplina),
    FOREIGN KEY (id_simulado) REFERENCES simulado(id),
    FOREIGN KEY (id_disciplina) REFERENCES disciplina(id)
);

-- ================================
-- NOTAS E JUSTIFICATIVAS
-- ================================
CREATE TABLE nota_aluno_disciplina (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_aluno INT NOT NULL,
    id_simulado INT NOT NULL,
    id_disciplina INT NOT NULL,
    valor DECIMAL(4,2) NOT NULL,
    data_lancamento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (id_aluno, id_simulado, id_disciplina),
    FOREIGN KEY (id_aluno) REFERENCES aluno(id),
    FOREIGN KEY (id_simulado) REFERENCES simulado(id),
    FOREIGN KEY (id_disciplina) REFERENCES disciplina(id),
    CHECK (valor >= 0.00 AND valor <= 10.00)
);

CREATE TABLE justificativa (
    id INT AUTO_INCREMENT PRIMARY KEY,
    texto TEXT NOT NULL,
    data_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_professor INT NOT NULL,
    id_nota INT NOT NULL,
    nota_anterior DECIMAL(4,2) NOT NULL,
    nota_corrigida DECIMAL(4,2) NOT NULL,
    FOREIGN KEY (id_professor) REFERENCES professor(id),
    FOREIGN KEY (id_nota) REFERENCES nota_aluno_disciplina(id)
);

-- ================================
-- RANKING
-- ================================
CREATE TABLE ranking (
    id_simulado INT NOT NULL,
    id_aluno INT NOT NULL,
    media DECIMAL(5,2) NOT NULL,
    posicao INT NOT NULL,
    congelado BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id_simulado, id_aluno),
    FOREIGN KEY (id_simulado) REFERENCES simulado(id),
    FOREIGN KEY (id_aluno) REFERENCES aluno(id)
);
