// Análise: Strategy concreta para cálculo de ranking usando média aritmética
package dev.com.qnota.dominio.principal.ranking;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dev.com.qnota.dominio.principal.aluno.Aluno;
import dev.com.qnota.dominio.principal.aluno.NotaServico;

/**
 * Implementação da Strategy usando média aritmética simples:
 * - média aritmética (soma das notas / quantidade de notas)
 * - ordenação decrescente por média
 * - desempate pela data de nascimento
 */
public class CalculoRankingMediaAritmetica implements CalculoRankingStrategy {

    private final NotaServico notaServico;

    public CalculoRankingMediaAritmetica(NotaServico notaServico) {
        this.notaServico = Objects.requireNonNull(notaServico);
    }

    @Override
    public List<Ranking.Linha> calcular(List<Aluno> alunos,
                                        Map<Integer, Double> pesos) {

        var ordenados = alunos.stream()
            .map(a -> new Temp(a, calcularMediaAritmetica(a)))
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

    private double calcularMediaAritmetica(Aluno aluno) {
        var notas = aluno.getNotas();
        if (notas.isEmpty()) {
            return 0.0;
        }
        
        double soma = notas.stream()
            .mapToDouble(n -> n.getValor())
            .sum();
        
        double media = soma / notas.size();
        return BigDecimal.valueOf(media).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private record Temp(Aluno aluno, double media) {}
}

