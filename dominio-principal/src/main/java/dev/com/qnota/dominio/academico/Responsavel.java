package dev.com.qnota.dominio.academico;

import java.time.*;
import java.util.Objects;

/**
 * Gerado automaticamente a partir do esquema SQL QNota em 2025-09-28 23:13:19.
 * Estrutura de pacotes: cadastro, ensino, avaliacao.
 * Sem dependências de ORM; apenas POJOs simples.
 */

public class Responsavel {
    private Integer idResponsavel;
    private String nome;
    private String cpf;
    private String email;

    public Responsavel() {}

    public Responsavel(Integer idResponsavel, String nome, String cpf, String email) {
        this.idResponsavel = idResponsavel;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
    }

    public Integer getIdResponsavel() { return idResponsavel; }
    public void setIdResponsavel(Integer idResponsavel) { this.idResponsavel = idResponsavel; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Responsavel)) return false;
        Responsavel that = (Responsavel) o;
        return Objects.equals(idResponsavel, that.idResponsavel);
    }
    @Override public int hashCode() { return Objects.hash(idResponsavel); }
    @Override public String toString() {
        return "Responsavel{id=" + idResponsavel + ", nome='" + nome + "'}";
    }
}

