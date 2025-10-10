package dev.com.qnota.dominio.principal.disciplina;

import java.util.Optional;

public interface DisciplinaRepositorio {

    // geração do id fica na infraestrutura
    DisciplinaId proximoId();

    void salvar(Disciplina d);
    Optional<Disciplina> porId(DisciplinaId id);
    void remover(DisciplinaId id);

    // Regras/consultas usadas pelo serviço
    boolean existeNomeNaArea(String nome, String areaNome);      // RN-121
    boolean foiUsadaEmAlgumSimulado(DisciplinaId id);            // RN-44
    boolean foiUsadaEmSimuladoFinalizado(DisciplinaId id);       // RN-62
}
