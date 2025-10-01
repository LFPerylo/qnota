/* Título da análise: QNota - Serviço de Aplicação para Turma (RNs 06, 08, 10, 93..96) */
package dev.com.qnota.dominio.academico.turma;

import dev.com.qnota.dominio.academico.professor.ProfessorId;

public class TurmaServico {
    private final TurmaRepositorio repo;

    public TurmaServico(TurmaRepositorio repo) { this.repo = repo; }

    public void criar(Turma t) {
        if (repo.existeNomeNoAno(t.getNome(), t.getAnoLetivo()))
            throw new IllegalArgumentException("RN-06: Nome único no ano letivo.");
        repo.salvar(t);
    }

    public void trocarProfessor(TurmaId id, ProfessorId novoProfessor) {
        if (repo.possuiSimuladosFinalizados(id))
            throw new IllegalStateException("RN-10: Não é permitido alterar professor com simulados finalizados.");
        var turma = repo.porId(id).orElseThrow();
        var atualizada = new Turma(turma.getId(), turma.getNome(), turma.getAnoLetivo(), turma.isAtivo(), novoProfessor);
        repo.salvar(atualizada);
    }

    public void inativar(TurmaId id) {
        if (repo.possuiSimuladosEmEdicao(id))
            throw new IllegalStateException("RN-95: Finalize simulados em edição antes de inativar.");
        var turma = repo.porId(id).orElseThrow();
        var atualizada = new Turma(turma.getId(), turma.getNome(), turma.getAnoLetivo(), false, turma.getProfessor());
        repo.salvar(atualizada);
    }

    public void excluir(TurmaId id) {
        if (repo.possuiAlunosAtivos(id) || repo.possuiSimulados(id))
            throw new IllegalStateException("RN-08: Não é possível excluir turma com vínculos.");
        // remoção física deverá ser realizada na implementação do repositório
    }
}
