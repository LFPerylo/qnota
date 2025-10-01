package dev.com.qnota.dominio.avaliacao;

import java.time.*;
import java.util.Objects;

/**
 * Gerado automaticamente a partir do esquema SQL QNota em 2025-09-28 23:13:19.
 * Estrutura de pacotes: cadastro, ensino, avaliacao.
 * Sem dependências de ORM; apenas POJOs simples.
 */

/**
 * Associação entre Simulado e Disciplina com 'peso' (PK composta).
 */
public class SimuladoDisciplina {
    private Integer idSimulado;
    private Integer idDisciplina;
    private java.math.BigDecimal peso; // DECIMAL(4,2)

    public SimuladoDisciplina() {}

    public SimuladoDisciplina(Integer idSimulado, Integer idDisciplina, java.math.BigDecimal peso) {
        this.idSimulado = idSimulado;
        this.idDisciplina = idDisciplina;
        this.peso = peso;
    }

    public Integer getIdSimulado() { return idSimulado; }
    public void setIdSimulado(Integer idSimulado) { this.idSimulado = idSimulado; }

    public Integer getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(Integer idDisciplina) { this.idDisciplina = idDisciplina; }

    public java.math.BigDecimal getPeso() { return peso; }
    public void setPeso(java.math.BigDecimal peso) { this.peso = peso; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimuladoDisciplina)) return false;
        SimuladoDisciplina that = (SimuladoDisciplina) o;
        return java.util.Objects.equals(idSimulado, that.idSimulado)
                && java.util.Objects.equals(idDisciplina, that.idDisciplina);
    }
    @Override public int hashCode() { return java.util.Objects.hash(idSimulado, idDisciplina); }
    @Override public String toString() {
        return "SimuladoDisciplina{simulado=" + idSimulado + ", disciplina=" + idDisciplina + ", peso=" + peso + "}";
    }
}

