Feature: Retificar Nota com Justificativa

  # RN-37: justificativa obrigatória com >= 20 caracteres
  # RN-38: armazenar nota original e retificada como versões separadas
  # RN-39: só pode retificar quando o simulado está EM_EDICAO

  Scenario: Retificar com justificativa válida em simulado em edição (sucesso - RN-37/RN-38/RN-39)
    Given um simulado "em edição" com nota original 6.0
    When o coordenador retifica a nota para 8.5 com justificativa "Corrigido após revisão do gabarito: questão reavaliada."
    Then o sistema confirma retificação com nova versão armazenada
    And a justificativa registra valores 6.0 -> 8.5

  Scenario: Bloquear retificação sem justificativa suficiente (falha - RN-37)
    Given um simulado "em edição" com nota original 7.0
    When o coordenador tenta retificar a nota para 7.5 com justificativa "muito curto"
    Then o sistema rejeita a retificação e informa "RN-37"

  Scenario: Bloquear retificação em simulado finalizado (falha - RN-39)
    Given um simulado "finalizado" com nota original 5.0
    When o coordenador tenta retificar a nota para 6.0 com justificativa "Após revisão detalhada, pontos foram corretamente ajustados."
    Then o sistema rejeita a retificação e informa "RN-39"
