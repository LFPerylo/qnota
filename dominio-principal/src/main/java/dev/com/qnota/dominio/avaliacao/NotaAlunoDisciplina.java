package dev.com.qnota.dominio.avaliacao;

import java.time.*;
import java.util.Objects;

/**
 * Gerado automaticamente a partir do esquema SQL QNota em 2025-09-28 23:13:19.
 * Estrutura de pacotes: cadastro, ensino, avaliacao.
 * Sem dependências de ORM; apenas POJOs simples.
 */

public class NotaAlunoDisciplina {
    private Integer idNota;
    private java.math.BigDecimal valor; // DECIMAL(4,2)
    private Integer idAluno;
    private Integer idSimulado;
    private Integer idDisciplina;

    public NotaAlunoDisciplina() {}

    public NotaAlunoDisciplina(Integer idNota, java.math.BigDecimal valor, Integer idAluno, Integer idSimulado, Integer idDisciplina) {
        this.idNota = idNota;
        this.valor = valor;
        this.idAluno = idAluno;
        this.idSimulado = idSimulado;
        this.idDisciplina = idDisciplina;
    }

    public Integer getIdNota() { return idNota; }
    public void setIdNota(Integer idNota) { this.idNota = idNota; }

    public java.math.BigDecimal getValor() { return valor; }
    public void setValor(java.math.BigDecimal valor) { this.valor = valor; }

    public Integer getIdAluno() { return idAluno; }
    public void setIdAluno(Integer idAluno) { this.idAluno = idAluno; }

    public Integer getIdSimulado() { return idSimulado; }
    public void setIdSimulado(Integer idSimulado) { this.idSimulado = idSimulado; }

    public Integer getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(Integer idDisciplina) { this.idDisciplina = idDisciplina; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotaAlunoDisciplina)) return false;
        NotaAlunoDisciplina that = (NotaAlunoDisciplina) o;
        return java.util.Objects.equals(idNota, that.idNota);
    }
    @Override public int hashCode() { return java.util.Objects.hash(idNota); }
    @Override public String toString() {
        return "NotaAlunoDisciplina{id=" + idNota + ", valor=" + valor + "}";
    }
}


