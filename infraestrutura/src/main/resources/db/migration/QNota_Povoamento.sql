-- Use o schema
SET search_path TO qnota;

-- AREAS
INSERT INTO areas_conhecimento (id, nome) VALUES
  (1,'Matemática'),
  (2,'Português'),
  (3,'Ciências')
ON CONFLICT (id) DO NOTHING;

-- DISCIPLINAS
INSERT INTO disciplinas (id, nome, versao, idVersaoOrigem, ativo, area_id) VALUES
  (1,'Matemática',1,NULL,TRUE,1),
  (2,'Português',1,NULL,TRUE,2),
  (3,'Ciências',1,NULL,TRUE,3)
ON CONFLICT (id) DO NOTHING;

-- PROFESSORES
INSERT INTO professores (id, nome, cpf, enderecoEletronico, especialidades) VALUES
  (1,'Ana Paula','111.111.111-11','ana@escola.com', '["Matemática","Português"]'::jsonb),
  (2,'Carlos Silva','222.222.222-22','carlos@escola.com', '["Ciências"]'::jsonb)
ON CONFLICT (id) DO NOTHING;

-- TURMAS
INSERT INTO turmas (id, nome, anoLetivo, ativo, professor_id) VALUES
  (1,'6ºA',2025,TRUE,1),
  (2,'6ºB',2025,TRUE,2)
ON CONFLICT (id) DO NOTHING;

-- COORDENADORES
INSERT INTO coordenadores (id, nome, enderecoEletronico, senhaHash, ativo) VALUES
  (1,'Coordenadora Júlia','julia@escola.com','$2a$10$hash-exemplo',TRUE)
ON CONFLICT (id) DO NOTHING;

-- RESPONSAVEIS
INSERT INTO responsaveis (id, nome, cpf, enderecoEletronico, status) VALUES
  (1,'Maria Souza','333.333.333-33','maria@email.com','ATIVO'),
  (2,'João Souza','444.444.444-44','joao@email.com','ATIVO'),
  (3,'Paula Dias','555.555.555-55','paula@email.com','INADIMPLENTE')
ON CONFLICT (id) DO NOTHING;

-- ALUNOS
INSERT INTO alunos (id, nome, dataNascimento, ativo, turma_id) VALUES
  (1,'Lucas Souza','2013-04-10',TRUE,1),
  (2,'Bianca Dias','2013-07-22',TRUE,1)
ON CONFLICT (id) DO NOTHING;

-- VÍNCULOS (um principal por aluno)
INSERT INTO aluno_responsaveis (responsavel_id, aluno_id, principal) VALUES
  (1,1,TRUE),   -- Maria é principal do Lucas
  (2,1,FALSE),  -- João também vinculado ao Lucas
  (3,2,TRUE)    -- Paula é principal da Bianca
ON CONFLICT DO NOTHING;

-- SIMULADOS
INSERT INTO simulados (id, dataAplicacao, status, turma_id) VALUES
  (1,'2025-05-15','EM_EDICAO',1),
  (2,'2025-06-20','FINALIZADO',1)
ON CONFLICT (id) DO NOTHING;

-- DISCIPLINAS DO SIMULADO (pesos somam 10)
INSERT INTO simulado_disciplinas (simulado_id, disciplina_id, peso) VALUES
  (1,1,6.0),(1,2,4.0),
  (2,1,5.0),(2,2,5.0)
ON CONFLICT DO NOTHING;

-- NOTAS (para o simulado 2, que está FINALIZADO)
INSERT INTO notas_do_aluno (aluno_id, simulado_id, disciplina_id, valor, dataLancamento) VALUES
  (1,2,1,8.5, NOW()),
  (1,2,2,7.0, NOW()),
  (2,2,1,9.0, NOW()),
  (2,2,2,8.0, NOW())
ON CONFLICT DO NOTHING;

-- JUSTIFICATIVAS (exemplo)
INSERT INTO justificativas
(id, aluno_id, simulado_id, disciplina_id, professor_id, notaAnterior, notaCorrigida, texto, dataHora)
VALUES
('J-0001', 1, 2, 2, 1, 6.5, 7.0, 'Correção de erro de digitação na planilha.', NOW())
ON CONFLICT DO NOTHING;

-- RANKING (para o simulado 2)
INSERT INTO rankings (id, congelado, simulado_id) VALUES
  (1, TRUE, 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO ranking_linhas (ranking_id, aluno_id, media, posicao) VALUES
  (1,2,8.50,1),
  (1,1,7.70,2)
ON CONFLICT DO NOTHING;

-- Ajusta sequences para não colidir em futuros INSERTs sem id
SELECT setval(pg_get_serial_sequence('qnota.coordenadores','id'), COALESCE((SELECT MAX(id) FROM qnota.coordenadores),1), TRUE);
SELECT setval(pg_get_serial_sequence('qnota.responsaveis','id'),  COALESCE((SELECT MAX(id) FROM qnota.responsaveis),1), TRUE);
SELECT setval(pg_get_serial_sequence('qnota.professores','id'),  COALESCE((SELECT MAX(id) FROM qnota.professores),1), TRUE);
SELECT setval(pg_get_serial_sequence('qnota.areas_conhecimento','id'), COALESCE((SELECT MAX(id) FROM qnota.areas_conhecimento),1), TRUE);
SELECT setval(pg_get_serial_sequence('qnota.disciplinas','id'),   COALESCE((SELECT MAX(id) FROM qnota.disciplinas),1), TRUE);
SELECT setval(pg_get_serial_sequence('qnota.turmas','id'),       COALESCE((SELECT MAX(id) FROM qnota.turmas),1), TRUE);
SELECT setval(pg_get_serial_sequence('qnota.alunos','id'),       COALESCE((SELECT MAX(id) FROM qnota.alunos),1), TRUE);
SELECT setval(pg_get_serial_sequence('qnota.simulados','id'),    COALESCE((SELECT MAX(id) FROM qnota.simulados),1), TRUE);
SELECT setval(pg_get_serial_sequence('qnota.rankings','id'),     COALESCE((SELECT MAX(id) FROM qnota.rankings),1), TRUE);
