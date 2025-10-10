package dev.com.qnota.dominio.principal.justificativa;

import java.util.List;
import dev.com.qnota.dominio.principal.nota.NotaId;

public interface JustificativaRepositorio {
    void salvar(Justificativa j);
    List<Justificativa> porNota(NotaId idNota);
}
