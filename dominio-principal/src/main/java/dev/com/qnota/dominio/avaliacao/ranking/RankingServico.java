package dev.com.qnota.dominio.avaliacao.ranking;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import dev.com.qnota.dominio.academico.aluno.Aluno;
import dev.com.qnota.dominio.academico.aluno.AlunoRepositorio;
import dev.com.qnota.dominio.avaliacao.nota.Nota;
import dev.com.qnota.dominio.avaliacao.nota.NotaRepositorio;
import dev.com.qnota.dominio.avaliacao.simulado.Simulado;
import dev.com.qnota.dominio.avaliacao.simulado.SimuladoId;
import dev.com.qnota.dominio.avaliacao.simulado.SimuladoRepositorio;

public class RankingServico {

    private final NotaRepositorio notaRepo;
    private final AlunoRepositorio alunoRepo;
    private final SimuladoRepositorio simuladoRepo;
    private final RankingRepositorio rankingRepo;

    public RankingServico(NotaRepositorio notaRepo,
                          AlunoRepositorio alunoRepo,
                          SimuladoRepositorio simuladoRepo,
                          RankingRepositorio rankingRepo) {
        this.notaRepo = notaRepo;
        this.alunoRepo = alunoRepo;
        this.simuladoRepo = simuladoRepo;
        this.rankingRepo = rankingRepo;
    }

    /** RN-98/99: recalcula e salva (nao congela). Se já estiver congelado, apenas devolve o atual. */
    public List<RankingRepositorio.ItemRanking> recalcular(SimuladoId simuladoId) {
        var simulado = simuladoRepo.porId(simuladoId).orElseThrow();

        if (simulado.getStatus() == Simulado.Status.FINALIZADO || rankingRepo.estaCongelado(simuladoId)) {
            return rankingRepo.carregar(simuladoId); // RN-102: não recalcula ranking congelado
        }

        var pesos = simuladoRepo.pesosDoSimulado(simuladoId);                // disciplinaId -> peso
        var notas = notaRepo.porSimulado(simuladoId);                        // todas as notas do simulado
        var alunos = alunoRepo.porTurma(simulado.getTurma());               // todos alunos da turma

        var itens = calcularPosicoes(alunos, notas, pesos);
        rankingRepo.limpar(simuladoId);
        rankingRepo.salvarPosicoes(simuladoId, itens);
        return itens;
    }

    /** RN-102: congela o ranking após finalização do simulado. */
    public void congelar(SimuladoId simuladoId) {
        if (!rankingRepo.estaCongelado(simuladoId)) {
            // garante que existe um cálculo salvo antes de congelar
            recalcular(simuladoId);
            rankingRepo.congelar(simuladoId);
        }
    }

    // ======== cálculo interno ========
    private List<RankingRepositorio.ItemRanking> calcularPosicoes(
            List<Aluno> alunos, List<Nota> notas, Map<Integer, Double> pesos) {

        // média ponderada (0..10) e desempate por aluno mais velho
        var ordenados = alunos.stream()
            .map(a -> new Temp(a, mediaPonderada(a, notas, pesos)))
            .sorted(Comparator
                    .comparing(Temp::media).reversed()
                    .thenComparing(t -> t.aluno().getDataNascimento()))
            .toList();

        // atribui posições 1..N
        var itens = new ArrayList<RankingRepositorio.ItemRanking>();
        int pos = 1;
        for (var t : ordenados) {
            itens.add(new RankingRepositorio.ItemRanking(t.aluno().getId(), t.media(), pos++));
        }
        return itens;
    }

    private double mediaPonderada(Aluno aluno, List<Nota> notas, Map<Integer, Double> pesos) {
        double soma = 0, somaPesos = 0;
        for (var n : notas) {
            if (n.getAluno().equals(aluno.getId())) {
                double p = pesos.getOrDefault(n.getDisciplina().value(), 0.0);
                soma += n.getValor() * p;
                somaPesos += p;
            }
        }
        double media = somaPesos == 0 ? 0 : (soma / somaPesos) * 10.0; // pesos somam 10
        return BigDecimal.valueOf(media).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private record Temp(Aluno aluno, double media) {}
}
