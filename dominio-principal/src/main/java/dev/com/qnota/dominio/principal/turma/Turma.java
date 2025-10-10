package dev.com.qnota.dominio.principal.turma;

import java.util.Objects;
import dev.com.qnota.dominio.principal.professor.ProfessorId;

public class Turma {
    private final TurmaId id;
    private String nome;
    private int anoLetivo;
    private boolean ativo;
    private ProfessorId professor;

    public Turma(TurmaId id, String nome, int anoLetivo, boolean ativo, ProfessorId professor) {
        this.id        = Objects.requireNonNull(id, "id não pode ser nulo");
        this.nome      = requireNonBlank(nome, "'nome' não pode ser vazio");
        this.anoLetivo = requireAnoLetivo(anoLetivo);
        this.ativo     = ativo;
        this.professor = Objects.requireNonNull(professor, "professor não pode ser nulo");
    }

    // getters
    public TurmaId getId()              { return id; }
    public String getNome()             { return nome; }
    public int getAnoLetivo()           { return anoLetivo; }
    public boolean isAtivo()            { return ativo; }
    public ProfessorId getProfessor()   { return professor; }

    // operações locais
    public void renomear(String novoNome) {
        this.nome = requireNonBlank(novoNome, "'nome' não pode ser vazio");
    }

    public void mudarProfessor(ProfessorId novoProfessor) {
        this.professor = Objects.requireNonNull(novoProfessor, "professor não pode ser nulo");
    }

    public void inativar() { this.ativo = false; }
    public void ativar()   { this.ativo = true;  }

    public void alterarAnoLetivo(int novoAno) {
        this.anoLetivo = requireAnoLetivo(novoAno);
    }

    // ===== helpers (apenas estes dois) =====
    private static String requireNonBlank(String s, String msg) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException(msg);
        }
        return s.trim();
    }

    private static int requireAnoLetivo(int ano) {
        if (ano <= 0) {
            throw new IllegalArgumentException("anoLetivo deve ser positivo");
        }
        return ano;
    }
}
