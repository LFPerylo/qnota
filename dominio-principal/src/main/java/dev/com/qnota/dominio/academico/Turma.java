package dev.com.qnota.dominio.academico;

import java.time.*;
import java.util.Objects;

/**
 * Gerado automaticamente a partir do esquema SQL QNota em 2025-09-28 23:13:19.
 * Estrutura de pacotes: cadastro, ensino, avaliacao.
 * Sem dependências de ORM; apenas POJOs simples.
 */

public class Turma {
    private Integer idTurma;
    private String nome;
    private Integer anoLetivo;
    private Boolean ativo = Boolean.TRUE;
    private Integer idProfessor; // FK

    public Turma() {}

    public Turma(Integer idTurma, String nome, Integer anoLetivo, Boolean ativo, Integer idProfessor) {
        this.idTurma = idTurma;
        this.nome = nome;
        this.anoLetivo = anoLetivo;
        this.ativo = ativo;
        this.idProfessor = idProfessor;
    }

    public Integer getIdTurma() { return idTurma; }
    public void setIdTurma(Integer idTurma) { this.idTurma = idTurma; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Integer getAnoLetivo() { return anoLetivo; }
    public void setAnoLetivo(Integer anoLetivo) { this.anoLetivo = anoLetivo; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public Integer getIdProfessor() { return idProfessor; }
    public void setIdProfessor(Integer idProfessor) { this.idProfessor = idProfessor; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Turma)) return false;
        Turma that = (Turma) o;
        return Objects.equals(idTurma, that.idTurma);
    }
    @Override public int hashCode() { return Objects.hash(idTurma); }
    @Override public String toString() {
        return "Turma{id=" + idTurma + ", nome='" + nome + "', anoLetivo=" + anoLetivo + "}";
    }
}

