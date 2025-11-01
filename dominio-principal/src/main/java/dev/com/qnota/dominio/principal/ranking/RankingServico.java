package dev.com.qnota.dominio.principal.ranking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import dev.com.qnota.dominio.principal.aluno.Aluno;
import dev.com.qnota.dominio.principal.aluno.AlunoRepositorio;
import dev.com.qnota.dominio.principal.aluno.NotaServico;
import dev.com.qnota.dominio.principal.simulado.Simulado;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.simulado.SimuladoRepositorio;

public class RankingServico {

    private final AlunoRepositorio alunoRepo;
    private final SimuladoRepositorio simuladoRepo;
    private final RankingRepositorio rankingRepo;
    private final NotaServico notaServico;

    public RankingServico(AlunoRepositorio alunoRepo,
                          SimuladoRepositorio simuladoRepo,
                          RankingRepositorio rankingRepo,
                          NotaServico notaServico) {
        this.alunoRepo = alunoRepo;
        this.simuladoRepo = simuladoRepo;
        this.rankingRepo = rankingRepo;
        this.notaServico = notaServico;
    }

    /** RN-98/99: recalcula e salva (não congela). Se já estiver congelado, devolve o atual. */
    public List<Ranking.Linha> recalcular(SimuladoId simuladoId) {
        var simulado = simuladoRepo.porId(simuladoId);

        if (simulado.getStatus() == Simulado.Status.FINALIZADO || rankingRepo.estaCongelado(simuladoId)) {
            return rankingRepo.carregar(simuladoId); // RN-102
        }

        var pesos = simuladoRepo.pesosDoSimulado(simuladoId);
        var alunos = alunoRepo.porTurma(simulado.getTurma());

        var linhas = calcularPosicoes(alunos, alunos, pesos);
        rankingRepo.limpar(simuladoId);
        rankingRepo.salvarPosicoes(simuladoId, linhas);
        return linhas;
    }

    /** Versão orientada a agregado (opcional). */
    public Ranking recalcularComoAgregado(SimuladoId simuladoId) {
        var simulado = simuladoRepo.porId(simuladoId);

        if (simulado.getStatus() == Simulado.Status.FINALIZADO || rankingRepo.estaCongelado(simuladoId)) {
            return rankingRepo.carregarAgregado(simuladoId)
                    .orElseGet(() -> new Ranking(simuladoId, List.of()));
        }

        var pesos = simuladoRepo.pesosDoSimulado(simuladoId);
        var alunos = alunoRepo.porTurma(simulado.getTurma());

        var linhas = calcularPosicoes(alunos, alunos, pesos);
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

    // ======== cálculo interno ========
    private List<Ranking.Linha> calcularPosicoes(
            List<Aluno> alunos, List<Aluno> todosAlunos, Map<Integer, Double> pesos) {

        var ordenados = alunos.stream()
            .map(a -> new Temp(a, mediaPonderada(a, todosAlunos, pesos)))
            .sorted(Comparator
                    .comparing(Temp::media).reversed()
                    .thenComparing(t -> t.aluno().getDataNascimento()))
            .toList();

        var linhas = new ArrayList<Ranking.Linha>();
        int pos = 1;
        for (var t : ordenados) {
            linhas.add(new Ranking.Linha(t.aluno().getId(), t.media(), pos++));
        }
        return linhas;
    }

    private double mediaPonderada(Aluno aluno, List<Aluno> todosAlunos, Map<Integer, Double> pesos) {
        return notaServico.calcularMediaPonderada(aluno, pesos);
    }

    private record Temp(Aluno aluno, double media) {}
}
