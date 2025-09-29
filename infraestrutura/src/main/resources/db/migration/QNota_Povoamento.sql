
USE qnota;

-- Professores
INSERT INTO professor (nome, cpf, email) VALUES
('Ana Lúcia Costa', '123.456.789-00', 'ana.costa@escola.com'),
('Carlos Eduardo Lima', '987.654.321-00', 'carlos.lima@escola.com');

-- Responsáveis
INSERT INTO responsavel (nome, cpf, email) VALUES
('Fernanda Silva', '111.222.333-44', 'fernanda.silva@email.com'),
('João Mendes', '555.666.777-88', 'joao.mendes@email.com');

-- Turmas
INSERT INTO turma (nome, ano_letivo, ativo, id_professor) VALUES
('Turma A', 2025, TRUE, 1),
('Turma B', 2025, TRUE, 2);

-- Alunos
INSERT INTO aluno (nome, data_nascimento, ativo, id_turma) VALUES
('Lucas Pereira', '2012-03-15', TRUE, 1),
('Mariana Torres', '2011-10-22', TRUE, 1),
('Rafael Souza', '2012-07-09', TRUE, 2);

-- Associação aluno-responsável
INSERT INTO aluno_responsavel (id_aluno, id_responsavel) VALUES
(1, 1),
(2, 1),
(3, 2);

-- Disciplinas
INSERT INTO disciplina (nome) VALUES
('Matemática'),
('Português'),
('Ciências');

-- Simulados
INSERT INTO simulado (data_aplicacao, status, id_turma) VALUES
('2025-09-10', 'FINALIZADO', 1),
('2025-09-20', 'EM_EDICAO', 2);

-- SimuladoDisciplina
INSERT INTO simulado_disciplina (id_simulado, id_disciplina, peso) VALUES
(1, 1, 5.00),
(1, 2, 5.00),
(2, 1, 6.00),
(2, 3, 4.00);

-- Notas
INSERT INTO nota_aluno_disciplina (id_aluno, id_simulado, id_disciplina, valor) VALUES
(1, 1, 1, 8.5),
(1, 1, 2, 7.0),
(2, 1, 1, 9.0),
(2, 1, 2, 6.5);

-- Justificativa (retificação)
INSERT INTO justificativa (texto, data_hora, id_professor, id_nota, nota_anterior, nota_corrigida) VALUES
('Erro de digitação corrigido após revisão.', NOW(), 1, 1, 8.5, 9.0);
