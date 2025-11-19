package dev.com.qnota.dominio.principal.aluno;

import dev.com.qnota.dominio.principal.professor.ProfessorId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Value Object que representa uma justificativa para alteração de nota.
 * Agora faz parte do agregado Aluno.
 */
public class Justificativa {

    private final double notaAnterior;
    private final double notaCorrigida;
    private final String texto;
    private final LocalDateTime dataHora;
    private final ProfessorId professor;

    public Justificativa(double notaAnterior, double notaCorrigida, 
                        String texto, LocalDateTime dataHora, ProfessorId professor) {
        this.notaAnterior = notaAnterior;
        this.notaCorrigida = notaCorrigida;
        this.texto = Objects.requireNonNull(texto, "'texto' não pode ser nulo");
        this.dataHora = Objects.requireNonNull(dataHora, "'dataHora' não pode ser nula");
        this.professor = Objects.requireNonNull(professor, "'professor' não pode ser nulo");
    }

    public double getNotaAnterior() {
        return notaAnterior;
    }

    public double getNotaCorrigida() {
        return notaCorrigida;
    }

    public String getTexto() {
        return texto;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public ProfessorId getProfessor() {
        return professor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Justificativa that = (Justificativa) o;
        return Double.compare(that.notaAnterior, notaAnterior) == 0 &&
               Double.compare(that.notaCorrigida, notaCorrigida) == 0 &&
               Objects.equals(texto, that.texto) &&
               Objects.equals(dataHora, that.dataHora) &&
               Objects.equals(professor, that.professor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notaAnterior, notaCorrigida, texto, dataHora, professor);
    }

    @Override
    public String toString() {
        return "Justificativa{" +
                "notaAnterior=" + notaAnterior +
                ", notaCorrigida=" + notaCorrigida +
                ", texto='" + texto + '\'' +
                ", dataHora=" + dataHora +
                ", professor=" + professor +
                '}';
    }
}


