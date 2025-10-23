package dev.com.qnota.dominio.principal.turma;

import java.util.Optional;

public interface TurmaRepositorio {

    /**
     * Persiste a turma. Se {@code getId()==null}, a infraestrutura deve
     * gerar um novo ID e chamar {@code atribuirIdSeAusente(novoId)} antes de concluir.
     * @return o TurmaId atribuído (novo ou existente)
     */
    TurmaId salvar(Turma t);

    Turma porId(TurmaId id);
    void remover(TurmaId id);

    boolean existeNomeNoAno(String nome, int anoLetivo);
    boolean possuiAlunosAtivos(TurmaId id);
    boolean possuiSimulados(TurmaId id);
    boolean possuiSimuladosEmEdicao(TurmaId id);
    boolean possuiSimuladosFinalizados(TurmaId id);

    // usado por cenários que comparam ano letivo entre turmas
    int anoLetivoDe(TurmaId id);
}
