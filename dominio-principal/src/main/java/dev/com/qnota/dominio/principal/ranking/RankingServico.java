package dev.com.qnota.dominio.principal.ranking;

import java.util.List;

import dev.com.qnota.dominio.principal.aluno.AlunoRepositorio;
import dev.com.qnota.dominio.principal.simulado.Simulado;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.simulado.SimuladoObserver;
import dev.com.qnota.dominio.principal.simulado.SimuladoRepositorio;

public class RankingServico implements SimuladoObserver {

    private final AlunoRepositorio alunoRepo;
    private final SimuladoRepositorio simuladoRepo;
    private final RankingRepositorio rankingRepo;
    private final CalculoRankingStrategy calculoRanking;

    // Agora só precisa da Strategy

    public RankingServico(AlunoRepositorio alunoRepo,
                          SimuladoRepositorio simuladoRepo,
                          RankingRepositorio rankingRepo,
                          CalculoRankingStrategy calculoRanking) {
        this.alunoRepo = alunoRepo;
        this.simuladoRepo = simuladoRepo;
        this.rankingRepo = rankingRepo;
        this.calculoRanking = calculoRanking;
    }

    /** RN-98/99: recalcula e salva (não congela). Se já estiver congelado, devolve o atual. */
    public List<Ranking.Linha> recalcular(SimuladoId simuladoId) {
        var simulado = simuladoRepo.porId(simuladoId);

        if (simulado.getStatus() == Simulado.Status.FINALIZADO
                || rankingRepo.estaCongelado(simuladoId)) {
            return rankingRepo.carregar(simuladoId); // RN-102
        }

        var pesos = simuladoRepo.pesosDoSimulado(simuladoId);
        var alunos = alunoRepo.porTurma(simulado.getTurma());

        var linhas = calculoRanking.calcular(alunos, pesos);

        rankingRepo.limpar(simuladoId);
        rankingRepo.salvarPosicoes(simuladoId, linhas);
        return linhas;
    }

    /** Versão orientada a agregado (opcional). */
    public Ranking recalcularComoAgregado(SimuladoId simuladoId) {
        var simulado = simuladoRepo.porId(simuladoId);

        if (simulado.getStatus() == Simulado.Status.FINALIZADO
                || rankingRepo.estaCongelado(simuladoId)) {
            return rankingRepo.carregarAgregado(simuladoId)
                    .orElseGet(() -> new Ranking(simuladoId, List.of()));
        }

        var pesos = simuladoRepo.pesosDoSimulado(simuladoId);
        var alunos = alunoRepo.porTurma(simulado.getTurma());

        var linhas = calculoRanking.calcular(alunos, pesos);

        var ranking = new Ranking(simuladoId, linhas);
        return rankingRepo.salvar(ranking);
    }

    /** RN-102: congela o ranking após finalização do simulado. */
    public void congelar(SimuladoId simuladoId) {
        if (!rankingRepo.estaCongelado(simuladoId)) {
            recalcular(simuladoId); // garante cálculo salvo
            rankingRepo.congelar(simuladoId);
        }
    }

    /** Implementação de Observer: reage à finalização de simulados. */
    @Override
    public void aoFinalizarSimulado(SimuladoId id) {
        congelar(id); // RN-102 reagindo ao evento de finalização
    }
}
