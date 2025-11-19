Feature: Gerenciar disciplinas

  # ========= H7.0 CADASTRAR DISCIPLINA =========

  Scenario: Cadastrar disciplina v1 ativa com nome único por área (sucesso - RN-121)
    Given uma "disciplina" com nome "Matemática" e área "Exatas" que "não está" registrada
    When um coordenador cadastra a "disciplina" com dados válidos
    Then o sistema confirma o cadastro da "disciplina" v1 ativa

  Scenario: Impedir cadastro de disciplina com nome duplicado na mesma área (falha - RN-121)
    Given uma "disciplina" com nome "Matemática" e área "Exatas" que "já está" registrada
    When um coordenador tenta cadastrar a "disciplina" com nome "Matemática" na área "Exatas"
    Then o sistema rejeita o cadastro em disciplinas
    And o sistema informa em disciplinas que "RN-121"

  Scenario: Permitir mesmo nome em áreas distintas (sucesso - RN-121)
    Given uma "disciplina" com nome "Biologia" e área "Naturezas" que "já está" registrada
    When um coordenador cadastra a "disciplina" com nome "Biologia" na área "Saúde"
    Then o sistema confirma o cadastro da "disciplina" v1 ativa

  # ========= H7.1 EDITAR DISCIPLINA =========

  Scenario: Editar in-place quando não há simulados finalizados (sucesso - RN-43/RN-62)
    Given uma "disciplina" com nome "Química" e área "Exatas" que "já está" registrada
    And a "disciplina" "não foi" usada em simulados finalizados
    When um coordenador edita a "disciplina" para nome "Química Orgânica" e área "Exatas"
    Then o sistema confirma a alteração da "disciplina" mantendo o mesmo id e versao
    And o sistema confirma que a "disciplina" tem nome "Química Orgânica" e área "Exatas"

  Scenario: Criar nova versão quando já foi usada em simulados finalizados (sucesso - RN-62)
    Given uma "disciplina" com nome "História" e área "Humanas" que "já está" registrada
    And a "disciplina" "foi" usada em simulados finalizados
    When um coordenador edita a "disciplina" para nome "História Geral" e área "Humanas"
    Then o sistema confirma a criação de nova versão da "disciplina"
    And a nova versão está ativa com versao original + 1 e idVersaoOrigem preenchido
    And a versão original permanece preservada

  Scenario: Bloquear edição que viole unicidade por área (falha - RN-121)
    Given uma "disciplina" com nome "Geografia" e área "Humanas" que "já está" registrada
    And uma "disciplina" com nome "Geo" e área "Humanas" que "já está" registrada
    And a "disciplina" "Geografia" "foi" usada em simulados finalizados
    When um coordenador tenta editar "Geografia" para nome "Geo" e área "Humanas"
    Then o sistema rejeita a alteração em disciplinas
    And o sistema informa em disciplinas que "RN-121"

  Scenario: Editar apenas a área respeitando unicidade (sucesso - RN-121)
    Given uma "disciplina" com nome "Filosofia" e área "Humanas" que "já está" registrada
    And a "disciplina" "não foi" usada em simulados finalizados
    When um coordenador edita a "disciplina" para nome "Filosofia" e área "Sociais"
    Then o sistema confirma a alteração da "disciplina" mantendo o mesmo id e versao
    And o sistema confirma que a "disciplina" tem nome "Filosofia" e área "Sociais"

  # ========= H7.2 EXCLUIR DISCIPLINA =========

  Scenario: Excluir disciplina nunca usada em simulados (sucesso - RN-44)
    Given uma "disciplina" com nome "Artes" e área "Humanas" que "já está" registrada
    And a "disciplina" "não foi" usada em simulados (qualquer status)
    When um coordenador exclui a "disciplina"
    Then o sistema confirma a exclusão da "disciplina"

  Scenario: Bloquear exclusão se usada em simulado em edição (falha - RN-44)
    Given uma "disciplina" com nome "Redação" e área "Linguagens" que "já está" registrada
    And a "disciplina" "foi" usada em simulados em edição
    When um coordenador tenta excluir a "disciplina"
    Then o sistema rejeita a exclusão em disciplinas
    And o sistema informa em disciplinas que "RN-44"

  Scenario: Bloquear exclusão se usada em simulado finalizado (falha - RN-44)
    Given uma "disciplina" com nome "Física" e área "Exatas" que "já está" registrada
    And a "disciplina" "foi" usada em simulados finalizados
    When um coordenador tenta excluir a "disciplina"
    Then o sistema rejeita a exclusão em disciplinas
    And o sistema informa em disciplinas que "RN-44"
