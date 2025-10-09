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

    private final AlunoId id;
    private String nome;
    private LocalDate dataNascimento;
    private boolean ativo;
    private TurmaId turma;
    private final List<AlunoResponsavel> responsaveis;

    public Aluno(AlunoId id,
                 String nome,
                 LocalDate dataNascimento,
                 boolean ativo,
                 TurmaId turma,
                 List<AlunoResponsavel> responsaveis) {

        this.id = Objects.requireNonNull(id, "'id' não pode ser nulo");
        this.nome = requireNonBlank(nome, "'nome' não pode ser vazio");
        this.dataNascimento = Objects.requireNonNull(dataNascimento, "'dataNascimento' não pode ser nula");
        this.ativo = ativo;
        this.turma = Objects.requireNonNull(turma, "'turma' não pode ser nula");

        // cópia defensiva + bloqueio de elementos nulos
        this.responsaveis = copyAndValidateResponsaveis(responsaveis);

        // invariantes do agregado (RN-02, RN-19, RN-58, RN-20)
        validarInvariantesResponsaveis(this.responsaveis);
    }

    // ===== getters =====
    public AlunoId getId() { return id; }
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

    public void adicionarResponsavel(ResponsavelId idResp, String grauParentesco, boolean principal) {
        Objects.requireNonNull(idResp, "'responsavelId' não pode ser nulo");

        if (responsaveis.size() >= 3)
            throw new IllegalStateException("RN-02: Máximo de 3 responsáveis por aluno.");

        if (responsaveis.stream().anyMatch(ar -> ar.responsavel().equals(idResp)))
            throw new IllegalStateException("RN-20: Vínculo de responsável duplicado para este aluno.");

        if (principal && responsaveis.stream().anyMatch(AlunoResponsavel::principal))
            throw new IllegalStateException("RN-58: Já existe responsável principal.");

        var nova = new ArrayList<>(responsaveis);
        nova.add(new AlunoResponsavel(idResp, grauParentesco, principal)); // record garante NOT NULL
        validarInvariantesResponsaveis(nova);
        responsaveis.clear();
        responsaveis.addAll(nova);
    }

    public void removerResponsavel(ResponsavelId idResp) {
        var nova = new ArrayList<>(responsaveis);
        var removido = nova.removeIf(ar -> ar.responsavel().equals(idResp));
        if (!removido) return;

        if (nova.isEmpty())
            throw new IllegalStateException("RN-19: Aluno deve ter ao menos um responsável.");

        boolean eraPrincipal = responsaveis.stream()
                .filter(ar -> ar.responsavel().equals(idResp))
                .findFirst().map(AlunoResponsavel::principal).orElse(false);

        if (eraPrincipal && nova.stream().noneMatch(AlunoResponsavel::principal)) {
            var primeiro = nova.get(0);
            nova.set(0, new AlunoResponsavel(primeiro.responsavel(), primeiro.grauParentesco(), true));
        }
        validarInvariantesResponsaveis(nova);
        responsaveis.clear();
        responsaveis.addAll(nova);
    }

    public void definirPrincipal(ResponsavelId idResp) {
        if (responsaveis.stream().noneMatch(ar -> ar.responsavel().equals(idResp)))
            throw new IllegalStateException("RN-20: Responsável não está vinculado ao aluno.");

        var nova = new ArrayList<AlunoResponsavel>(responsaveis.size());
        for (var ar : responsaveis) {
            boolean principal = ar.responsavel().equals(idResp);
            nova.add(new AlunoResponsavel(ar.responsavel(), ar.grauParentesco(), principal));
        }
        validarInvariantesResponsaveis(nova);
        responsaveis.clear();
        responsaveis.addAll(nova);
    }

    // ===== invariantes =====
    private static void validarInvariantesResponsaveis(List<AlunoResponsavel> lista) {
        if (lista.isEmpty())
            throw new IllegalArgumentException("RN-19: Aluno deve ter ao menos um responsável.");

        if (lista.size() > 3)
            throw new IllegalArgumentException("RN-02: Máximo de 3 responsáveis por aluno.");

        long qtdPrincipais = lista.stream().filter(AlunoResponsavel::principal).count();
        if (qtdPrincipais != 1)
            throw new IllegalArgumentException("RN-58: Deve haver exatamente um responsável principal.");

        Set<ResponsavelId> set = new LinkedHashSet<>();
        boolean duplicado = lista.stream().anyMatch(ar -> !set.add(ar.responsavel()));
        if (duplicado)
            throw new IllegalArgumentException("RN-20: Vínculo de responsável duplicado para este aluno.");
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
            // Deixar cair na RN-19 (>=1) com mensagem mais clara do agregado
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
    public record AlunoResponsavel(ResponsavelId responsavel, String grauParentesco, boolean principal) {
        public AlunoResponsavel {
            Objects.requireNonNull(responsavel, "'responsavel' não pode ser nulo");
            grauParentesco = requireNonBlank(grauParentesco, "'grauParentesco' não pode ser vazio");
        }
    }
}
