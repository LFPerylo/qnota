package dev.com.qnota.dominio.principal.cadastro;

import java.time.*;
import java.util.Objects;

/**
 * Gerado automaticamente a partir do esquema SQL QNota em 2025-09-28 23:13:19.
 * Estrutura de pacotes: cadastro, ensino, avaliacao.
 * Sem dependências de ORM; apenas POJOs simples.
 */

public class Justificativa {
    private Integer idJustificativa;
    private String texto;
    private java.time.LocalDateTime dataHora; // TIMESTAMP
    private Integer idAluno;
    private Integer idSimulado;
    private Integer idProfessor;

    public Justificativa() {}

    public Justificativa(Integer idJustificativa, String texto, java.time.LocalDateTime dataHora,
                         Integer idAluno, Integer idSimulado, Integer idProfessor) {
        this.idJustificativa = idJustificativa;
        this.texto = texto;
        this.dataHora = dataHora;
        this.idAluno = idAluno;
        this.idSimulado = idSimulado;
        this.idProfessor = idProfessor;
    }

    public Integer getIdJustificativa() { return idJustificativa; }
    public void setIdJustificativa(Integer idJustificativa) { this.idJustificativa = idJustificativa; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public java.time.LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(java.time.LocalDateTime dataHora) { this.dataHora = dataHora; }

    public Integer getIdAluno() { return idAluno; }
    public void setIdAluno(Integer idAluno) { this.idAluno = idAluno; }

    public Integer getIdSimulado() { return idSimulado; }
    public void setIdSimulado(Integer idSimulado) { this.idSimulado = idSimulado; }

    public Integer getIdProfessor() { return idProfessor; }
    public void setIdProfessor(Integer idProfessor) { this.idProfessor = idProfessor; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Justificativa)) return false;
        Justificativa that = (Justificativa) o;
        return java.util.Objects.equals(idJustificativa, that.idJustificativa);
    }
    @Override public int hashCode() { return java.util.Objects.hash(idJustificativa); }
    @Override public String toString() {
        return "Justificativa{id=" + idJustificativa + "}";
    }
}

