package dev.com.qnota.dominio.principal.responsavel;

import java.util.Objects;

public final class Responsavel {

    // ID gerado na infraestrutura; atribuído pelo repositório após persistir
    private ResponsavelId id;

    private String nome;
    private final String cpf;   // IMUTÁVEL por regra
    private String email;
    private Status status;

    public enum Status { ATIVO, INADIMPLENTE, INATIVO }

    /** Constrói sem ID; o repositório chamará atribuirIdSeAusente(...) após salvar. */
    public Responsavel(String nome, String cpf, String email, Status status) {
        this.nome   = requireNonBlank(nome,  "'nome' não pode ser vazio");
        this.cpf    = requireCpfValido(cpf); // valida formato e dígitos
        this.email  = requireNonBlank(email, "'email' não pode ser vazio");
        this.status = Objects.requireNonNull(status, "'status' não pode ser nulo");
    }

    /** Infra chama para fixar o ID gerado. Não permite reatribuição divergente. */
    public void atribuirIdSeAusente(ResponsavelId novoId) {
        Objects.requireNonNull(novoId, "'id' não pode ser nulo");
        if (this.id != null && !this.id.equals(novoId)) {
            throw new IllegalStateException("ID já atribuído para este responsável");
        }
        this.id = novoId;
    }

    // ===== getters =====
    public ResponsavelId getId()   { return id; }
    public String        getNome() { return nome; }
    public String        getCpf()  { return cpf; }
    public String        getEmail(){ return email; }
    public Status        getStatus(){ return status; }

    // ===== comportamentos locais =====
    /** RN-17 implícita: CPF imutável; altera apenas nome e e-mail. */
    public void renomear(String novoNome) {
        this.nome = requireNonBlank(novoNome, "'nome' não pode ser vazio");
    }

    public void alterarEmail(String novoEmail) {
        this.email = requireNonBlank(novoEmail, "'email' não pode ser vazio");
    }

    public void marcarInadimplente() { this.status = Status.INADIMPLENTE; } // RN-136
    public void regularizar()        { this.status = Status.ATIVO; }
    public void inativar()           { this.status = Status.INATIVO; }

    // ===== helpers =====
    private static String requireNonBlank(String s, String msg) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException(msg);
        return s.trim();
    }

    private static String requireCpfValido(String cpf) {
        if (!CpfValidator.valido(cpf)) {
            throw new IllegalArgumentException("o CPF está em formato inválido");
        }
        return cpf;
    }
}
