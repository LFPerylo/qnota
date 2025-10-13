package dev.com.qnota.dominio.principal.turma;

import java.util.Objects;
import dev.com.qnota.dominio.principal.professor.ProfessorId;

public class Turma {

    // ID gerado pelo repositório/infra
    private TurmaId id;

    private String nome;
    private int anoLetivo;
    private boolean ativo;
    private ProfessorId professor;

    /** Constrói uma turma sem ID (infra atribui depois). */
    public Turma(String nome, int anoLetivo, boolean ativo, ProfessorId professor) {
        this.nome      = requireNonBlank(nome, "'nome' não pode ser vazio");
        this.anoLetivo = requireAnoLetivo(anoLetivo);
        this.ativo     = ativo;
        this.professor = Objects.requireNonNull(professor, "professor não pode ser nulo");
    }

    /** Infra chama para fixar o ID gerado. Não permite reatribuição divergente. */
    public void atribuirIdSeAusente(TurmaId novoId) {
        Objects.requireNonNull(novoId, "'id' não pode ser nulo");
        if (this.id != null && !this.id.equals(novoId)) {
            throw new IllegalStateException("ID já atribuído para esta turma");
        }
        this.id = novoId;
    }

    // getters
    public TurmaId getId()            { return id; }
    public String getNome()           { return nome; }
    public int getAnoLetivo()         { return anoLetivo; }
    public boolean isAtivo()          { return ativo; }
    public ProfessorId getProfessor() { return professor; }

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

    // ===== helpers =====
    private static String requireNonBlank(String s, String msg) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException(msg);
        return s.trim();
    }

    private static int requireAnoLetivo(int ano) {
        if (ano <= 0) throw new IllegalArgumentException("anoLetivo deve ser positivo");
        return ano;
    }
}
