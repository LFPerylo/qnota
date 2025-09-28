package dev.com.qnota.dominio.principal.avaliacao;

import java.time.*;
import java.util.Objects;

/**
 * Gerado automaticamente a partir do esquema SQL QNota em 2025-09-28 23:13:19.
 * Estrutura de pacotes: cadastro, ensino, avaliacao.
 * Sem dependências de ORM; apenas POJOs simples.
 */

public class Aluno {
    private Integer idAluno;
    private String nome;
    private LocalDate dataNascimento;
    private Boolean ativo = Boolean.TRUE;
    private Integer idTurma; // FK

    public Aluno() {}

    public Aluno(Integer idAluno, String nome, LocalDate dataNascimento, Boolean ativo, Integer idTurma) {
        this.idAluno = idAluno;
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.ativo = ativo;
        this.idTurma = idTurma;
    }

    public Integer getIdAluno() { return idAluno; }
    public void setIdAluno(Integer idAluno) { this.idAluno = idAluno; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public Integer getIdTurma() { return idTurma; }
    public void setIdTurma(Integer idTurma) { this.idTurma = idTurma; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Aluno)) return false;
        Aluno that = (Aluno) o;
        return Objects.equals(idAluno, that.idAluno);
    }
    @Override public int hashCode() { return Objects.hash(idAluno); }
    @Override public String toString() {
        return "Aluno{id=" + idAluno + ", nome='" + nome + "'}";
    }
}

