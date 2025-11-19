package dev.com.qnota.dominio.principal.simulado;

import java.util.Objects;

import dev.com.qnota.dominio.principal.ranking.RankingServico;

/**
 * Implementação padrão da finalização de simulado no QNota.
 * Regras cobertas:
 * - RN-16: todas as notas devem estar lançadas antes de finalizar
 * - RN-102: congelar ranking após a finalização
 */
public class FinalizacaoSimuladoPadrao extends FinalizacaoSimuladoTemplate {

    private final SimuladoRepositorio simuladoRepo;
    private final RankingServico rankingServico;

    public FinalizacaoSimuladoPadrao(SimuladoRepositorio simuladoRepo,
                                     RankingServico rankingServico) {
        this.simuladoRepo = Objects.requireNonNull(simuladoRepo);
        this.rankingServico = Objects.requireNonNull(rankingServico);
    }

    @Override
    protected Simulado carregarSimulado(SimuladoId id) {
        return simuladoRepo.porId(id);
    }

    @Override
    protected void validarPreCondicoes(SimuladoId id, Simulado s) {
        // RN-16: Todas as notas devem estar lançadas
        if (!simuladoRepo.todasNotasLancadas(id)) {
            throw new IllegalStateException("RN-16: Todas as notas devem estar lançadas.");
        }
    }

    @Override
    protected void salvar(Simulado s) {
        simuladoRepo.salvar(s);
    }

    @Override
    protected void aposFinalizar(Simulado s, SimuladoId id) {
        // RN-102: congelar ranking após finalização do simulado
        rankingServico.congelar(id);
    }
}
