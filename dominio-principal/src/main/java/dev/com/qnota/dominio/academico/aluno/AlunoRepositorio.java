/* Título da análise: QNota - Repositório do Agregado Aluno (assinaturas usadas pelos serviços) */
package dev.com.qnota.dominio.academico.aluno;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import dev.com.qnota.dominio.academico.turma.TurmaId;

public interface AlunoRepositorio {
    void salvar(Aluno aluno);
    Optional<Aluno> porId(AlunoId id);
    void remover(AlunoId id);

    boolean existeOutroComMesmoNomeENascimentoNaTurma(String nome, LocalDate data, TurmaId turmaId);
    int contarResponsaveis(AlunoId id);
    boolean existeVinculo(AlunoId id); // usado para RN-04 (não excluir se tiver notas/simulados)

    List<Aluno> porTurma(TurmaId turmaId);

    // RN-67: há notas pendentes para este aluno em simulados EM_EDICAO da turma atual?
    boolean temNotasPendentesEmSimuladosEmEdicao(AlunoId alunoId);

    // RN-04: aluno tem alguma nota registrada?
    boolean temNotas(AlunoId alunoId);
}
