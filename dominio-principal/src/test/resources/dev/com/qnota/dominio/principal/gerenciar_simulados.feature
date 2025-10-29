Feature: Gerenciar simulados

  # ========= H8.0 CADASTRAR SIMULADO =========

  Scenario: Cadastrar simulado válido em turma ativa (sucesso - RN-52)
    Given uma "turma" "7A" "está" ativa com professor "P1" que possui especialidade "Matemática"
    And existem disciplinas "Matemática" e "Física" na área "Exatas"
    When um coordenador cadastra um "simulado" para a turma "7A" com disciplinas válidas
    Then o sistema confirma o cadastro do "simulado"

  Scenario: Bloquear cadastro em turma inativa (falha - RN-96)
    Given uma "turma" "7A" "está" inativa com professor "P1"
    When um coordenador tenta cadastrar um "simulado" para a turma "7A"
    Then o sistema rejeita o cadastro em simulados
    And o sistema informa em simulados que "RN-96"

  Scenario: Bloquear cadastro com professor sem especialidade compatível (falha - RN-53)
    Given uma "turma" "7A" "está" ativa com professor "P1" que possui especialidade "História"
    And existem disciplinas "Matemática" e "Física" na área "Exatas"
    When um coordenador tenta cadastrar um "simulado" para a turma "7A" com disciplinas "Matemática" e "Física"
    Then o sistema rejeita o cadastro em simulados
    And o sistema informa em simulados que "RN-53"

  Scenario: Bloquear cadastro com mais de 3 simulados em edição (falha - RN-52)
    Given uma "turma" "7A" "está" ativa com professor "P1" que possui especialidade "Matemática"
    And a turma "7A" "possui" 3 simulados em edição
    When um coordenador tenta cadastrar um "simulado" para a turma "7A"
    Then o sistema rejeita o cadastro em simulados
    And o sistema informa em simulados que "RN-52"

  Scenario: Permitir cadastro com exatamente 3 simulados em edição (sucesso - RN-52)
    Given uma "turma" "7A" "está" ativa com professor "P1" que possui especialidade "Matemática"
    And a turma "7A" "possui" 2 simulados em edição
    When um coordenador cadastra um "simulado" para a turma "7A" com disciplinas válidas
    Then o sistema confirma o cadastro do "simulado"

  # ========= H8.1 EDITAR SIMULADO =========

  Scenario: Editar disciplinas de simulado em edição (sucesso - RN-12/13/14B/14C)
    Given um "simulado" "está" em edição para a turma "7A"
    And o "simulado" possui disciplinas "Matemática" e "Física"
    When um coordenador edita as disciplinas do "simulado" para "Matemática" e "Química"
    Then o sistema confirma a alteração do "simulado"
    And o sistema recalcula o ranking

  Scenario: Bloquear edição de simulado finalizado (falha - RN-102)
    Given um "simulado" "está" finalizado para a turma "7A"
    When um coordenador tenta editar as disciplinas do "simulado"
    Then o sistema rejeita a alteração em simulados
    And o sistema informa em simulados que "RN-14C"

  # ========= H8.2 FINALIZAR SIMULADO =========

  Scenario: Finalizar simulado com todas as notas lançadas (sucesso - RN-16)
    Given um "simulado" "está" em edição para a turma "7A"
    And todas as notas do "simulado" "foram" lançadas
    When um coordenador finaliza o "simulado"
    Then o sistema confirma a finalização do "simulado"
    And o sistema congela o ranking

  Scenario: Bloquear finalização com notas pendentes (falha - RN-16)
    Given um "simulado" "está" em edição para a turma "7A"
    And existem notas pendentes no "simulado"
    When um coordenador tenta finalizar o "simulado"
    Then o sistema rejeita a finalização em simulados
    And o sistema informa em simulados que "RN-16"

  # ========= H8.3 EXCLUIR SIMULADO =========

  Scenario: Excluir simulado sem notas lançadas (sucesso - RN-15)
    Given um "simulado" "está" em edição para a turma "7A"
    And o "simulado" "não possui" notas lançadas
    When um coordenador exclui o "simulado"
    Then o sistema confirma a exclusão do "simulado"

  Scenario: Bloquear exclusão com notas lançadas (falha - RN-15)
    Given um "simulado" "está" em edição para a turma "7A"
    And o "simulado" "possui" notas lançadas
    When um coordenador tenta excluir o "simulado"
    Then o sistema rejeita a exclusão em simulados
    And o sistema informa em simulados que "RN-15"

  # ========= VALIDAÇÕES BÁSICAS =========

  Scenario: Bloquear cadastro sem disciplinas (falha - RN-12)
    Given uma "turma" "7A" "está" ativa com professor "P1" que possui especialidade "Matemática"
    When um coordenador tenta cadastrar um "simulado" para a turma "7A" sem disciplinas
    Then o sistema rejeita o cadastro em simulados
    And o sistema informa em simulados que "RN-12"

  Scenario: Bloquear cadastro com menos de 2 disciplinas (falha - RN-12)
    Given uma "turma" "7A" "está" ativa com professor "P1" que possui especialidade "Matemática"
    When um coordenador tenta cadastrar um "simulado" para a turma "7A" com apenas 1 disciplina
    Then o sistema rejeita o cadastro em simulados
    And o sistema informa em simulados que "RN-12"

  Scenario: Bloquear cadastro sem data de aplicação (falha - NOT NULL)
    Given uma "turma" "7A" "está" ativa com professor "P1" que possui especialidade "Matemática"
    When um coordenador tenta cadastrar um "simulado" para a turma "7A" sem data de aplicação
    Then o sistema rejeita o cadastro em simulados
    And o sistema informa em simulados que "dataAplicacao não pode ser nula"

  Scenario: Bloquear cadastro sem turma (falha - NOT NULL)
    Given uma "turma" "7A" "está" ativa com professor "P1" que possui especialidade "Matemática"
    When um coordenador tenta cadastrar um "simulado" sem turma
    Then o sistema rejeita o cadastro em simulados
    And o sistema informa em simulados que "turma não pode ser nula"
