package dev.com.qnota.dominio.principal.justificativa;

import java.util.List;
import dev.com.qnota.dominio.principal.nota.NotaId;

public interface JustificativaRepositorio {

    /**
     * Persiste a justificativa. Se {@code getId()==null}, gera/atribui um novo ID
     * (via {@code atribuirIdSeAusente}) e retorna esse ID. Caso contrário, apenas
     * atualiza (append é o caso comum) e retorna o mesmo ID.
     */
    JustificativaId salvar(Justificativa j);

    List<Justificativa> porNota(NotaId idNota);
}
