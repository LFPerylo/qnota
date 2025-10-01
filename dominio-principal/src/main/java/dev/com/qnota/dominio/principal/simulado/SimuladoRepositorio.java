package dev.com.qnota.dominio.principal.simulado;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import dev.com.qnota.dominio.principal.turma.TurmaId;

public interface SimuladoRepositorio {
    void salvar(Simulado s);
    Optional<Simulado> porId(SimuladoId id);
    int contarEmEdicaoPorTurma(TurmaId turmaId);
    List<Simulado> listarPorTurma(TurmaId turmaId);

    /** disciplinaId -> peso (ex.: 1 -> 6.0, 2 -> 4.0) */
    Map<Integer, Double> pesosDoSimulado(SimuladoId id);

    /** RN-16: checar se todas as notas da turma foram lançadas */
    boolean todasNotasLancadas(SimuladoId id);
}
