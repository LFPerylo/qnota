package dev.com.qnota.dominio.principal.justificativa;

import java.time.LocalDateTime;

import dev.com.qnota.dominio.principal.nota.NotaId;
import dev.com.qnota.dominio.principal.professor.ProfessorId;

public class Justificativa {
    private final JustificativaId id;
    private final NotaId nota;
    private final double notaAnterior;
    private final double notaCorrigida;
    private final String justificativa;
    private final LocalDateTime dataHora;
    private final ProfessorId professor;

    public Justificativa(JustificativaId id, NotaId nota, double notaAnterior, double notaCorrigida, String justificativa, LocalDateTime dataHora, ProfessorId professor) {
        if (justificativa == null || justificativa.trim().length() < 20) {
            throw new IllegalArgumentException("Justificativa deve ter ao menos 20 caracteres.");
        }
        this.id = id;
        this.nota = nota;
        this.notaAnterior = notaAnterior;
        this.notaCorrigida = notaCorrigida;
        this.justificativa = justificativa;
        this.dataHora = dataHora;
        this.professor = professor;
    }

    public JustificativaId getId() { return id; }
    public NotaId getNota() { return nota; }
    public double getNotaAnterior() { return notaAnterior; }
    public double getNotaCorrigida() { return notaCorrigida; }
    public String getJustificativa() { return justificativa; }
    public LocalDateTime getDataHora() { return dataHora; }
    public ProfessorId getProfessor() { return professor; }
}
