package dev.com.qnota.dominio.principal.turma;

import java.util.Optional;

public interface TurmaRepositorio {
    void salvar(Turma t);
    Optional<Turma> porId(TurmaId id);
    void remover(TurmaId id);

    boolean existeNomeNoAno(String nome, int anoLetivo);
    boolean possuiAlunosAtivos(TurmaId id);
    boolean possuiSimulados(TurmaId id);
    boolean possuiSimuladosEmEdicao(TurmaId id);
    boolean possuiSimuladosFinalizados(TurmaId id);

    // usado por cenários que comparam ano letivo entre turmas
    int anoLetivoDe(TurmaId id);
}
