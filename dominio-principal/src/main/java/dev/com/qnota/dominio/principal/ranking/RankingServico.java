package dev.com.qnota.dominio.principal.ranking;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import dev.com.qnota.dominio.principal.aluno.Aluno;
import dev.com.qnota.dominio.principal.aluno.AlunoRepositorio;
import dev.com.qnota.dominio.principal.simulado.Simulado;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.simulado.SimuladoRepositorio;

public class RankingServico {

    private final AlunoRepositorio alunoRepo;
    private final SimuladoRepositorio simuladoRepo;
    private final RankingRepositorio rankingRepo;

    public RankingServico(AlunoRepositorio alunoRepo,
                          SimuladoRepositorio simuladoRepo,
                          RankingRepositorio rankingRepo) {
        this.alunoRepo = alunoRepo;
        this.simuladoRepo = simuladoRepo;
        this.rankingRepo = rankingRepo;
    }

    /** RN-98/99: recalcula e salva (não congela). Se já estiver congelado, devolve o atual. */
    public List<RankingRepositorio.ItemRanking> recalcular(SimuladoId simuladoId) {
        var simulado = simuladoRepo.porId(simuladoId);

        if (simulado.getStatus() == Simulado.Status.FINALIZADO || rankingRepo.estaCongelado(simuladoId)) {
            return rankingRepo.carregar(simuladoId); // RN-102
        }

        var pesos = simuladoRepo.pesosDoSimulado(simuladoId);
        var alunos = alunoRepo.porTurma(simulado.getTurma());

        var itens = calcularPosicoes(alunos, alunos, pesos);
        rankingRepo.limpar(simuladoId);
        rankingRepo.salvarPosicoes(simuladoId, itens);
        return itens;
    }

    /** Versão orientada a agregado (opcional). Usa os métodos default do repositório. */
    public Ranking recalcularComoAgregado(SimuladoId simuladoId) {
        var simulado = simuladoRepo.porId(simuladoId);

        if (simulado.getStatus() == Simulado.Status.FINALIZADO || rankingRepo.estaCongelado(simuladoId)) {
            return rankingRepo.carregarAgregado(simuladoId).orElseGet(() -> new Ranking(simuladoId, List.of()));
        }

        var pesos = simuladoRepo.pesosDoSimulado(simuladoId);
        var alunos = alunoRepo.porTurma(simulado.getTurma());

        var itens = calcularPosicoes(alunos, alunos, pesos);

        var linhas = itens.stream()
                .map(i -> new Ranking.Linha(i.aluno(), i.media(), i.posicao()))
                .toList();
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
    private List<RankingRepositorio.ItemRanking> calcularPosicoes(
            List<Aluno> alunos, List<Aluno> todosAlunos, Map<Integer, Double> pesos) {

        var ordenados = alunos.stream()
            .map(a -> new Temp(a, mediaPonderada(a, todosAlunos, pesos)))
            .sorted(Comparator
                    .comparing(Temp::media).reversed()
                    .thenComparing(t -> t.aluno().getDataNascimento()))
            .toList();

        var itens = new ArrayList<RankingRepositorio.ItemRanking>();
        int pos = 1;
        for (var t : ordenados) {
            itens.add(new RankingRepositorio.ItemRanking(t.aluno().getId(), t.media(), pos++));
        }
        return itens;
    }

    private double mediaPonderada(Aluno aluno, List<Aluno> todosAlunos, Map<Integer, Double> pesos) {
        double soma = 0, somaPesos = 0;
        
        // Busca todas as notas do aluno nos simulados
        for (var notaDoAluno : aluno.getNotas()) {
            double p = pesos.getOrDefault(notaDoAluno.getDisciplinaId().value(), 0.0);
            soma += notaDoAluno.getValor() * p;
            somaPesos += p;
        }
        
        double media = somaPesos == 0 ? 0 : (soma / somaPesos) * 10.0; // pesos somam 10
        return BigDecimal.valueOf(media).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private record Temp(Aluno aluno, double media) {}
}
