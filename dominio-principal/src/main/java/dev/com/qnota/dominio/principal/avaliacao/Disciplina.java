package dev.com.qnota.dominio.principal.avaliacao;

import java.time.*;
import java.util.Objects;

/**
 * Gerado automaticamente a partir do esquema SQL QNota em 2025-09-28 23:13:19.
 * Estrutura de pacotes: cadastro, ensino, avaliacao.
 * Sem dependências de ORM; apenas POJOs simples.
 */

public class Disciplina {
    private Integer idDisciplina;
    private String nome;

    public Disciplina() {}

    public Disciplina(Integer idDisciplina, String nome) {
        this.idDisciplina = idDisciplina;
        this.nome = nome;
    }

    public Integer getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(Integer idDisciplina) { this.idDisciplina = idDisciplina; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Disciplina)) return false;
        Disciplina that = (Disciplina) o;
        return Objects.equals(idDisciplina, that.idDisciplina);
    }
    @Override public int hashCode() { return Objects.hash(idDisciplina); }
    @Override public String toString() {
        return "Disciplina{id=" + idDisciplina + ", nome='" + nome + "'}";
    }
}
