/* Título da análise: QNota - Finalização padrão de Simulado (Template Method) */
package dev.com.qnota.dominio.principal.simulado;

import java.util.Objects;

/**
 * Implementação padrão da finalização de simulado no QNota.
 * Regras cobertas diretamente aqui:
 * - RN-16: todas as notas devem estar lançadas antes de finalizar
 *
 * A RN-102 (congelar ranking após a finalização) passa a ser tratada
 * por um Observer (RankingServico).
 */
public class FinalizacaoSimuladoPadrao extends FinalizacaoSimuladoTemplate {

    private final SimuladoRepositorio simuladoRepo;

    public FinalizacaoSimuladoPadrao(SimuladoRepositorio simuladoRepo) {
        this.simuladoRepo = Objects.requireNonNull(simuladoRepo);
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
}
