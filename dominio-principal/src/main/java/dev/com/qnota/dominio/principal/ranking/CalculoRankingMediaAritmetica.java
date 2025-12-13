// Análise: Strategy concreta baseada em média aritmética simples (ignora pesos)
package dev.com.qnota.dominio.principal.ranking;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import dev.com.qnota.dominio.principal.aluno.Aluno;

/**
 * Implementação da Strategy usando média aritmética simples:
 * - Ignora os pesos das disciplinas
 * - Soma todas as notas e divide pela quantidade
 * - Ordenação decrescente por média
 * - Desempate pela data de nascimento (mais velho primeiro)
 * 
 * Útil quando a instituição deseja que todas as disciplinas
 * tenham o mesmo peso no cálculo do ranking.
 */
public class CalculoRankingMediaAritmetica implements CalculoRankingStrategy {

    @Override
    public List<Ranking.Linha> calcular(List<Aluno> alunos,
                                        Map<Integer, Double> pesos) {
        // Pesos são ignorados nesta estratégia
        
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

    /**
     * Calcula a média aritmética simples das notas do aluno.
     * Soma todas as notas e divide pela quantidade.
     */
    private double calcularMediaAritmetica(Aluno aluno) {
        var notas = aluno.getNotas();
        
        if (notas.isEmpty()) {
            return 0.0;
        }
        
        double soma = notas.stream()
            .mapToDouble(n -> n.getValor())
            .sum();
        
        double media = soma / notas.size();
        
        // Arredonda para 2 casas decimais
        return BigDecimal.valueOf(media)
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
    }

    private record Temp(Aluno aluno, double media) {}
}

