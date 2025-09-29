package dev.com.qnota.dominio.principal.avaliacao;

import java.time.*;
import java.util.Objects;

/**
 * Gerado automaticamente a partir do esquema SQL QNota em 2025-09-28 23:13:19.
 * Estrutura de pacotes: cadastro, ensino, avaliacao.
 * Sem dependências de ORM; apenas POJOs simples.
 */

public class Simulado {
    private Integer idSimulado;
    private java.time.LocalDate dataAplicacao;
    private SimuladoStatus status = SimuladoStatus.RASCUNHO;
    private Integer idTurma; // FK

    public Simulado() {}

    public Simulado(Integer idSimulado, java.time.LocalDate dataAplicacao, SimuladoStatus status, Integer idTurma) {
        this.idSimulado = idSimulado;
        this.dataAplicacao = dataAplicacao;
        this.status = status;
        this.idTurma = idTurma;
    }

    public Integer getIdSimulado() { return idSimulado; }
    public void setIdSimulado(Integer idSimulado) { this.idSimulado = idSimulado; }

    public java.time.LocalDate getDataAplicacao() { return dataAplicacao; }
    public void setDataAplicacao(java.time.LocalDate dataAplicacao) { this.dataAplicacao = dataAplicacao; }

    public SimuladoStatus getStatus() { return status; }
    public void setStatus(SimuladoStatus status) { this.status = status; }

    public Integer getIdTurma() { return idTurma; }
    public void setIdTurma(Integer idTurma) { this.idTurma = idTurma; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Simulado)) return false;
        Simulado that = (Simulado) o;
        return java.util.Objects.equals(idSimulado, that.idSimulado);
    }
    @Override public int hashCode() { return java.util.Objects.hash(idSimulado); }
    @Override public String toString() {
        return "Simulado{id=" + idSimulado + ", data=" + dataAplicacao + ", status=" + status + "}";
    }
}

