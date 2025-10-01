package dev.com.qnota.dominio.principal.aluno;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;
import dev.com.qnota.dominio.principal.turma.TurmaId;

public class Aluno {
    private final AlunoId id;
    private String nome;
    private LocalDate dataNascimento;
    private boolean ativo;
    private TurmaId turma;
    private final List<AlunoResponsavel> responsaveis;

    public Aluno(AlunoId id, String nome, LocalDate dataNascimento, boolean ativo, TurmaId turma, List<AlunoResponsavel> responsaveis) {
        this.id = id;
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.ativo = ativo;
        this.turma = turma;
        this.responsaveis = new ArrayList<>(responsaveis == null ? List.of() : responsaveis);
    }

    public AlunoId getId() { return id; }
    public String getNome() { return nome; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public boolean isAtivo() { return ativo; }
    public TurmaId getTurma() { return turma; }
    public List<AlunoResponsavel> getResponsaveis() { return Collections.unmodifiableList(responsaveis); }

    public void inativar() { this.ativo = false; }
    public void ativar() { this.ativo = true; }

    public record AlunoResponsavel(ResponsavelId responsavel, String grauParentesco, boolean principal) {}
}
