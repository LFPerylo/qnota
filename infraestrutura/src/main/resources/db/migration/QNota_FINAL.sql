
CREATE DATABASE IF NOT EXISTS QNota;
USE QNota;

CREATE TABLE professor (
    id_professor INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf CHAR(11) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE turma (
    id_turma INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    ano_letivo INT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    id_professor INT NOT NULL,
    FOREIGN KEY (id_professor) REFERENCES professor(id_professor)
);

CREATE TABLE aluno (
    id_aluno INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    data_nascimento DATE NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    id_turma INT NOT NULL,
    FOREIGN KEY (id_turma) REFERENCES turma(id_turma)
);

CREATE TABLE responsavel (
    id_responsavel INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf CHAR(11) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE aluno_responsavel (
    id_aluno INT,
    id_responsavel INT,
    PRIMARY KEY (id_aluno, id_responsavel),
    FOREIGN KEY (id_aluno) REFERENCES aluno(id_aluno),
    FOREIGN KEY (id_responsavel) REFERENCES responsavel(id_responsavel)
);

CREATE TABLE disciplina (
    id_disciplina INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE simulado (
    id_simulado INT AUTO_INCREMENT PRIMARY KEY,
    data_aplicacao DATE NOT NULL,
    status ENUM('RASCUNHO', 'EM_EDICAO', 'FINALIZADO') NOT NULL DEFAULT 'RASCUNHO',
    id_turma INT NOT NULL,
    FOREIGN KEY (id_turma) REFERENCES turma(id_turma)
);

CREATE TABLE simulado_disciplina (
    id_simulado INT,
    id_disciplina INT,
    peso DECIMAL(4,2) NOT NULL,
    PRIMARY KEY (id_simulado, id_disciplina),
    FOREIGN KEY (id_simulado) REFERENCES simulado(id_simulado),
    FOREIGN KEY (id_disciplina) REFERENCES disciplina(id_disciplina)
);

CREATE TABLE nota_aluno_disciplina (
    id_nota INT AUTO_INCREMENT PRIMARY KEY,
    valor DECIMAL(4,2) NOT NULL,
    id_aluno INT NOT NULL,
    id_simulado INT NOT NULL,
    id_disciplina INT NOT NULL,
    FOREIGN KEY (id_aluno) REFERENCES aluno(id_aluno),
    FOREIGN KEY (id_simulado) REFERENCES simulado(id_simulado),
    FOREIGN KEY (id_disciplina) REFERENCES disciplina(id_disciplina),
    UNIQUE (id_aluno, id_simulado, id_disciplina)
);

CREATE TABLE justificativa (
    id_justificativa INT AUTO_INCREMENT PRIMARY KEY,
    texto TEXT NOT NULL,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_aluno INT NOT NULL,
    id_simulado INT NOT NULL,
    id_professor INT NOT NULL,
    FOREIGN KEY (id_aluno) REFERENCES aluno(id_aluno),
    FOREIGN KEY (id_simulado) REFERENCES simulado(id_simulado),
    FOREIGN KEY (id_professor) REFERENCES professor(id_professor)
);
