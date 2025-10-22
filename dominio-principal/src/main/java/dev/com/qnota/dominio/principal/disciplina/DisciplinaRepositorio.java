package dev.com.qnota.dominio.principal.disciplina;

import java.util.Optional;

public interface DisciplinaRepositorio {

    /**
     * Persiste a disciplina. Se {@code getId()==null}, gera e atribui um novo ID
     * (via {@code atribuirIdSeAusente}) e retorna esse ID. Caso contrário, atualiza
     * o registro existente e retorna o mesmo ID.
     */
    DisciplinaId salvar(Disciplina d);

    Disciplina porId(DisciplinaId id);
    void remover(DisciplinaId id);

    // Regras/consultas usadas pelo serviço
    boolean existeNomeNaArea(String nome, String areaNome);      // RN-121
    boolean foiUsadaEmAlgumSimulado(DisciplinaId id);            // RN-44
    boolean foiUsadaEmSimuladoFinalizado(DisciplinaId id);       // RN-62
}
