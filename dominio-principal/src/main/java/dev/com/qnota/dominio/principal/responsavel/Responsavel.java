package dev.com.qnota.dominio.principal.responsavel;

import java.util.Objects;

final public class Responsavel {

    private final ResponsavelId id;
    private String nome;
    private final String cpf;     // IMUTÁVEL por regra
    private String email;
    private Status status;

    public enum Status { ATIVO, INADIMPLENTE, INATIVO }

    public Responsavel(ResponsavelId id, String nome, String cpf, String email, Status status) {
        this.id     = Objects.requireNonNull(id,     "'id' não pode ser nulo");
        this.nome   = requireNonBlank(nome,          "'nome' não pode ser vazio");
        this.cpf    = requireCpfValido(cpf);         // valida formato e dígitos
        this.email  = requireNonBlank(email,         "'email' não pode ser vazio");
        this.status = Objects.requireNonNull(status, "'status' não pode ser nulo");
    }

    // ===== getters =====
    public ResponsavelId getId()   { return id; }
    public String        getNome() { return nome; }
    public String        getCpf()  { return cpf; }
    public String        getEmail(){ return email; }
    public Status        getStatus(){ return status; }

    // ===== comportamentos do agregado (só estado local) =====

    /** RN-17 implícita: CPF imutável; apenas nome e e-mail podem ser alterados. */
    public void renomear(String novoNome) {
        this.nome = requireNonBlank(novoNome, "'nome' não pode ser vazio");
    }

    public void alterarEmail(String novoEmail) {
        this.email = requireNonBlank(novoEmail, "'email' não pode ser vazio");
        // se quiser reforçar um formato mínimo: 
        // if (!this.email.contains("@")) throw new IllegalArgumentException("email em formato inválido");
    }

    public void marcarInadimplente() { this.status = Status.INADIMPLENTE; } // RN-136 (estado do responsável)
    public void regularizar()        { this.status = Status.ATIVO; }
    public void inativar()           { this.status = Status.INATIVO; }

    // ===== helpers de validação =====
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
