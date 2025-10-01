USE qnota;

-- ==============================
-- PROFESSORES
-- ==============================
INSERT INTO professor (nome, cpf, email, especialidade) VALUES
('Carlos Silva', '111.111.111-11', 'carlos.silva@escola.com', 'Matemática'),
('Fernanda Souza', '222.222.222-22', 'fernanda.souza@escola.com', 'Linguagens'),
('Marcos Andrade', '333.333.333-33', 'marcos.andrade@escola.com', 'Ciências Humanas');

-- ==============================
-- RESPONSAVEIS
-- ==============================
INSERT INTO responsavel (nome, cpf, email, inadimplente) VALUES
('João Pereira', '444.444.444-44', 'joao.pereira@email.com', FALSE),
('Maria Oliveira', '555.555.555-55', 'maria.oliveira@email.com', FALSE),
('Ana Costa', '666.666.666-66', 'ana.costa@email.com', TRUE); -- inadimplente

-- ==============================
-- TURMAS
-- ==============================
INSERT INTO turma (nome, ano_letivo, ativo, id_professor) VALUES
('Turma A', 2025, TRUE, 1),
('Turma B', 2025, TRUE, 2);

-- ==============================
-- ALUNOS
-- ==============================
INSERT INTO aluno (nome, data_nascimento, ativo, id_turma) VALUES
('Pedro Santos', '2010-05-12', TRUE, 1),
('Lucas Lima', '2010-08-20', TRUE, 1),
('Mariana Alves', '2011-02-10', TRUE, 2);

-- ==============================
-- VÍNCULOS ALUNO-RESPONSAVEL
-- ==============================
INSERT INTO aluno_responsavel (id_aluno, id_responsavel, principal) VALUES
(1, 1, TRUE),  -- Pedro -> João
(1, 2, FALSE), -- Pedro -> Maria
(2, 2, TRUE),  -- Lucas -> Maria
(3, 1, FALSE), -- Mariana -> João
(3, 3, TRUE);  -- Mariana -> Ana (inadimplente, mas já estava vinculada antes)

-- ==============================
-- DISCIPLINAS
-- ==============================
INSERT INTO disciplina (nome, area) VALUES
('Matemática', 'Exatas'),
('Português', 'Linguagens'),
('História', 'Humanas'),
('Geografia', 'Humanas');

-- ==============================
-- SIMULADOS
-- ==============================
INSERT INTO simulado (data_aplicacao, status, id_turma) VALUES
('2025-06-15', 'EM_EDICAO', 1),
('2025-06-20', 'EM_EDICAO', 2);

-- ==============================
-- SIMULADO_DISCIPLINA (pesos somando 10)
-- ==============================
INSERT INTO simulado_disciplina (id_simulado, id_disciplina, peso) VALUES
(1, 1, 6.0), -- Matemática
(1, 2, 4.0), -- Português
(2, 2, 5.0), -- Português
(2, 3, 5.0); -- História

-- ==============================
-- NOTAS DOS ALUNOS
-- ==============================
INSERT INTO nota_aluno_disciplina (id_aluno, id_simulado, id_disciplina, valor) VALUES
(1, 1, 1, 8.50), -- Pedro - Matemática
(1, 1, 2, 7.00), -- Pedro - Português
(2, 1, 1, 6.00), -- Lucas - Matemática
(2, 1, 2, 9.00), -- Lucas - Português
(3, 2, 2, 8.00), -- Mariana - Português
(3, 2, 3, 7.50); -- Mariana - História

-- ==============================
-- JUSTIFICATIVA DE NOTA
-- ==============================
INSERT INTO justificativa (texto, id_professor, id_nota, nota_anterior, nota_corrigida)
VALUES
('Correção por erro de digitação no lançamento da nota.', 1, 3, 5.00, 6.00);
