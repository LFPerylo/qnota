package dev.com.qnota.dominio.principal.justificativa;

import java.time.LocalDateTime;
import java.util.Objects;

import dev.com.qnota.dominio.principal.nota.NotaId;
import dev.com.qnota.dominio.principal.professor.ProfessorId;

public class Justificativa {

    private final JustificativaId id;
    private final NotaId nota;
    private final double notaAnterior;
    private final double notaCorrigida;
    private final String justificativa;     // texto
    private final LocalDateTime dataHora;
    private final ProfessorId professor;

    public Justificativa(JustificativaId id,
                         NotaId nota,
                         double notaAnterior,
                         double notaCorrigida,
                         String justificativa,
                         LocalDateTime dataHora,
                         ProfessorId professor) {

        this.id        = Objects.requireNonNull(id,        "'id' não pode ser nulo");
        this.nota      = Objects.requireNonNull(nota,      "'nota' não pode ser nula");
        this.professor = Objects.requireNonNull(professor, "'professor' não pode ser nulo");
        this.dataHora  = Objects.requireNonNull(dataHora,  "'dataHora' não pode ser nula");

        if (notaAnterior < 0 || notaAnterior > 10)
            throw new IllegalArgumentException("notaAnterior fora do intervalo 0..10");
        if (notaCorrigida < 0 || notaCorrigida > 10)
            throw new IllegalArgumentException("notaCorrigida fora do intervalo 0..10");
        if (Double.compare(notaAnterior, notaCorrigida) == 0)
            throw new IllegalArgumentException("não houve alteração de nota");

        String txt = (justificativa == null) ? "" : justificativa.trim();
        if (txt.length() < 20)
            throw new IllegalArgumentException("Justificativa deve ter ao menos 20 caracteres.");

        this.notaAnterior  = notaAnterior;
        this.notaCorrigida = notaCorrigida;
        this.justificativa = txt;
    }

    // getters
    public JustificativaId getId()          { return id; }
    public NotaId getNota()                 { return nota; }
    public double getNotaAnterior()         { return notaAnterior; }
    public double getNotaCorrigida()        { return notaCorrigida; }
    public String getJustificativa()        { return justificativa; }
    public LocalDateTime getDataHora()      { return dataHora; }
    public ProfessorId getProfessor()       { return professor; }
}
