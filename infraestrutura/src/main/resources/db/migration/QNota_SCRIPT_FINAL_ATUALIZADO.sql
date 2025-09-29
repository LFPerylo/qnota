
DROP DATABASE IF EXISTS qnota;
CREATE DATABASE qnota;
USE qnota;

CREATE TABLE professor (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL
);

CREATE TABLE responsavel (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL
);

CREATE TABLE turma (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    ano_letivo INT NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    id_professor INT NOT NULL,
    FOREIGN KEY (id_professor) REFERENCES professor(id),
    UNIQUE (nome, ano_letivo)
);

CREATE TABLE aluno (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    data_nascimento DATE NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    id_turma INT NOT NULL,
    FOREIGN KEY (id_turma) REFERENCES turma(id),
    UNIQUE (nome, data_nascimento, id_turma)
);

CREATE TABLE aluno_responsavel (
    id_aluno INT,
    id_responsavel INT,
    PRIMARY KEY (id_aluno, id_responsavel),
    FOREIGN KEY (id_aluno) REFERENCES aluno(id),
    FOREIGN KEY (id_responsavel) REFERENCES responsavel(id)
);

CREATE TABLE disciplina (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    CHECK (nome NOT IN ('disciplina', 'materia', 'matéria', 'nome', 'teste', 'teste1', 'exemplo', 'exemplo1'))
);

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

CREATE TABLE nota_aluno_disciplina (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_aluno INT NOT NULL,
    id_simulado INT NOT NULL,
    id_disciplina INT NOT NULL,
    valor DECIMAL(4,2) NOT NULL,
    FOREIGN KEY (id_aluno) REFERENCES aluno(id),
    FOREIGN KEY (id_simulado) REFERENCES simulado(id),
    FOREIGN KEY (id_disciplina) REFERENCES disciplina(id),
    UNIQUE (id_aluno, id_simulado, id_disciplina)
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
