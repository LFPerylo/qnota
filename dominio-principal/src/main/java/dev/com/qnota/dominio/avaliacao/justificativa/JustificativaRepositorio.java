/* Título da análise: QNota - Repositório do Agregado Justificativa (assinaturas usadas pelos serviços) */
package dev.com.qnota.dominio.avaliacao.justificativa;

import java.util.List;

import dev.com.qnota.dominio.avaliacao.nota.NotaId;

public interface JustificativaRepositorio {
    void salvar(Justificativa j);
    List<Justificativa> porNota(NotaId idNota);
}
