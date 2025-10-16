package dev.com.qnota.dominio.principal.simulado;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import dev.com.qnota.dominio.principal.turma.TurmaId;

public interface SimuladoRepositorio {

    /**
     * Persiste o simulado. Se {@code getId()==null}, a infraestrutura deve
     * gerar um novo ID e chamar {@code atribuirIdSeAusente(novoId)} antes de concluir.
     */
    void salvar(Simulado s);

    Optional<Simulado> porId(SimuladoId id);

    int contarEmEdicaoPorTurma(TurmaId turmaId);
    List<Simulado> listarPorTurma(TurmaId turmaId);

    /** disciplinaId -> peso (ex.: 1 -> 6.0, 2 -> 4.0) — usado no RankingServico */
    Map<Integer, Double> pesosDoSimulado(SimuladoId id);

    /** RN-16: checar se todas as notas da turma foram lançadas */
    boolean todasNotasLancadas(SimuladoId id);
    
    /** RN-15: verificar se existe nota para o simulado */
    boolean existeNotaParaSimulado(SimuladoId id);
    
    /** RN-15: remover simulado */
    void remover(SimuladoId id);
}
