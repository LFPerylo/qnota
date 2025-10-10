package dev.com.qnota.dominio.principal.professor;

import java.util.List;
import java.util.Optional;

public interface ProfessorRepositorio {
    void salvar(Professor p);
    Optional<Professor> porId(ProfessorId id);

    // Regras que dependem de Turma/Simulado são consultadas por aqui:
    int contarTurmasAtivas(ProfessorId id);      // RN-07: limite de turmas
    List<String> nomesDeAreasDoProfessor(ProfessorId id); // apoio p/ compatibilidade de áreas
    boolean possuiSimuladoFinalizado(ProfessorId id);     // RN-26A: bloqueio de exclusão

    void substituirProfessor(ProfessorId antigo, ProfessorId substituto); // RN-125
}
