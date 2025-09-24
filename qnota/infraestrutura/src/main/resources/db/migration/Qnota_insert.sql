-- ===== PROFESSOR =====
INSERT INTO Professor (nome, cpf, email) VALUES
('Carlos Silva', '123.456.789-00', 'carlos.silva@escola.com'),
('Maria Souza', '987.654.321-00', 'maria.souza@escola.com');

-- ===== TURMA =====
INSERT INTO Turma (nome, ano_letivo, id_professor) VALUES
('3º Ano A', 2025, 1),
('4º Ano B', 2025, 2);

-- ===== ALUNO =====
INSERT INTO Aluno (nome, data_nascimento, ativo, id_turma) VALUES
('João Pedro', '2014-05-10', TRUE, 1),
('Ana Clara', '2013-09-22', TRUE, 1),
('Lucas Lima', '2012-01-15', TRUE, 2),
('Mariana Alves', '2011-12-30', TRUE, 2);

-- ===== RESPONSAVEL =====
INSERT INTO Responsavel (nome, cpf, email) VALUES
('Paulo Pedro', '111.222.333-44', 'paulo.pedro@email.com'),
('Fernanda Clara', '555.666.777-88', 'fernanda.clara@email.com'),
('Roberto Lima', '999.888.777-66', 'roberto.lima@email.com'),
('Juliana Alves', '444.333.222-11', 'juliana.alves@email.com');

-- ===== ALUNO RESPONSAVEL =====
INSERT INTO AlunoResponsavel (id_aluno, id_responsavel) VALUES
(1, 1), -- João -> Paulo
(2, 2), -- Ana -> Fernanda
(3, 3), -- Lucas -> Roberto
(4, 4); -- Mariana -> Juliana

-- ===== DISCIPLINA =====
INSERT INTO Disciplina (nome) VALUES
('Português'),
('Matemática');

-- ===== SIMULADO =====
INSERT INTO Simulado (data_aplicacao, status, id_turma) VALUES
('2025-03-01', 'FINALIZADO', 1),
('2025-03-15', 'FINALIZADO', 2);

-- ===== SIMULADO DISCIPLINA =====
INSERT INTO SimuladoDisciplina (id_simulado, id_disciplina, peso) VALUES
(1, 1, 5.00), -- Simulado 1 - Português
(1, 2, 5.00), -- Simulado 1 - Matemática
(2, 1, 4.00), -- Simulado 2 - Português
(2, 2, 6.00); -- Simulado 2 - Matemática

-- ===== NOTA =====
INSERT INTO Nota (valor, id_aluno, id_simulado, id_disciplina) VALUES
(8.50, 1, 1, 1),
(7.00, 1, 1, 2),
(9.00, 2, 1, 1),
(6.50, 2, 1, 2),
(7.50, 3, 2, 1),
(8.00, 3, 2, 2),
(9.50, 4, 2, 1),
(7.00, 4, 2, 2);

-- ===== HISTORICO JUSTIFICATIVA =====
INSERT INTO HistoricoJustificativa (id_aluno, id_simulado, id_professor, texto) VALUES
(1, 1, 1, 'Aluno apresentou bom desempenho, precisa melhorar em Matemática.'),
(2, 1, 1, 'Aluna teve dificuldade em Matemática, sugerido reforço.'),
(3, 2, 2, 'Aluno regular, desempenho satisfatório.'),
(4, 2, 2, 'Excelente desempenho, destaque na turma.');
