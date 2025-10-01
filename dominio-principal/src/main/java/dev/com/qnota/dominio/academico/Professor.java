package dev.com.qnota.dominio.academico;

import java.time.*;
import java.util.Objects;

/**
 * Gerado automaticamente a partir do esquema SQL QNota em 2025-09-28 23:13:19.
 * Estrutura de pacotes: cadastro, ensino, avaliacao.
 * Sem dependências de ORM; apenas POJOs simples.
 */

public class Professor {
    private Integer idProfessor;
    private String nome;
    private String cpf;
    private String email;

    public Professor() {}

    public Professor(Integer idProfessor, String nome, String cpf, String email) {
        this.idProfessor = idProfessor;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
    }

    public Integer getIdProfessor() { return idProfessor; }
    public void setIdProfessor(Integer idProfessor) { this.idProfessor = idProfessor; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Professor)) return false;
        Professor that = (Professor) o;
        return Objects.equals(idProfessor, that.idProfessor);
    }
    @Override public int hashCode() { return Objects.hash(idProfessor); }
    @Override public String toString() {
        return "Professor{id=" + idProfessor + ", nome='" + nome + "'}";
    }
}

