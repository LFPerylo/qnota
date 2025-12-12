Feature: Gerenciar responsáveis

  # ========= H4.0 CRIAR RESPONSÁVEL =========

  Scenario: Cadastrar responsável com CPF válido (sucesso)
    Given um "responsável" com CPF "123.456.789-09" "não está" registrado
    When um coordenador cadastra o "responsável" com nome "Ana Souza" e e-mail "ana@exemplo.com"
    Then o sistema confirma o cadastro do "responsável"

  Scenario: Bloquear cadastro com CPF em formato inválido (falha)
    Given um "responsável" com CPF "123" "não está" registrado
    When um coordenador tenta cadastrar o "responsável" com esse CPF
    Then o sistema rejeita o cadastro
    And o sistema informa que "o CPF está em formato inválido"

  Scenario: Bloquear cadastro de responsável com CPF já existente (falha)
    Given um "responsável" com CPF "529.982.247-25" "já está" registrado
    When um coordenador tenta cadastrar outro "responsável" com o CPF "529.982.247-25"
    Then o sistema rejeita o cadastro
    And o sistema informa que "já existe responsável com esse CPF"

  Scenario: Cadastrar responsável com CPF inédito (sucesso)
    Given não existe "responsável" com CPF "145.382.206-20"
    When um coordenador cadastra o "responsável" com CPF "145.382.206-20"
    Then o sistema confirma o cadastro do "responsável"

  # --------- Validações básicas no CADASTRO (NOT NULL / NOT BLANK) ---------

  Scenario: Bloquear cadastro sem nome (falha - NOT BLANK)
    Given um "responsável" "não está" registrado e "sem nome"
    When um coordenador tenta cadastrar o "responsável" com CPF "111.444.777-35" e e-mail "a@a.com"
    Then o sistema rejeita o cadastro
    And o sistema informa que "'nome' não pode ser vazio"

  Scenario: Cadastrar responsável com nome preenchido (sucesso - NOT BLANK)
    Given um "responsável" "não está" registrado
    When um coordenador cadastra o "responsável" com nome "Bruno Lima", CPF "111.444.777-35" e e-mail "bruno@exemplo.com"
    Then o sistema confirma o cadastro do "responsável"

  Scenario: Bloquear cadastro sem e-mail (falha - NOT BLANK)
    Given um "responsável" "não está" registrado e "sem e-mail"
    When um coordenador tenta cadastrar o "responsável" com nome "Carla" e CPF "145.382.206-20"
    Then o sistema rejeita o cadastro
    And o sistema informa que "'email' não pode ser vazio"

  Scenario: Cadastrar responsável com e-mail preenchido (sucesso - NOT BLANK)
    Given um "responsável" "não está" registrado
    When um coordenador cadastra o "responsável" com nome "Carla", CPF "529.982.247-25" e e-mail "carla@exemplo.com"
    Then o sistema confirma o cadastro do "responsável"

  Scenario: Bloquear cadastro sem CPF (falha - NOT NULL/BLANK)
    Given um "responsável" "não está" registrado e "sem CPF"
    When um coordenador tenta cadastrar o "responsável" com nome "Diego" e e-mail "diego@exemplo.com"
    Then o sistema rejeita o cadastro
    And o sistema informa que "o CPF está em formato inválido"

  Scenario: Cadastrar responsável com CPF somente dígitos (sucesso - formato aceito)
    Given um "responsável" "não está" registrado
    When um coordenador cadastra o "responsável" com nome "Elaine", CPF "52998224725" e e-mail "elaine@exemplo.com"
    Then o sistema confirma o cadastro do "responsável"

  # ========= H4.3 EDITAR RESPONSÁVEL =========

  Scenario: Editar nome e e-mail mantendo CPF (sucesso)
    Given um "responsável" com CPF "123.456.789-09" "está" registrado
    When um coordenador altera o nome do "responsável" para "Ana Paula Souza" e o e-mail para "ana.paula@exemplo.com"
    Then o sistema confirma a atualização dos dados do "responsável"

  # --------- Validações básicas na EDIÇÃO (NOT BLANK) ---------

  Scenario: Bloquear edição com nome vazio (falha - NOT BLANK)
    Given um "responsável" com CPF "123.456.789-09" "está" registrado
    When um coordenador tenta alterar o nome do "responsável" para "" mantendo o e-mail "ana@exemplo.com"
    Then o sistema rejeita a atualização
    And o sistema informa que "'nome' não pode ser vazio"

  Scenario: Bloquear edição com e-mail vazio (falha - NOT BLANK)
    Given um "responsável" com CPF "123.456.789-09" "está" registrado
    When um coordenador tenta alterar o e-mail do "responsável" para "" mantendo o nome "Ana"
    Then o sistema rejeita a atualização
    And o sistema informa que "'email' não pode ser vazio"

  # ========= H4.2 VINCULAR / DESVINCULAR =========

  Scenario: Desvincular mantendo ao menos um responsável (sucesso - RN-19)
    Given um "aluno" "está" vinculado aos "responsáveis" "R1" e "R2"
    When um coordenador desvincula o "responsável" "R2" do "aluno"
    Then o sistema confirma a desvinculação
    And o "aluno" permanece com pelo menos um "responsável" ativo

  Scenario: Bloquear desvinculação que deixaria o aluno sem responsável (falha - RN-19)
    Given um "aluno" "está" vinculado apenas ao "responsável" "R1"
    When um coordenador tenta desvincular o "responsável" "R1" do "aluno"
    Then o sistema rejeita a desvinculação
    And o sistema informa que "o aluno deve ter pelo menos um responsável"

  Scenario: Vincular responsável ainda não associado ao aluno (sucesso - RN-20)
    Given um "aluno" "está" vinculado ao "responsável" "R1"
    And o "responsável" "R2" "não está" vinculado a esse "aluno"
    When um coordenador vincula o "responsável" "R2" ao "aluno" com grau "Mãe" e "não principal"
    Then o sistema confirma o vínculo do "responsável" ao "aluno"

  Scenario: Bloquear vínculo duplicado de responsável com o mesmo aluno (falha - RN-20)
    Given um "aluno" "está" vinculado ao "responsável" "R1"
    When um coordenador tenta vincular novamente o "responsável" "R1" ao mesmo "aluno"
    Then o sistema rejeita o vínculo
    And o sistema informa que "já existe vínculo entre o responsável e o aluno"


  # --------- Inadimplência no vínculo (RN-136) ---------

  Scenario: Bloquear vínculo de responsável inadimplente (falha - RN-136)
    Given um "responsável" "está" marcado como "inadimplente"
    And o "responsável" "não está" vinculado ao "aluno" "A1"
    When um coordenador tenta vincular esse "responsável" ao "aluno" "A1"
    Then o sistema rejeita o vínculo
    And o sistema informa que "responsável inadimplente não pode ser vinculado até regularização"

  Scenario: Permitir vínculo após regularização (sucesso - RN-136)
    Given um "responsável" "estava" marcado como "inadimplente" e "foi" regularizado
    And o "responsável" "não está" vinculado ao "aluno" "A1"
    When um coordenador vincula o "responsável" ao "aluno" "A1" com grau "Tio" e "não principal"
    Then o sistema confirma o vínculo do "responsável" ao "aluno"

  # ========= H4.1 EXCLUIR RESPONSÁVEL =========

  Scenario: Excluir responsável sem vínculos ativos (sucesso - RN-21)
    Given um "responsável" "está" registrado e "não possui" vínculos ativos com "alunos"
    When um coordenador exclui o "responsável"
    Then o sistema confirma a exclusão do "responsável"

  Scenario: Bloquear exclusão de responsável vinculado a aluno (falha - RN-21)
    Given um "responsável" "está" registrado e "possui" vínculo com o "aluno" "A1"
    When um coordenador tenta excluir o "responsável"
    Then o sistema rejeita a exclusão
    And o sistema informa que "o responsável possui vínculos ativos"
