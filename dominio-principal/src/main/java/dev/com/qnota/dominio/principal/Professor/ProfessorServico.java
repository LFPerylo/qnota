package dev.com.qnota.dominio.principal.professor;

import java.util.List;
import java.util.Objects;

public class ProfessorServico {

    private final ProfessorRepositorio repo;

    public ProfessorServico(ProfessorRepositorio repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    /** Cadastro: entidade garante NOT NULL/NOT BLANK e RN-84 (>= 1 especialidade). */
    public void cadastrar(ProfessorId id, String nome, String cpf, String email, List<String> especialidades) {
        var p = new Professor(id, nome, cpf, email, especialidades);
        repo.salvar(p);
    }

    /** Edição de dados de contato (CPF é imutável na entidade). */
    public void atualizarContato(ProfessorId id, String novoNome, String novoEmail) {
        var p = repo.porId(id).orElseThrow(() -> new IllegalStateException("professor não encontrado"));
        p.renomear(novoNome);
        p.alterarEmail(novoEmail);
        repo.salvar(p);
    }

    /** RN-07: no máximo 3 turmas ativas (checagem centralizada no serviço). */
    public void validarLimiteDeTurmas(ProfessorId id) {
        if (repo.contarTurmasAtivas(id) > 3)
            throw new IllegalStateException("RN-07: Até 3 turmas simultâneas.");
    }

    /**
     * Exclusão com substituição:
     * - RN-26A: não pode excluir se houver simulados finalizados associados às turmas do professor.
     * - RN-125: substitui nas turmas e (por política) nos simulados futuros.
     */
    public void removerComSubstituto(ProfessorId aRemover, ProfessorId substituto) {
        if (repo.possuiSimuladoFinalizado(aRemover))
            throw new IllegalStateException("RN-26A: Não pode excluir se houver simulados finalizados vinculados.");
        repo.substituirProfessor(aRemover, substituto); // move vínculos p/ o substituto
    }

    /** Conveniências para especialidades (invariantes ficam na entidade). */
    public void adicionarEspecialidade(ProfessorId id, String area) {
        var p = repo.porId(id).orElseThrow(() -> new IllegalStateException("professor não encontrado"));
        p.adicionarEspecialidade(area);
        repo.salvar(p);
    }

    public void removerEspecialidade(ProfessorId id, String area) {
        var p = repo.porId(id).orElseThrow(() -> new IllegalStateException("professor não encontrado"));
        p.removerEspecialidade(area); // não deixa zerar (RN-84)
        repo.salvar(p);
    }

    public List<String> areasDoProfessor(ProfessorId id) {
        return repo.nomesDeAreasDoProfessor(id);
    }
}
