/* Título da análise: QNota - Serviço de Aplicação para Professor (RNs 07, 84, 26, 26A, 125) */
package dev.com.qnota.dominio.principal.professor;

import java.util.List;

public class ProfessorServico {
    private final ProfessorRepositorio repo;

    public ProfessorServico(ProfessorRepositorio repo) { this.repo = repo; }

    public void cadastrar(Professor p) {
        if (p.getEspecialidades().isEmpty())
            throw new IllegalArgumentException("RN-84: Professor deve ter ao menos uma especialidade.");
        repo.salvar(p);
    }

    public void validarLimiteDeTurmas(ProfessorId id) {
        if (repo.contarTurmasAtivas(id) > 3) throw new IllegalStateException("RN-07: Até 3 turmas simultâneas.");
    }

    public void removerComSubstituto(ProfessorId aRemover, ProfessorId substituto) {
        if (repo.possuiSimuladoFinalizado(aRemover))
            throw new IllegalStateException("RN-26A: Não pode excluir se houver simulados finalizados vinculados.");
        // RN-125: substituir em todas as turmas e simulados futuros
        repo.substituirProfessor(aRemover, substituto);
    }

    public List<String> areasDoProfessor(ProfessorId id) {
        return repo.nomesDeAreasDoProfessor(id);
    }
}
