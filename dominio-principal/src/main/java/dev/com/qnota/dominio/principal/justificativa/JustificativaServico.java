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
     * RN-37..39: para retificação de nota, exige justificativa >= 20 caracteres,
     * registra data/hora e professor responsável. Demais invariantes na entidade.
     */
    public void registrar(NotaId nota,
                          double anterior,
                          double corrigida,
                          String texto,
                          ProfessorId professor) {

        var j = new Justificativa(nota, anterior, corrigida, texto, LocalDateTime.now(), professor);
        repo.salvar(j); // repositório gera e atribui o ID
    }
}
