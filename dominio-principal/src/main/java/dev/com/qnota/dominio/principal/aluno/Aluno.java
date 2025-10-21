package dev.com.qnota.dominio.principal.aluno;

import java.time.LocalDate;
import java.util.*;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;
import dev.com.qnota.dominio.principal.turma.TurmaId;

public class Aluno {

    private AlunoId id; // atribuído pelo repositório/ORM
    private String nome;
    private LocalDate dataNascimento;
    private boolean ativo;
    private TurmaId turma;

    // Agora: lista simples de responsáveis + um principal
    private final List<ResponsavelId> responsaveis;
    private ResponsavelId responsavelPrincipal;

    /** Construtor recomendado (ID gerado na persistência). */
    public Aluno(String nome,
                 LocalDate dataNascimento,
                 boolean ativo,
                 TurmaId turma,
                 List<ResponsavelId> responsaveis,
                 ResponsavelId principal) {

        this.id = null;
        this.nome = requireNonBlank(nome, "'nome' não pode ser vazio");
        this.dataNascimento = Objects.requireNonNull(dataNascimento, "'dataNascimento' não pode ser nula");
        this.ativo = ativo;
        this.turma = Objects.requireNonNull(turma, "'turma' não pode ser nula");

        var lista = copyIds(responsaveis);
        validarInvariantes(lista, principal);
        this.responsaveis = lista;
        this.responsavelPrincipal = principal;
    }

    /** Construtor compatível quando o ID já é conhecido. */
    public Aluno(AlunoId id,
                 String nome,
                 LocalDate dataNascimento,
                 boolean ativo,
                 TurmaId turma,
                 List<ResponsavelId> responsaveis,
                 ResponsavelId principal) {
        this(nome, dataNascimento, ativo, turma, responsaveis, principal);
        this.id = Objects.requireNonNull(id, "'id' não pode ser nulo");
    }

    /** ORM/Repo fixa o ID se ainda não houver. */
    public void atribuirIdSeAusente(AlunoId novoId) {
        Objects.requireNonNull(novoId, "'id' não pode ser nulo");
        if (this.id != null && !this.id.equals(novoId))
            throw new IllegalStateException("ID já atribuído e diferente");
        this.id = novoId;
    }

    // ========= getters =========
    public AlunoId getId() { return id; }
    public String getNome() { return nome; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public boolean isAtivo() { return ativo; }
    public TurmaId getTurma() { return turma; }
    public List<ResponsavelId> getResponsaveis() { return Collections.unmodifiableList(responsaveis); }
    public ResponsavelId getResponsavelPrincipal() { return responsavelPrincipal; }

    // ========= operações =========
    public void inativar() { this.ativo = false; }
    public void ativar()   { this.ativo = true;  }

    public void mudarTurma(TurmaId novaTurma) {
        this.turma = Objects.requireNonNull(novaTurma, "'novaTurma' não pode ser nula");
    }

    /** Substituição completa da lista e do principal. */
    public void substituirResponsaveis(List<ResponsavelId> novaLista, ResponsavelId novoPrincipal) {
        var tmp = copyIds(novaLista);
        validarInvariantes(tmp, novoPrincipal);
        responsaveis.clear();
        responsaveis.addAll(tmp);
        responsavelPrincipal = novoPrincipal;
    }

    /** Vincular responsável (até 3). Se principal=true, define como principal. */
    public void adicionarResponsavel(ResponsavelId idResp, boolean principal) {
        Objects.requireNonNull(idResp, "'responsavelId' não pode ser nulo");

        if (responsaveis.size() >= 3)
            throw new IllegalStateException("o número máximo de responsáveis por aluno é 3");

        if (responsaveis.contains(idResp))
            throw new IllegalStateException("já existe vínculo entre o responsável e o aluno");

        var nova = new ArrayList<>(responsaveis);
        nova.add(idResp);

        var novoPrincipal = this.responsavelPrincipal;
        if (principal) {
            if (novoPrincipal != null)
                throw new IllegalStateException("deve haver exatamente um responsável principal");
            novoPrincipal = idResp;
        }
        if (novoPrincipal == null) novoPrincipal = idResp; // garante 1 principal

        validarInvariantes(nova, novoPrincipal);
        responsaveis.clear();
        responsaveis.addAll(nova);
        responsavelPrincipal = novoPrincipal;
    }

    /** Desvincula; se remover o principal, promove o primeiro da lista. */
    public void removerResponsavel(ResponsavelId idResp) {
        var nova = new ArrayList<>(responsaveis);
        boolean removido = nova.remove(idResp);
        if (!removido) return;

        if (nova.isEmpty())
            throw new IllegalStateException("o aluno deve ter pelo menos um responsável");

        var novoPrincipal = this.responsavelPrincipal;
        if (idResp.equals(this.responsavelPrincipal)) {
            novoPrincipal = nova.get(0); // autopromoção
        }

        validarInvariantes(nova, novoPrincipal);
        responsaveis.clear();
        responsaveis.addAll(nova);
        responsavelPrincipal = novoPrincipal;
    }

    public void definirPrincipal(ResponsavelId idResp) {
        if (!responsaveis.contains(idResp))
            throw new IllegalStateException("Vínculo de responsável inexistente");
        validarInvariantes(responsaveis, idResp);
        this.responsavelPrincipal = idResp;
    }

    // ========= invariantes =========
    private static void validarInvariantes(List<ResponsavelId> lista, ResponsavelId principal) {
        if (lista == null || lista.isEmpty())
            throw new IllegalArgumentException("Aluno deve ter ao menos um responsável");
        if (lista.size() > 3)
            throw new IllegalArgumentException("o número máximo de responsáveis por aluno é 3");
        // sem duplicados
        var set = new LinkedHashSet<>(lista);
        if (set.size() != lista.size())
            throw new IllegalArgumentException("Vínculo de responsável duplicado");
        // principal válido
        if (principal == null)
            throw new IllegalArgumentException("é obrigatório definir um responsável principal");
        if (!set.contains(principal))
            throw new IllegalArgumentException("o responsável principal deve estar entre os responsáveis");
    }

    // ========= helpers =========
    private static String requireNonBlank(String s, String msg) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException(msg);
        return s.trim();
    }
    private static List<ResponsavelId> copyIds(List<ResponsavelId> origem) {
        if (origem == null) return new ArrayList<>();
        var tmp = new ArrayList<ResponsavelId>(origem.size());
        for (var id : origem) {
            if (id == null) throw new IllegalArgumentException("Responsável não pode ser nulo");
            tmp.add(id);
        }
        return tmp;
    }
}
