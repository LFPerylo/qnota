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
    private final CalculoRankingMediaPonderada calculoPonderada;
    private final CalculoRankingMediaAritmetica calculoAritmetica;

    public RankingServico(AlunoRepositorio alunoRepo,
                          SimuladoRepositorio simuladoRepo,
                          RankingRepositorio rankingRepo,
                          CalculoRankingMediaPonderada calculoPonderada,
                          CalculoRankingMediaAritmetica calculoAritmetica) {
        this.alunoRepo = alunoRepo;
        this.simuladoRepo = simuladoRepo;
        this.rankingRepo = rankingRepo;
        this.calculoPonderada = calculoPonderada;
        this.calculoAritmetica = calculoAritmetica;
    }

    private CalculoRankingStrategy escolherEstrategia(Simulado simulado) {
        return simulado.getFormulaCalculo() == Simulado.FormulaCalculo.ARITMETICA
            ? calculoAritmetica
            : calculoPonderada;
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

        var estrategia = escolherEstrategia(simulado);
        var linhas = estrategia.calcular(alunos, pesos);

        // Orquestração movida do repositório para o serviço (camada correta)
        rankingRepo.limpar(simuladoId);
        rankingRepo.salvarPosicoes(simuladoId, linhas);
        return linhas;
    }

    /** Versão orientada a agregado (opcional). */
    public Ranking recalcularComoAgregado(SimuladoId simuladoId) {
        var simulado = simuladoRepo.porId(simuladoId);

        // Se já está congelado, carrega o agregado existente
        if (simulado.getStatus() == Simulado.Status.FINALIZADO
                || rankingRepo.estaCongelado(simuladoId)) {
            return carregarAgregado(simuladoId); // delegação interna
        }

        var pesos = simuladoRepo.pesosDoSimulado(simuladoId);
        var alunos = alunoRepo.porTurma(simulado.getTurma());

        var estrategia = escolherEstrategia(simulado);
        var linhas = estrategia.calcular(alunos, pesos);

        // Orquestração movida do repositório para o serviço (camada correta)
        var ranking = new Ranking(simuladoId, linhas);
        salvarAgregado(ranking); // delegação interna
        return ranking;
    }

    /** Carrega o agregado a partir das linhas e do estado de congelamento. */
    private Ranking carregarAgregado(SimuladoId simuladoId) {
        var linhas = rankingRepo.carregar(simuladoId);
        var ranking = new Ranking(simuladoId, linhas);
        if (rankingRepo.estaCongelado(simuladoId)) {
            ranking.congelar();
        }
        return ranking;
    }

    /** Persiste o agregado usando operações primitivas do repositório. */
    private void salvarAgregado(Ranking ranking) {
        var simuladoId = ranking.getSimulado();
        rankingRepo.limpar(simuladoId);
        rankingRepo.salvarPosicoes(simuladoId, ranking.getLinhas());
        if (ranking.isCongelado()) {
            rankingRepo.congelar(simuladoId);
        }
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
