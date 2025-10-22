package dev.com.qnota.dominio.principal.aluno;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    boolean temNotasPendentesEmSimuladosEmEdicao(AlunoId alunoId);
    boolean temNotas(AlunoId alunoId);

    // usado no serviço p/ RN-57.1
    boolean possuiSimuladoFinalizado(AlunoId alunoId);

    // opcional — serviço atual não precisa
    void alterarTurma(AlunoId alunoId, TurmaId novaTurmaId);
}
