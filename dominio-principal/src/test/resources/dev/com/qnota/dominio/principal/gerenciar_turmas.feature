Feature: Gerenciar turmas

  # ========= H2.1 CADASTRAR TURMA =========

  Scenario: Cadastrar turma válida (sucesso - RN-06)
    Given uma "turma" com nome "7A" e ano letivo "2024" "não está" registrada
    When um coordenador cadastra a "turma" com professor válido
    Then o sistema confirma o cadastro da "turma"

  Scenario: Impedir cadastro de turma com nome duplicado no mesmo ano (falha - RN-06)
    Given uma "turma" com nome "7A" e ano letivo "2024" "já está" registrada
    When um coordenador tenta cadastrar uma "turma" com nome "7A" e ano letivo "2024"
    Then o sistema rejeita o cadastro em turmas
    And o sistema informa em turmas que "Nome único no ano letivo"

  Scenario: Permitir cadastro de turma com mesmo nome em anos diferentes (sucesso - RN-06)
    Given uma "turma" com nome "7A" e ano letivo "2024" "já está" registrada
    When um coordenador cadastra uma "turma" com nome "7A" e ano letivo "2025"
    Then o sistema confirma o cadastro da "turma"

  # ========= H2.2 EDITAR TURMA =========

  Scenario: Renomear turma sem simulados finalizados (sucesso)
    Given uma "turma" "está" registrada e "não possui" simulados finalizados
    When um coordenador renomeia a "turma" para "7B"
    Then o sistema confirma a alteração da "turma"

  Scenario: Trocar professor de turma sem simulados finalizados (sucesso - RN-10)
    Given uma "turma" "está" registrada e "não possui" simulados finalizados
    And existe um professor "P2" válido
    When um coordenador troca o professor da "turma" para "P2"
    Then o sistema confirma a alteração da "turma"

  Scenario: Bloquear troca de professor com simulados finalizados (falha - RN-10)
    Given uma "turma" "está" registrada e "possui" simulados finalizados
    And existe um professor "P2" válido
    When um coordenador tenta trocar o professor da "turma" para "P2"
    Then o sistema rejeita a alteração em turmas
    And o sistema informa em turmas que "Não é permitido alterar professor com simulados finalizados"

  # ========= H2.3 INATIVAR TURMA =========

  Scenario: Inativar turma sem simulados em edição (sucesso - RN-95)
    Given uma "turma" "está" registrada e "não possui" simulados em edição
    When um coordenador inativa a "turma"
    Then o sistema confirma a inativação da "turma"

  Scenario: Bloquear inativação com simulados em edição (falha - RN-95)
    Given uma "turma" "está" registrada e "possui" simulados em edição
    When um coordenador tenta inativar a "turma"
    Then o sistema rejeita a inativação em turmas
    And o sistema informa em turmas que "Finalize simulados em edição antes de inativar"

  # ========= H2.4 EXCLUIR TURMA =========

  Scenario: Excluir turma sem vínculos (sucesso - RN-08)
    Given uma "turma" "está" registrada e "não possui" vínculos
    When um coordenador exclui a "turma"
    Then o sistema confirma a exclusão da "turma"

  Scenario: Bloquear exclusão com alunos ativos (falha - RN-08)
    Given uma "turma" "está" registrada e "possui" alunos ativos
    When um coordenador tenta excluir a "turma"
    Then o sistema rejeita a exclusão em turmas
    And o sistema informa em turmas que "Não é possível excluir turma com vínculos"

  Scenario: Bloquear exclusão com simulados (falha - RN-08)
    Given uma "turma" "está" registrada e "possui" simulados
    When um coordenador tenta excluir a "turma"
    Then o sistema rejeita a exclusão em turmas
    And o sistema informa em turmas que "Não é possível excluir turma com vínculos"

  # ========= VALIDAÇÕES BÁSICAS =========

  Scenario: Bloquear cadastro sem nome da turma (falha - NOT NULL)
    Given uma "turma" "não está" registrada e "sem nome"
    When um coordenador tenta cadastrar a "turma" com professor válido
    Then o sistema rejeita o cadastro em turmas
    And o sistema informa em turmas que "'nome' não pode ser vazio"

  Scenario: Bloquear cadastro com nome da turma em branco (falha - NOT BLANK)
    Given uma "turma" "não está" registrada e "nome em branco"
    When um coordenador tenta cadastrar a "turma" com professor válido
    Then o sistema rejeita o cadastro em turmas
    And o sistema informa em turmas que "'nome' não pode ser vazio"

  Scenario: Bloquear cadastro com ano letivo inválido (falha - VALIDAÇÃO)
    Given uma "turma" "não está" registrada e "ano letivo inválido"
    When um coordenador tenta cadastrar a "turma" com professor válido
    Then o sistema rejeita o cadastro em turmas
    And o sistema informa em turmas que "anoLetivo deve ser positivo"

  Scenario: Bloquear cadastro sem professor (falha - NOT NULL)
    Given uma "turma" "não está" registrada e "sem professor"
    When um coordenador tenta cadastrar a "turma" sem professor
    Then o sistema rejeita o cadastro em turmas
    And o sistema informa em turmas que "professor não pode ser nulo"


