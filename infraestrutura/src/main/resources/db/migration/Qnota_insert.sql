
USE QNota;

-- Professores
INSERT INTO professor (nome, cpf, email) VALUES
('Maria Ferreira', '12345678901', 'maria@escola.com'),
('João Almeida', '23456789012', 'joao@escola.com');

-- Turmas
INSERT INTO turma (nome, ano_letivo, ativo, id_professor) VALUES
('8º Ano A', 2025, TRUE, 1),
('9º Ano B', 2025, TRUE, 2);

-- Alunos
INSERT INTO aluno (nome, data_nascimento, ativo, id_turma) VALUES
('Ana Clara', '2012-05-10', TRUE, 1),
('Bruno Silva', '2011-09-22', TRUE, 1),
('Carlos Mendes', '2010-03-15', FALSE, 2);

-- Responsáveis
INSERT INTO responsavel (nome, cpf, email) VALUES
('Luciana Clara', '34567890123', 'luciana@exemplo.com'),
('Renato Silva', '45678901234', 'renato@exemplo.com');

-- Vínculo aluno-responsável
INSERT INTO aluno_responsavel (id_aluno, id_responsavel) VALUES
(1, 1),
(2, 2);

-- Disciplinas
INSERT INTO disciplina (nome) VALUES
('Matemática'),
('Português'),
('Ciências');

-- Simulados
INSERT INTO simulado (data_aplicacao, status, id_turma) VALUES
('2025-05-15', 'FINALIZADO', 1),
('2025-06-20', 'EM_EDICAO', 1);

-- Simulado-Disciplina
INSERT INTO simulado_disciplina (id_simulado, id_disciplina, peso) VALUES
(1, 1, 5.0),
(1, 2, 5.0),
(2, 1, 6.0),
(2, 2, 4.0);

-- Notas
INSERT INTO nota_aluno_disciplina (valor, id_aluno, id_simulado, id_disciplina) VALUES
(8.5, 1, 1, 1),
(7.0, 1, 1, 2),
(9.0, 2, 1, 1),
(6.5, 2, 1, 2);

-- Justificativas
INSERT INTO justificativa (texto, id_aluno, id_simulado, id_professor) VALUES
('Erro na digitação da nota de matemática.', 1, 1, 1),
('Nota corrigida após revisão de prova.', 2, 1, 1);
