package dev.com.qnota.dominio.principal.professor;

import java.util.List;
import java.util.Optional;

public interface ProfessorRepositorio {

    /**
     * Persiste o professor. Se {@code getId()==null}, a infraestrutura/ORM deve
     * gerar um novo ID, chamar {@code atribuirIdSeAusente(novoId)} e retornar o ID.
     * Caso contrário, atualiza o registro e retorna o mesmo ID.
     */
    ProfessorId salvar(Professor p);

    Professor porId(ProfessorId id);

    // Regras que dependem de Turma/Simulado:
    int contarTurmasAtivas(ProfessorId id);                // RN-07
    List<String> nomesDeAreasDoProfessor(ProfessorId id);  // apoio/compat
    boolean possuiSimuladoFinalizado(ProfessorId id);      // RN-26A

    void substituirProfessor(ProfessorId antigo, ProfessorId substituto); // RN-125
}
