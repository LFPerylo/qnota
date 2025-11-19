// Análise: Strategy concreta baseada na regra atual de média ponderada
package dev.com.qnota.dominio.principal.ranking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dev.com.qnota.dominio.principal.aluno.Aluno;
import dev.com.qnota.dominio.principal.aluno.NotaServico;

/**
 * Implementação da Strategy usando a regra de negócio atual:
 * - média ponderada
 * - ordenação decrescente por média
 * - desempate pela data de nascimento
 */
public class CalculoRankingMediaPonderada implements CalculoRankingStrategy {

    private final NotaServico notaServico;

    public CalculoRankingMediaPonderada(NotaServico notaServico) {
        this.notaServico = Objects.requireNonNull(notaServico);
    }

    @Override
    public List<Ranking.Linha> calcular(List<Aluno> alunos,
                                        Map<Integer, Double> pesos) {

        var ordenados = alunos.stream()
            .map(a -> new Temp(a, notaServico.calcularMediaPonderada(a, pesos)))
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

    private record Temp(Aluno aluno, double media) {}
}
