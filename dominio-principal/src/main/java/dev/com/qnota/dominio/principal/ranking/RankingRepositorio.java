package dev.com.qnota.dominio.principal.ranking;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;

public interface RankingRepositorio {

    // ===== contrato legado (linhas por simulado) =====

    /** remove todas as linhas de ranking do simulado */
    void limpar(SimuladoId simulado);

    /** insere as posições calculadas (congelado=false) */
    void salvarPosicoes(SimuladoId simulado, List<ItemRanking> itens);

    /** marca todas as linhas do simulado como congeladas (congelado=true) */
    void congelar(SimuladoId simulado);

    /** retorna se o ranking está congelado (todas as linhas do simulado) */
    boolean estaCongelado(SimuladoId simulado);

    /** carrega as posições atuais (ordenadas por posicao) */
    List<ItemRanking> carregar(SimuladoId simulado);

    record ItemRanking(AlunoId aluno, double media, int posicao) {}

    // ===== novo contrato (agregado) =====

    /**
     * Persiste o agregado. Implementações com auto-increment devem:
     * - gerar um novo RankingId ao inserir (se getId()==null) e invocar ranking.atribuirIdSeAusente(...)
     * - atualizar linhas e flag de congelamento ao atualizar.
     * Implementação default: reutiliza o contrato legado (sem id).
     */
    default Ranking salvar(Ranking ranking) {
        var simId = ranking.getSimulado();
        limpar(simId);
        var itens = ranking.getLinhas().stream()
                .map(l -> new ItemRanking(l.aluno(), l.media(), l.posicao()))
                .collect(Collectors.toList());
        salvarPosicoes(simId, itens);
        if (ranking.isCongelado()) {
            congelar(simId);
        }
        return ranking; // em memória pode continuar sem id
    }

    /**
     * Carrega o agregado pelo simulado, montando a partir do contrato legado.
     * (Nome diferente para não colidir com NotaRepositorio.porSimulado(...)).
     */
    default Optional<Ranking> carregarAgregado(SimuladoId simulado) {
        var itens = carregar(simulado);
        var linhas = itens.stream()
                .map(i -> new Ranking.Linha(i.aluno(), i.media(), i.posicao()))
                .collect(Collectors.toList());
        var r = new Ranking(simulado, linhas);
        if (estaCongelado(simulado)) {
            r.congelar();
        }
        return Optional.of(r);
    }
}
