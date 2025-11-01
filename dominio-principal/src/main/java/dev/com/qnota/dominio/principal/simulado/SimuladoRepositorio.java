package dev.com.qnota.dominio.principal.simulado;

import java.util.List;
import java.util.Map;

import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.professor.ProfessorId;
import dev.com.qnota.dominio.principal.turma.TurmaId;

public interface SimuladoRepositorio {

    /**
     * Persiste o simulado. Se {@code getId()==null}, a infraestrutura deve
     * gerar um novo ID e chamar {@code atribuirIdSeAusente(novoId)} antes de concluir.
     * @return o SimuladoId atribuído (novo ou existente)
     */
    SimuladoId salvar(Simulado s);

    Simulado porId(SimuladoId id);

    int contarEmEdicaoPorTurma(TurmaId turmaId);
    List<Simulado> listarPorTurma(TurmaId turmaId);

    /** disciplinaId -> peso (ex.: 1 -> 6.0, 2 -> 4.0) — usado no RankingServico */
    Map<Integer, Double> pesosDoSimulado(SimuladoId id);

    /** RN-16: checar se todas as notas da turma foram lançadas */
    boolean todasNotasLancadas(SimuladoId id);

    /** RN-26A: verificar se professor possui simulados finalizados */
    boolean possuiSimuladoFinalizadoParaProfessor(ProfessorId professorId);
    
    /** Verifica se o aluno possui simulados finalizados */
    boolean possuiSimuladoFinalizadoParaAluno(AlunoId alunoId);
    
    /** Verifica se o aluno possui notas pendentes em simulados em edição */
    boolean temNotasPendentesEmSimuladosEmEdicao(AlunoId alunoId);

    /** RN-15: remover simulado */
    void remover(SimuladoId id);
}
