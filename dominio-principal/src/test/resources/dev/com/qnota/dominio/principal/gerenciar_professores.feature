Feature: Gerenciar professores

  # ========= H3.1 CADASTRAR PROFESSOR =========

  Scenario: Cadastrar professor válido (sucesso - RN-84)
    Given um "professor" com nome "João Silva" e especialidades "Matemática" e "Física" "não está" registrado
    When um coordenador cadastra o "professor" com dados válidos
    Then o sistema confirma o cadastro do "professor"

  Scenario: Cadastrar professor com uma especialidade (sucesso - RN-84)
    Given um "professor" com nome "Maria Santos" e especialidade "Português" "não está" registrado
    When um coordenador cadastra o "professor" com dados válidos
    Then o sistema confirma o cadastro do "professor"

  Scenario: Bloquear cadastro sem especialidades (falha - RN-84)
    Given um "professor" com nome "Pedro Costa" e especialidades "vazias" "não está" registrado
    When um coordenador tenta cadastrar o "professor" sem especialidades
    Then o sistema rejeita o cadastro em professores
    And o sistema informa em professores que "Professor deve ter ao menos uma especialidade"

  Scenario: Bloquear cadastro com especialidades duplicadas (falha - RN-84)
    Given um "professor" com nome "Ana Lima" e especialidades "Matemática" e "matemática" "não está" registrado
    When um coordenador tenta cadastrar o "professor" com especialidades duplicadas
    Then o sistema rejeita o cadastro em professores
    And o sistema informa em professores que "Professor deve ter ao menos uma especialidade"

  # ========= H3.2 EDITAR PROFESSOR =========

  Scenario: Atualizar dados de contato do professor (sucesso)
    Given um "professor" "está" registrado
    When um coordenador atualiza os dados de contato do "professor"
    Then o sistema confirma a alteração do "professor"

  Scenario: Adicionar especialidade ao professor (sucesso)
    Given um "professor" "está" registrado
    When um coordenador adiciona a especialidade "Química" ao "professor"
    Then o sistema confirma a alteração do "professor"

  Scenario: Remover especialidade mantendo pelo menos uma (sucesso - RN-84)
    Given um "professor" com especialidades "Matemática" e "Física" "está" registrado
    When um coordenador remove a especialidade "Física" do "professor"
    Then o sistema confirma a alteração do "professor"

  Scenario: Bloquear remoção da última especialidade (falha - RN-84)
    Given um "professor" com especialidade "Matemática" "está" registrado
    When um coordenador tenta remover a única especialidade do "professor"
    Then o sistema rejeita a alteração em professores
    And o sistema informa em professores que "Professor deve ter ao menos uma especialidade"

  # ========= H3.3 VALIDAR LIMITE DE TURMAS =========

  Scenario: Validar professor com até 3 turmas ativas (sucesso - RN-07)
    Given um "professor" "está" registrado e "possui" 3 turmas ativas
    When um coordenador valida o limite de turmas do "professor"
    Then o sistema confirma que o "professor" está dentro do limite

  Scenario: Bloquear professor com mais de 3 turmas ativas (falha - RN-07)
    Given um "professor" "está" registrado e "possui" 4 turmas ativas
    When um coordenador tenta validar o limite de turmas do "professor"
    Then o sistema rejeita a validação em professores
    And o sistema informa em professores que "Até 3 turmas simultâneas"

  # ========= H3.4 EXCLUIR PROFESSOR =========

  Scenario: Excluir professor sem simulados finalizados (sucesso - RN-26A)
    Given um "professor" "está" registrado e "não possui" simulados finalizados
    And existe um professor "substituto" válido
    When um coordenador exclui o "professor" com substituto
    Then o sistema confirma a exclusão do "professor"

  Scenario: Bloquear exclusão com simulados finalizados (falha - RN-26A)
    Given um "professor" "está" registrado e "possui" simulados finalizados
    And existe um professor "substituto" válido
    When um coordenador tenta excluir o "professor" com substituto
    Then o sistema rejeita a exclusão em professores
    And o sistema informa em professores que "Não pode excluir se houver simulados finalizados vinculados"

  # ========= VALIDAÇÕES BÁSICAS =========

  Scenario: Bloquear cadastro sem nome do professor (falha - NOT NULL)
    Given um "professor" "não está" registrado e "sem nome"
    When um coordenador tenta cadastrar o "professor" com dados válidos
    Then o sistema rejeita o cadastro em professores
    And o sistema informa em professores que "'nome' não pode ser vazio"

  Scenario: Bloquear cadastro com nome do professor em branco (falha - NOT BLANK)
    Given um "professor" "não está" registrado e "nome em branco"
    When um coordenador tenta cadastrar o "professor" com dados válidos
    Then o sistema rejeita o cadastro em professores
    And o sistema informa em professores que "'nome' não pode ser vazio"

  Scenario: Bloquear cadastro sem CPF do professor (falha - NOT NULL)
    Given um "professor" "não está" registrado e "sem CPF"
    When um coordenador tenta cadastrar o "professor" com dados válidos
    Then o sistema rejeita o cadastro em professores
    And o sistema informa em professores que "'cpf' não pode ser vazio"

  Scenario: Bloquear cadastro sem email do professor (falha - NOT NULL)
    Given um "professor" "não está" registrado e "sem email"
    When um coordenador tenta cadastrar o "professor" com dados válidos
    Then o sistema rejeita o cadastro em professores
    And o sistema informa em professores que "'email' não pode ser vazio"

  Scenario: Bloquear cadastro com especialidade vazia (falha - NOT BLANK)
    Given um "professor" "não está" registrado e "especialidade vazia"
    When um coordenador tenta cadastrar o "professor" com especialidade vazia
    Then o sistema rejeita o cadastro em professores
    And o sistema informa em professores que "especialidade não pode ser vazia"


