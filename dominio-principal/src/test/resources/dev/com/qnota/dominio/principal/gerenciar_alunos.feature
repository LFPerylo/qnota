Feature: Gerenciar alunos

	# ========= H1.1 CADASTRAR ALUNO =========

	Scenario: Cadastrar aluno válido em turma (sucesso - RN-03)
		Given um "aluno" com nome "Maria Silva" e nascimento "2013-04-10" "não está" registrado na turma "7A"
		When um coordenador cadastra o "aluno" na turma "7A" com responsáveis válidos
		Then o sistema confirma o cadastro do "aluno"

	Scenario: Impedir cadastro duplicado por nome e data na mesma turma (falha - RN-03)
		Given um "aluno" com nome "Maria Silva" e nascimento "2013-04-10" "já está" registrado na turma "7A"
		When um coordenador tenta cadastrar o mesmo "aluno" novamente na turma "7A"
		Then o sistema rejeita o cadastro
		And o sistema informa que "já existe aluno com mesmo nome e data de nascimento na turma"

	Scenario: Cadastrar aluno com até três responsáveis (sucesso - RN-02)
		Given um "aluno" "não está" registrado
		When um coordenador cadastra o "aluno" informando exatamente 3 responsáveis
		Then o sistema confirma o cadastro do "aluno"

	Scenario: Impedir cadastro com mais de três responsáveis (falha - RN-02)
		Given um "aluno" "não está" registrado
		When um coordenador tenta cadastrar o "aluno" informando 4 responsáveis
		Then o sistema rejeita o cadastro
		And o sistema informa que "o número máximo de responsáveis por aluno é 3"

	Scenario: Cadastrar aluno com responsável principal definido (sucesso - RN-58)
		Given um "aluno" "não está" registrado
		When um coordenador cadastra o "aluno" com responsáveis válidos e um deles marcado como "principal"
		Then o sistema confirma o cadastro do "aluno"

	Scenario: Bloquear cadastro sem responsável principal (falha - RN-58)
		Given um "aluno" "não está" registrado
		When um coordenador tenta cadastrar o "aluno" sem nenhum responsável marcado como "principal"
		Then o sistema rejeita o cadastro
		And o sistema informa que "é obrigatório definir um responsável principal"

	# ========= H1 EDITAR / TRANSFERIR TURMA =========

	Scenario: Transferir aluno para turma do mesmo ano e sem simulados finalizados (sucesso - RN-57/RN-57a)
		Given um "aluno" da turma "7A" "não possui" simulados finalizados
		And a turma de destino "7B" pertence ao ano letivo "2025" igual ao da turma atual
		When um coordenador transfere o "aluno" da turma "7A" para "7B"
		Then o sistema confirma a transferência do "aluno"

	Scenario: Bloquear transferência por diferença de ano letivo (falha - RN-57a)
		Given um "aluno" da turma "7A" "não possui" simulados finalizados
		And a turma de destino "8B" pertence ao ano letivo "2026" diferente do atual
		When um coordenador tenta transferir o "aluno" para "8B"
		Then o sistema rejeita a transferência
		And o sistema informa que "a nova turma deve estar no mesmo ano letivo"

	Scenario: Bloquear alteração de turma quando há simulados finalizados (falha - RN-57)
		Given um "aluno" da turma "7A" "possui" simulados finalizados
		When um coordenador tenta transferir o "aluno" para a turma "7B"
		Then o sistema rejeita a transferência
		And o sistema informa que "não é permitido alterar a turma do aluno com simulados finalizados"

	# ========= H1.2 EXCLUIR / INATIVAR =========

	Scenario: Excluir aluno sem notas registradas (sucesso - RN-04)
		Given um "aluno" "está" registrado e "não possui" notas em simulados
		When um coordenador exclui o "aluno"
		Then o sistema confirma a exclusão do "aluno"

	Scenario: Bloquear exclusão de aluno com notas (falha - RN-04)
		Given um "aluno" "está" registrado e "possui" notas em simulados
		When um coordenador tenta excluir o "aluno"
		Then o sistema rejeita a exclusão
		And o sistema informa que "o aluno possui vínculos com simulados/nota"

	Scenario: Inativar aluno sem pendências de notas (sucesso - RN-67)
		Given um "aluno" "está" ativo e "não possui" notas pendentes em simulados em andamento
		When um coordenador inativa o "aluno"
		Then o sistema confirma a inativação do "aluno"

	Scenario: Bloquear inativação com notas pendentes (falha - RN-67)
		Given um "aluno" "está" ativo e "possui" notas pendentes em simulados em andamento
		When um coordenador tenta inativar o "aluno"
		Then o sistema rejeita a inativação
		And o sistema informa que "existem notas pendentes de lançamento"

	# ========= VALIDAÇÕES BÁSICAS (NOT NULL / FORMATO) =========

	Scenario: Bloquear cadastro sem responsáveis (falha - RN-19)
		Given um "aluno" "não está" registrado
		When um coordenador tenta cadastrar o "aluno" na turma "7A" "sem responsáveis"
		Then o sistema rejeita o cadastro
		And o sistema informa que "Aluno deve ter ao menos um responsável"

	Scenario: Bloquear cadastro com responsáveis duplicados (falha - RN-20)
		Given um "aluno" "não está" registrado
		And existem responsáveis "R1" e "R2" válidos
		When um coordenador tenta cadastrar o "aluno" na turma "7A" informando os responsáveis "R1" e "R1"
		Then o sistema rejeita o cadastro
		And o sistema informa que "Vínculo de responsável duplicado"

	Scenario: Bloquear cadastro com dois responsáveis principais (falha - RN-58)
		Given um "aluno" "não está" registrado
		When um coordenador tenta cadastrar o "aluno" na turma "7A" com dois responsáveis marcados como "principal"
		Then o sistema rejeita o cadastro
		And o sistema informa que "deve haver exatamente um responsável principal"

	Scenario: Bloquear cadastro sem nome do aluno (falha - NOT NULL)
		Given um "aluno" "não está" registrado e "sem nome"
		When um coordenador tenta cadastrar o "aluno" na turma "7A" com responsáveis válidos
		Then o sistema rejeita o cadastro
		And o sistema informa que "'nome' não pode ser vazio"

	Scenario: Bloquear cadastro com nome do aluno em branco (falha - NOT BLANK)
		Given um "aluno" "não está" registrado e "nome em branco"
		When um coordenador tenta cadastrar o "aluno" na turma "7A" com responsáveis válidos
		Then o sistema rejeita o cadastro
		And o sistema informa que "'nome' não pode ser vazio"

	Scenario: Bloquear cadastro sem data de nascimento (falha - NOT NULL)
		Given um "aluno" "não está" registrado e "sem data de nascimento"
		When um coordenador tenta cadastrar o "aluno" na turma "7A" com responsáveis válidos
		Then o sistema rejeita o cadastro
		And o sistema informa que "'dataNascimento' não pode ser nula"

	Scenario: Bloquear cadastro sem turma (falha - NOT NULL)
		Given um "aluno" "não está" registrado e "sem turma"
		When um coordenador tenta cadastrar o "aluno" na turma "7A" com responsáveis válidos
		Then o sistema rejeita o cadastro
		And o sistema informa que "'turma' não pode ser nula"

	Scenario: Bloquear cadastro com item de responsável nulo (falha - NOT NULL)
		Given um "aluno" "não está" registrado
		When um coordenador tenta cadastrar o "aluno" na turma "7A" contendo um responsável "nulo" na lista
		Then o sistema rejeita o cadastro
		And o sistema informa que "Responsável não pode ser nulo"

	Scenario: Bloquear cadastro com grau de parentesco vazio (falha - NOT BLANK)
		Given um "aluno" "não está" registrado
		And existe um responsável "R1" válido com "grau de parentesco vazio"
		When um coordenador tenta cadastrar o "aluno" na turma "7A" informando o responsável "R1"
		Then o sistema rejeita o cadastro
		And o sistema informa que "'grauParentesco' não pode ser vazio"
