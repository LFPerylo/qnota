/* Título da análise: QNota - Repositório do Agregado Disciplina (assinaturas usadas pelos serviços) */
package dev.com.qnota.dominio.avaliacao.disciplina;

import java.util.Optional;

public interface DisciplinaRepositorio {
    void salvar(Disciplina d);
    Optional<Disciplina> porId(DisciplinaId id);

    boolean existeNomeNaArea(String nome, String areaNome);
    boolean foiUsadaEmAlgumSimulado(DisciplinaId id);
    boolean foiUsadaEmSimuladoFinalizado(DisciplinaId id);
}
