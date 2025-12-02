package dev.com.qnota.dominio.principal.ranking;

import java.util.List;

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

    // Nota: Métodos de orquestração de agregados foram REMOVIDOS.
    // A lógica agora está corretamente no RankingServico (camada de domínio).
    // Implementações concretas (infraestrutura) podem fornecer operações agregadas
    // se necessário para otimização, mas a interface de domínio permanece simples.
}
