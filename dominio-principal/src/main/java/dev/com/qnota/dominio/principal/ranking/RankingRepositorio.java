package dev.com.qnota.dominio.principal.ranking;

import java.util.List;
import java.util.Optional;

import dev.com.qnota.dominio.principal.simulado.SimuladoId;

public interface RankingRepositorio {

    // ===== contrato em termos do VO do domínio =====

    /** remove todas as linhas de ranking do simulado */
    void limpar(SimuladoId simulado);

    /** insere as posições calculadas (congelado=false) */
    void salvarPosicoes(SimuladoId simulado, List<Ranking.Linha> linhas);

    /** marca todas as linhas do simulado como congeladas (congelado=true) */
    void congelar(SimuladoId simulado);

    /** retorna se o ranking está congelado (todas as linhas do simulado) */
    boolean estaCongelado(SimuladoId simulado);

    /** carrega as posições atuais (ordenadas por posicao) */
    List<Ranking.Linha> carregar(SimuladoId simulado);

    // ===== agregado =====

    /**
     * Persiste o agregado. Implementações com auto-incremento podem:
     * - gerar um RankingId se getId()==null e chamar ranking.atribuirIdSeAusente(...)
     * - atualizar linhas e flag congelado.
     * Implementação default: reusa o contrato em linhas (sem id).
     */
    default Ranking salvar(Ranking ranking) {
        var simId = ranking.getSimulado();
        limpar(simId);
        salvarPosicoes(simId, ranking.getLinhas());
        if (ranking.isCongelado()) {
            congelar(simId);
        }
        return ranking;
    }

    /** Carrega o agregado a partir das linhas e do estado de congelamento. */
    default Optional<Ranking> carregarAgregado(SimuladoId simulado) {
        var linhas = carregar(simulado);
        var r = new Ranking(simulado, linhas);
        if (estaCongelado(simulado)) {
            r.congelar();
        }
        return Optional.of(r);
    }
}
