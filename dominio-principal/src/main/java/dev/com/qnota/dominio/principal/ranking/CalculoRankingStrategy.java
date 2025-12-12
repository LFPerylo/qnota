// Análise: Aplicação do padrão Strategy ao cálculo do ranking
package dev.com.qnota.dominio.principal.ranking;

import java.util.List;
import java.util.Map;

import dev.com.qnota.dominio.principal.aluno.Aluno;

/**
 * Strategy para cálculo das linhas do ranking.
 * Encapsula o algoritmo de negócio responsável por:
 * - calcular médias
 * - ordenar alunos
 * - gerar posições
 */
public interface CalculoRankingStrategy {

    List<Ranking.Linha> calcular(List<Aluno> alunos,
                                 Map<Integer, Double> pesos);
}
