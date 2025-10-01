/* Título da análise: QNota - Repositório do Agregado Responsavel (assinaturas usadas pelos serviços) */
package dev.com.qnota.dominio.academico.responsavel;

import java.util.Optional;

public interface ResponsavelRepositorio {
    void salvar(Responsavel r);
    Optional<Responsavel> porId(ResponsavelId id);
    boolean cpfExiste(String cpf);
    boolean estaVinculadoAAlgumAluno(ResponsavelId id);
}
