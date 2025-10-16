package dev.com.qnota.dominio.principal.aluno;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;
import dev.com.qnota.dominio.principal.turma.TurmaId;

public class Aluno {

    // Agora o id NÃO é final para permitir atribuição pós-persistência (auto-increment/identity)
    private AlunoId id;

    private String nome;
    private LocalDate dataNascimento;
    private boolean ativo;
    private TurmaId turma;
    private final List<AlunoResponsavel> responsaveis;

    /** Construtor recomendado quando o ID será gerado pelo repositório/banco. */
    public Aluno(String nome,
                 LocalDate dataNascimento,
                 boolean ativo,
                 TurmaId turma,
                 List<AlunoResponsavel> responsaveis) {

        this.id = null; // será atribuído pelo repositório
        this.nome = requireNonBlank(nome, "'nome' não pode ser vazio");
        this.dataNascimento = Objects.requireNonNull(dataNascimento, "'dataNascimento' não pode ser nula");
        this.ativo = ativo;
        this.turma = Objects.requireNonNull(turma, "'turma' não pode ser nula");

        // cópia defensiva + bloqueio de elementos nulos
        this.responsaveis = copyAndValidateResponsaveis(responsaveis);

        // invariantes do agregado (RN-02, RN-19, RN-58, RN-20)
        validarInvariantesResponsaveis(this.responsaveis);
    }

    /** Construtor de compatibilidade (legado) quando o ID já é conhecido. */
    public Aluno(AlunoId id,
                 String nome,
                 LocalDate dataNascimento,
                 boolean ativo,
                 TurmaId turma,
                 List<AlunoResponsavel> responsaveis) {
        this(nome, dataNascimento, ativo, turma, responsaveis);
        this.id = Objects.requireNonNull(id, "'id' não pode ser nulo");
    }

    /**
     * Permite que o repositório atribua o ID após persistir (identity/auto-increment).
     * Se já houver ID e for diferente, lança exceção.
     */
    public void atribuirIdSeAusente(AlunoId novoId) {
        Objects.requireNonNull(novoId, "'id' não pode ser nulo");
        if (this.id != null && !this.id.equals(novoId)) {
            throw new IllegalStateException("ID já atribuído e diferente");
        }
        this.id = novoId;
    }

    // ===== getters =====
    public AlunoId getId() { return id; } // pode ser nulo antes da persistência
    public String getNome() { return nome; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public boolean isAtivo() { return ativo; }
    public TurmaId getTurma() { return turma; }
    public List<AlunoResponsavel> getResponsaveis() { return Collections.unmodifiableList(responsaveis); }

    // ===== operações do agregado =====
    public void inativar() { this.ativo = false; }
    public void ativar()   { this.ativo = true;  }

    public void mudarTurma(TurmaId novaTurma) {
        this.turma = Objects.requireNonNull(novaTurma, "'novaTurma' não pode ser nula");
    }

    public void substituirResponsaveis(List<AlunoResponsavel> novaLista) {
        var tmp = copyAndValidateResponsaveis(novaLista);
        validarInvariantesResponsaveis(tmp);
        responsaveis.clear();
        responsaveis.addAll(tmp);
    }

    public void adicionarResponsavel(ResponsavelId idResp, boolean principal) {
        Objects.requireNonNull(idResp, "'responsavelId' não pode ser nulo");

        if (responsaveis.size() >= 3)
            throw new IllegalStateException("o número máximo de responsáveis por aluno é 3");

        if (responsaveis.stream().anyMatch(ar -> ar.responsavel().equals(idResp)))
            throw new IllegalStateException("Vínculo de responsável duplicado");

        if (principal && responsaveis.stream().anyMatch(AlunoResponsavel::principal))
            throw new IllegalStateException("deve haver exatamente um responsável principal");

        var nova = new ArrayList<>(responsaveis);
        nova.add(new AlunoResponsavel(idResp, principal)); // record garante NOT NULL
        validarInvariantesResponsaveis(nova);
        responsaveis.clear();
        responsaveis.addAll(nova);
    }

    public void removerResponsavel(ResponsavelId idResp) {
        var nova = new ArrayList<>(responsaveis);
        var removido = nova.removeIf(ar -> ar.responsavel().equals(idResp));
        if (!removido) return;

        if (nova.isEmpty())
            throw new IllegalStateException("Aluno deve ter ao menos um responsável");

        boolean eraPrincipal = responsaveis.stream()
                .filter(ar -> ar.responsavel().equals(idResp))
                .findFirst().map(AlunoResponsavel::principal).orElse(false);

        if (eraPrincipal && nova.stream().noneMatch(AlunoResponsavel::principal)) {
            var primeiro = nova.get(0);
            nova.set(0, new AlunoResponsavel(primeiro.responsavel(), true));
        }
        validarInvariantesResponsaveis(nova);
        responsaveis.clear();
        responsaveis.addAll(nova);
    }

    public void definirPrincipal(ResponsavelId idResp) {
        if (responsaveis.stream().noneMatch(ar -> ar.responsavel().equals(idResp)))
            throw new IllegalStateException("Vínculo de responsável inexistente");

        var nova = new ArrayList<AlunoResponsavel>(responsaveis.size());
        for (var ar : responsaveis) {
            boolean principal = ar.responsavel().equals(idResp);
            nova.add(new AlunoResponsavel(ar.responsavel(), principal));
        }
        validarInvariantesResponsaveis(nova);
        responsaveis.clear();
        responsaveis.addAll(nova);
    }

    // ===== invariantes =====
    private static void validarInvariantesResponsaveis(List<AlunoResponsavel> lista) {
        if (lista.isEmpty())
            throw new IllegalArgumentException("Aluno deve ter ao menos um responsável");

        if (lista.size() > 3)
            throw new IllegalArgumentException("o número máximo de responsáveis por aluno é 3");

        long qtdPrincipais = lista.stream().filter(AlunoResponsavel::principal).count();
        if (qtdPrincipais == 0)
            throw new IllegalArgumentException("é obrigatório definir um responsável principal");
        if (qtdPrincipais > 1)
            throw new IllegalArgumentException("deve haver exatamente um responsável principal");

        Set<ResponsavelId> set = new LinkedHashSet<>();
        boolean duplicado = lista.stream().anyMatch(ar -> !set.add(ar.responsavel()));
        if (duplicado)
            throw new IllegalArgumentException("Vínculo de responsável duplicado");
    }

    // ===== helpers NOT NULL / NOT BLANK =====
    private static String requireNonBlank(String s, String messageIfInvalid) {
        if (s == null || s.trim().isEmpty()) {
            throw new IllegalArgumentException(messageIfInvalid);
        }
        return s.trim();
    }

    private static List<AlunoResponsavel> copyAndValidateResponsaveis(List<AlunoResponsavel> origem) {
        if (origem == null) {
            // Deixa cair na regra "Aluno deve ter ao menos um responsável"
            return new ArrayList<>();
        }
        var tmp = new ArrayList<AlunoResponsavel>(origem.size());
        for (var ar : origem) {
            if (ar == null) throw new IllegalArgumentException("Responsável não pode ser nulo");
            // o record já valida campos internos; só copiamos
            tmp.add(ar);
        }
        return tmp;
    }

    // ===== value object =====
    public record AlunoResponsavel(ResponsavelId responsavel, boolean principal) {
        public AlunoResponsavel {
            Objects.requireNonNull(responsavel, "'responsavel' não pode ser nulo");
        }
    }
}
