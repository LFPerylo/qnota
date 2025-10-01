/* Título da análise: QNota - Repositório do Agregado Professor (assinaturas usadas pelos serviços) */
package dev.com.qnota.dominio.academico.professor;

import java.util.List;
import java.util.Optional;

public interface ProfessorRepositorio {
    void salvar(Professor p);
    Optional<Professor> porId(ProfessorId id);
    int contarTurmasAtivas(ProfessorId id);
    List<String> nomesDeAreasDoProfessor(ProfessorId id); // para RN-53 (compatibilidade com disciplinas do simulado)
    boolean possuiSimuladoFinalizado(ProfessorId id);     // via turmas do professor
    void substituirProfessor(ProfessorId antigo, ProfessorId substituto); // RN-125
}
