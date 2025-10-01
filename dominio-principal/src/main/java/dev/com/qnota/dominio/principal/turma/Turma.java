package dev.com.qnota.dominio.principal.turma;

import dev.com.qnota.dominio.principal.professor.ProfessorId;

public class Turma {
    private final TurmaId id;
    private String nome;
    private int anoLetivo;
    private boolean ativo;
    private ProfessorId professor;

    public Turma(TurmaId id, String nome, int anoLetivo, boolean ativo, ProfessorId professor) {
        this.id = id;
        this.nome = nome;
        this.anoLetivo = anoLetivo;
        this.ativo = ativo;
        this.professor = professor;
    }

    public TurmaId getId() { return id; }
    public String getNome() { return nome; }
    public int getAnoLetivo() { return anoLetivo; }
    public boolean isAtivo() { return ativo; }
    public ProfessorId getProfessor() { return professor; }

    public void inativar() { this.ativo = false; }
}
