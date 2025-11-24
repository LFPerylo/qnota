Feature: Gerenciar Notas
  Como coordenador
  Quero gerenciar notas de simulados
  Para controlar o desempenho dos alunos

  Background:
    Given existe um "aluno" "A1" ativo na turma "7A"
    And existe um "simulado" "S1" em edição para a turma "7A"
    And existe uma "disciplina" "Matemática" na área "Exatas"

  Scenario: Lançar nota válida em simulado em edição (sucesso - RN-32)
    When um coordenador lança nota 8.5 para o "aluno" "A1" no "simulado" "S1" na "disciplina" "Matemática"
    Then o sistema confirma o lançamento da nota
    And o ranking é recalculado automaticamente

  Scenario: Bloquear lançamento em simulado finalizado (falha - RN-32)
    Given o "simulado" "S1" "está" finalizado
    When um coordenador tenta lançar nota 7.0 para o "aluno" "A1" no "simulado" "S1" na "disciplina" "Matemática"
    Then o sistema rejeita o lançamento em notas
    And o sistema informa em notas que "RN-32"

  Scenario: Bloquear lançamento de nota duplicada (falha - RN-33)
    Given já existe nota para o "aluno" "A1" no "simulado" "S1" na "disciplina" "Matemática"
    When um coordenador tenta lançar nota 9.0 para o "aluno" "A1" no "simulado" "S1" na "disciplina" "Matemática"
    Then o sistema rejeita o lançamento em notas
    And o sistema informa em notas que "RN-33"

  Scenario: Bloquear lançamento para aluno inativo (falha - RN-31/32/33)
    Given o "aluno" "A1" "está" inativo
    When um coordenador tenta lançar nota 6.5 para o "aluno" "A1" no "simulado" "S1" na "disciplina" "Matemática"
    Then o sistema rejeita o lançamento em notas
    And o sistema informa em notas que "RN-31/32/33"

  Scenario: Bloquear lançamento em turma inativa (falha - RN-94)
    Given a "turma" "7A" "está" inativa
    When um coordenador tenta lançar nota 7.5 para o "aluno" "A1" no "simulado" "S1" na "disciplina" "Matemática"
    Then o sistema rejeita o lançamento em notas
    And o sistema informa em notas que "RN-94"

  Scenario: Bloquear lançamento com nota fora da faixa (falha - RN-31)
    When um coordenador tenta lançar nota 11.0 para o "aluno" "A1" no "simulado" "S1" na "disciplina" "Matemática"
    Then o sistema rejeita o lançamento em notas
    And o sistema informa em notas que "RN-31"

  Scenario: Bloquear lançamento com nota negativa (falha - RN-31)
    When um coordenador tenta lançar nota -1.0 para o "aluno" "A1" no "simulado" "S1" na "disciplina" "Matemática"
    Then o sistema rejeita o lançamento em notas
    And o sistema informa em notas que "RN-31"

  Scenario: Bloquear lançamento com aluno inexistente (falha - RN-34)
    When um coordenador tenta lançar nota 8.0 para aluno inexistente no "simulado" "S1" na "disciplina" "Matemática"
    Then o sistema rejeita o lançamento em notas
    And o sistema informa em notas que "aluno não encontrado"

  Scenario: Bloquear lançamento com simulado inexistente (falha - RN-34)
    When um coordenador tenta lançar nota 8.0 para o "aluno" "A1" em simulado inexistente na "disciplina" "Matemática"
    Then o sistema rejeita o lançamento em notas
    And o sistema informa em notas que "simulado não encontrado"

  Scenario: Bloquear lançamento com disciplina inexistente (falha - RN-34)
    When um coordenador tenta lançar nota 8.0 para o "aluno" "A1" no "simulado" "S1" em disciplina inexistente
    Then o sistema rejeita o lançamento em notas
    And o sistema informa em notas que "disciplina não encontrada"

  Scenario: Retificar nota com justificativa válida (sucesso - RN-37/38/39)
    Given já existe nota 6.0 para o "aluno" "A1" no "simulado" "S1" na "disciplina" "Matemática"
    And existe um "professor" "P1" responsável pela turma "7A"
    When um coordenador retifica a nota para 8.5 com justificativa "Corrigido após revisão do gabarito: questão reavaliada."
    Then o sistema confirma a retificação da nota
    And uma nova versão da nota é criada
    And a justificativa é registrada no histórico

  Scenario: Bloquear retificação sem justificativa suficiente (falha - RN-37)
    Given já existe nota 7.0 para o "aluno" "A1" no "simulado" "S1" na "disciplina" "Matemática"
    And existe um "professor" "P1" responsável pela turma "7A"
    When um coordenador tenta retificar a nota para 7.5 com justificativa "muito curto"
    Then o sistema rejeita a retificação em notas
    And o sistema informa em notas que "RN-37"

  Scenario: Retificar em simulado finalizado (sucesso - RN-39 atualizado)
    Given já existe nota 5.0 para o "aluno" "A1" no "simulado" "S1" na "disciplina" "Matemática"
    And o "simulado" "S1" "está" finalizado
    And existe um "professor" "P1" responsável pela turma "7A"
    When um coordenador retifica a nota para 6.0 com justificativa "Após revisão detalhada, pontos foram corretamente ajustados."
    Then o sistema confirma a retificação da nota
    And uma nova versão da nota é criada
    And a justificativa é registrada no histórico
