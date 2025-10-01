package dev.com.qnota.dominio.avaliacao.ranking;

import java.util.List;

import dev.com.qnota.dominio.academico.aluno.AlunoId;
import dev.com.qnota.dominio.avaliacao.simulado.SimuladoId;

public interface RankingRepositorio {

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
}
