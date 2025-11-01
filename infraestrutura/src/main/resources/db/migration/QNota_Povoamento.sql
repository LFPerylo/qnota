USE qnota;

-- Áreas
INSERT INTO areas_conhecimento (nome) VALUES
  ('Matemática'), ('Português') ON DUPLICATE KEY UPDATE nome=VALUES(nome);

-- Professores
INSERT INTO professores (nome, cpf, enderecoEletronico, especialidades) VALUES
  ('Ana Souza','111.111.111-11','ana@escola.com','["Matemática"]'),
  ('Bruno Lima','222.222.222-22','bruno@escola.com','["Português"]')
ON DUPLICATE KEY UPDATE enderecoEletronico=VALUES(enderecoEletronico);

-- Disciplinas
INSERT INTO disciplinas (nome, versao, idVersaoOrigem, ativo, area_id) VALUES
  ('Matemática',1,NULL,1,(SELECT id FROM areas_conhecimento WHERE nome='Matemática')),
  ('Português',1,NULL,1,(SELECT id FROM areas_conhecimento WHERE nome='Português'))
ON DUPLICATE KEY UPDATE ativo=VALUES(ativo);

-- Turma
INSERT INTO turmas (nome, anoLetivo, ativo, professor_id) VALUES
  ('6º Ano A', 2025, 1, (SELECT id FROM professores WHERE nome='Ana Souza'))
ON DUPLICATE KEY UPDATE ativo=VALUES(ativo);

-- Responsáveis
INSERT INTO responsaveis (nome, cpf, enderecoEletronico, status) VALUES
  ('Maria Silva','333.333.333-33','maria@gmail.com','ATIVO'),
  ('João Silva','444.444.444-44','joao@gmail.com','ATIVO')
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- Alunos
INSERT INTO alunos (nome, dataNascimento, ativo, turma_id) VALUES
  ('Pedro Silva', '2013-05-20', 1, (SELECT id FROM turmas WHERE nome='6º Ano A'))
;

-- Vínculos aluno-responsável (um principal por aluno, garantido pelo índice único)
INSERT INTO aluno_responsaveis (responsavel_id, principal, aluno_id) VALUES
  ((SELECT id FROM responsaveis WHERE cpf='333.333.333-33'), 1, (SELECT id FROM alunos WHERE nome='Pedro Silva')),
  ((SELECT id FROM responsaveis WHERE cpf='444.444.444-44'), 0, (SELECT id FROM alunos WHERE nome='Pedro Silva'))
ON DUPLICATE KEY UPDATE principal=VALUES(principal);

-- Simulado
INSERT INTO simulados (dataAplicacao, status, turma_id) VALUES
  ('2025-06-10','EM_EDICAO',(SELECT id FROM turmas WHERE nome='6º Ano A'));

-- Disciplinas do simulado (pesos somando 10; regra validada na aplicação)
INSERT INTO simulado_disciplinas (simulado_id, disciplina_id, peso)
SELECT s.id, d.id, p.peso FROM
  (SELECT (SELECT id FROM simulados ORDER BY id DESC LIMIT 1) AS id) s
JOIN (
  SELECT (SELECT id FROM disciplinas WHERE nome='Matemática') AS id, 6.0 AS peso
  UNION ALL
  SELECT (SELECT id FROM disciplinas WHERE nome='Português')  AS id, 4.0
) d ON 1=1
JOIN (SELECT 1 AS peso) p ON 1=1
ON DUPLICATE KEY UPDATE peso=VALUES(peso);

-- Notas do aluno (exemplo)
INSERT INTO notas_do_aluno (aluno_id, simulado_id, disciplina_id, valor)
VALUES
  ((SELECT id FROM alunos WHERE nome='Pedro Silva'),
   (SELECT id FROM simulados ORDER BY id DESC LIMIT 1),
   (SELECT id FROM disciplinas WHERE nome='Matemática'),
   8.5),
  ((SELECT id FROM alunos WHERE nome='Pedro Silva'),
   (SELECT id FROM simulados ORDER BY id DESC LIMIT 1),
   (SELECT id FROM disciplinas WHERE nome='Português'),
   7.0)
ON DUPLICATE KEY UPDATE valor=VALUES(valor);

-- Justificativa (exemplo simples; gere UUID real na app)
INSERT INTO justificativas (id, aluno_id, simulado_id, disciplina_id, professor_id, notaAnterior, notaCorrigida, texto)
VALUES
  ('JUS-0001',
   (SELECT id FROM alunos WHERE nome='Pedro Silva'),
   (SELECT id FROM simulados ORDER BY id DESC LIMIT 1),
   (SELECT id FROM disciplinas WHERE nome='Português'),
   (SELECT id FROM professores WHERE nome='Bruno Lima'),
   7.0, 7.5,
   'Erro de digitação corrigido pelo professor.')
ON DUPLICATE KEY UPDATE texto=VALUES(texto);

-- Ranking (mock)
INSERT INTO rankings (congelado, simulado_id)
VALUES (0, (SELECT id FROM simulados ORDER BY id DESC LIMIT 1))
ON DUPLICATE KEY UPDATE congelado=VALUES(congelado);

INSERT INTO ranking_linhas (ranking_id, aluno_id, media, posicao)
VALUES (
  (SELECT id FROM rankings ORDER BY id DESC LIMIT 1),
  (SELECT id FROM alunos WHERE nome='Pedro Silva'),
  7.9, 1
)
ON DUPLICATE KEY UPDATE media=VALUES(media), posicao=VALUES(posicao);
