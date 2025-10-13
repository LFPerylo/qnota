package dev.com.qnota.dominio.principal.professor;

import java.util.List;
import java.util.Optional;

public interface ProfessorRepositorio {

    /**
     * Persiste o professor. Se {@code getId()==null}, a infraestrutura deve
     * gerar um novo ID e chamará {@code atribuirIdSeAusente(novoId)} antes
     * de concluir a persistência.
     */
    void salvar(Professor p);

    Optional<Professor> porId(ProfessorId id);

    // Regras que dependem de Turma/Simulado são consultadas por aqui:
    int contarTurmasAtivas(ProfessorId id);                // RN-07: limite de turmas
    List<String> nomesDeAreasDoProfessor(ProfessorId id);  // apoio p/ compatibilidade de áreas
    boolean possuiSimuladoFinalizado(ProfessorId id);      // RN-26A: bloqueio de exclusão

    void substituirProfessor(ProfessorId antigo, ProfessorId substituto); // RN-125
}
