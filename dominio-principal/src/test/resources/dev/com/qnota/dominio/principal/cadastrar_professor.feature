Feature: Cadastrar professor

	Scenario: Professor válido
		Given um professor com nome "João Silva"
     When o professor for cadastrado
     Then o professor é cadastrado com sucesso

	Scenario: Professor já cadastrado
		Given um professor com nome "João Silva" já está cadastrado
     When o professor for cadastrado novamente
     Then o sistema informa que o professor já está cadastrado
