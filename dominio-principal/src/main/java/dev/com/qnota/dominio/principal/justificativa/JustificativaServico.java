package dev.com.qnota.dominio.principal.justificativa;

import java.time.LocalDateTime;
import java.util.Objects;

import dev.com.qnota.dominio.principal.nota.NotaId;
import dev.com.qnota.dominio.principal.professor.ProfessorId;

public class JustificativaServico {

    private final JustificativaRepositorio repo;

    public JustificativaServico(JustificativaRepositorio repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    /**
     * RN-37..39: cria a justificativa com data/hora atual e retorna o ID gerado pela ORM.
     */
    public JustificativaId registrar(NotaId nota,
                                     double anterior,
                                     double corrigida,
                                     String texto,
                                     ProfessorId professor) {

        var j = new Justificativa(
                nota,
                anterior,
                corrigida,
                texto,
                LocalDateTime.now(),
                professor
        );
        return repo.salvar(j); // ORM atribui e repositório retorna o JustificativaId
    }
}
