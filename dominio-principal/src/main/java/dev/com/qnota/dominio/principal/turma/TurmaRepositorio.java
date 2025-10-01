/* Título da análise: QNota - Repositório do Agregado Turma (assinaturas usadas pelos serviços) */
package dev.com.qnota.dominio.principal.turma;

import java.util.Optional;

public interface TurmaRepositorio {
    void salvar(Turma t);
    Optional<Turma> porId(TurmaId id);

    boolean existeNomeNoAno(String nome, int anoLetivo);
    boolean possuiAlunosAtivos(TurmaId id);
    boolean possuiSimulados(TurmaId id);
    boolean possuiSimuladosEmEdicao(TurmaId id);
    boolean possuiSimuladosFinalizados(TurmaId id);
}
