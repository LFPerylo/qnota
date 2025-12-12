package dev.com.qnota.dominio.principal.aluno;

import java.time.LocalDate;
import java.util.List;

import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.turma.TurmaId;

public interface AlunoRepositorio {
    /** Persiste o agregado. Se getId()==null, gera/atribui um novo ID e retorna. */
    AlunoId salvar(Aluno aluno);

    Aluno porId(AlunoId id);
    void remover(AlunoId id);

    boolean existeOutroComMesmoNomeENascimentoNaTurma(String nome, LocalDate data, TurmaId turmaId);
    int contarResponsaveis(AlunoId id);
    boolean existeVinculo(AlunoId id);

    List<Aluno> porTurma(TurmaId turmaId);

    // opcional — serviço atual não precisa
    void alterarTurma(AlunoId alunoId, TurmaId novaTurmaId);
    
    // ===== operações de nota (agora parte do agregado Aluno) =====
    
    /**
     * Verifica se o aluno possui notas lançadas.
     */
    boolean temNotas(AlunoId alunoId);
    
    /**
     * Verifica se existe nota para o simulado especificado.
     */
    boolean existeNotaParaSimulado(SimuladoId simuladoId);
}
