package dev.com.qnota.dominio.principal.turma;

import dev.com.qnota.dominio.principal.professor.ProfessorId;

public class TurmaServico {
    private final TurmaRepositorio repo;

    public TurmaServico(TurmaRepositorio repo) { this.repo = repo; }

    /** RN-06: nome único no ano letivo. */
    public void criar(Turma t) {
        if (repo.existeNomeNoAno(t.getNome(), t.getAnoLetivo()))
            throw new IllegalArgumentException("RN-06: Nome único no ano letivo.");
        repo.salvar(t);
    }

    /** RN-10: não pode alterar professor se houver simulados finalizados. */
    public void trocarProfessor(TurmaId id, ProfessorId novoProfessor) {
        if (repo.possuiSimuladosFinalizados(id))
            throw new IllegalStateException("RN-10: Não é permitido alterar professor com simulados finalizados.");
        var turma = repo.porId(id).orElseThrow();
        turma.mudarProfessor(novoProfessor);   // operação local da entidade
        repo.salvar(turma);
    }

    /** RN-95: não inativa se existirem simulados em edição. */
    public void inativar(TurmaId id) {
        if (repo.possuiSimuladosEmEdicao(id))
            throw new IllegalStateException("RN-95: Finalize simulados em edição antes de inativar.");
        var turma = repo.porId(id).orElseThrow();
        turma.inativar();                       // operação local da entidade
        repo.salvar(turma);
    }

    /** RN-08: não excluir se houver vínculos (alunos ativos ou simulados). */
    public void excluir(TurmaId id) {
        if (repo.possuiAlunosAtivos(id) || repo.possuiSimulados(id))
            throw new IllegalStateException("RN-08: Não é possível excluir turma com vínculos.");
        // remoção física/log é responsabilidade da implementação do repositório
    }
}
