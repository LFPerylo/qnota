
-- Versão corrigida do modelo lógico do QNota

CREATE TABLE Professor (
    id_professor INTEGER PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE Turma (
    id_turma INTEGER PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    ano_letivo INT NOT NULL,
    id_professor INTEGER NOT NULL,
    FOREIGN KEY (id_professor) REFERENCES Professor(id_professor)
);

CREATE TABLE Aluno (
    id_aluno INTEGER PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    data_nascimento DATE NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    id_turma INTEGER NOT NULL,
    FOREIGN KEY (id_turma) REFERENCES Turma(id_turma)
);

CREATE TABLE Responsavel (
    id_responsavel INTEGER PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    email VARCHAR(100)
);

CREATE TABLE Disciplina (
    id_disciplina INTEGER PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE Simulado (
    id_simulado INTEGER PRIMARY KEY AUTO_INCREMENT,
    data_aplicacao DATE NOT NULL,
    status ENUM('RASCUNHO', 'EM_EDICAO', 'FINALIZADO') NOT NULL,
    id_turma INTEGER NOT NULL,
    FOREIGN KEY (id_turma) REFERENCES Turma(id_turma)
);

CREATE TABLE SimuladoDisciplina (
    id_simulado INTEGER NOT NULL,
    id_disciplina INTEGER NOT NULL,
    peso DECIMAL(4,2) NOT NULL CHECK (peso > 0),
    PRIMARY KEY (id_simulado, id_disciplina),
    FOREIGN KEY (id_simulado) REFERENCES Simulado(id_simulado),
    FOREIGN KEY (id_disciplina) REFERENCES Disciplina(id_disciplina)
);

CREATE TABLE Nota (
    id_nota INTEGER PRIMARY KEY AUTO_INCREMENT,
    valor DECIMAL(4,2) NOT NULL CHECK (valor >= 0 AND valor <= 10),
    id_aluno INTEGER NOT NULL,
    id_simulado INTEGER NOT NULL,
    id_disciplina INTEGER NOT NULL,
    FOREIGN KEY (id_aluno) REFERENCES Aluno(id_aluno),
    FOREIGN KEY (id_simulado, id_disciplina) REFERENCES SimuladoDisciplina(id_simulado, id_disciplina)
);

CREATE TABLE AlunoResponsavel (
    id_aluno INTEGER NOT NULL,
    id_responsavel INTEGER NOT NULL,
    PRIMARY KEY (id_aluno, id_responsavel),
    FOREIGN KEY (id_aluno) REFERENCES Aluno(id_aluno),
    FOREIGN KEY (id_responsavel) REFERENCES Responsavel(id_responsavel)
);

CREATE TABLE HistoricoJustificativa (
    id_justificativa INTEGER PRIMARY KEY AUTO_INCREMENT,
    id_aluno INTEGER NOT NULL,
    id_simulado INTEGER NOT NULL,
    id_professor INTEGER NOT NULL,
    texto TEXT NOT NULL,
    data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_aluno) REFERENCES Aluno(id_aluno),
    FOREIGN KEY (id_simulado) REFERENCES Simulado(id_simulado),
    FOREIGN KEY (id_professor) REFERENCES Professor(id_professor)
);
