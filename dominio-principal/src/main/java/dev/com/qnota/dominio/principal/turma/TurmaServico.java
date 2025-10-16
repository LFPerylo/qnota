package dev.com.qnota.dominio.principal.turma;

import dev.com.qnota.dominio.principal.professor.ProfessorId;
import dev.com.qnota.dominio.principal.professor.ProfessorRepositorio;

public class TurmaServico {
    private final TurmaRepositorio repo;
    private final ProfessorRepositorio professorRepo;

    public TurmaServico(TurmaRepositorio repo, ProfessorRepositorio professorRepo) { 
        this.repo = repo; 
        this.professorRepo = professorRepo;
    }

    /** Factory de conveniência para criar sem expor ID. */
    public void criar(String nome, int anoLetivo, ProfessorId professor) {
        if (repo.existeNomeNoAno(nome, anoLetivo))
            throw new IllegalArgumentException("RN-06: Nome único no ano letivo.");
        
        // RN-07: professor pode ter no máximo 3 turmas ativas
        if (professorRepo.contarTurmasAtivas(professor) >= 3)
            throw new IllegalStateException("RN-07: Professor já possui 3 turmas ativas.");
            
        var t = new Turma(nome, anoLetivo, true, professor);
        repo.salvar(t); // repo atribui o ID se estiver nulo
    }

    /** Mantido por compatibilidade: aceita a entidade (id deve estar nulo). */
    public void criar(Turma t) {
        if (repo.existeNomeNoAno(t.getNome(), t.getAnoLetivo()))
            throw new IllegalArgumentException("RN-06: Nome único no ano letivo.");
        
        // RN-07: professor pode ter no máximo 3 turmas ativas
        if (professorRepo.contarTurmasAtivas(t.getProfessor()) >= 3)
            throw new IllegalStateException("RN-07: Professor já possui 3 turmas ativas.");
            
        repo.salvar(t);
    }

    /** RN-10: não pode alterar professor se houver simulados finalizados. */
    public void trocarProfessor(TurmaId id, ProfessorId novoProfessor) {
        if (repo.possuiSimuladosFinalizados(id))
            throw new IllegalStateException("RN-10: Não é permitido alterar professor com simulados finalizados.");
        var turma = repo.porId(id).orElseThrow();
        turma.mudarProfessor(novoProfessor);
        repo.salvar(turma);
    }

    /** RN-95: não inativa se existirem simulados em edição. */
    public void inativar(TurmaId id) {
        if (repo.possuiSimuladosEmEdicao(id))
            throw new IllegalStateException("RN-95: Finalize simulados em edição antes de inativar.");
        var turma = repo.porId(id).orElseThrow();
        turma.inativar();
        repo.salvar(turma);
    }

    /** RN-08: não excluir se houver vínculos (alunos ativos ou simulados). */
    public void excluir(TurmaId id) {
        if (repo.possuiAlunosAtivos(id) || repo.possuiSimulados(id))
            throw new IllegalStateException("RN-08: Não é possível excluir turma com vínculos.");
        repo.remover(id);
    }
}
