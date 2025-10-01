package dev.com.qnota.dominio.principal.responsavel;

public class Responsavel {
    private final ResponsavelId id;
    private String nome;
    private String cpf;
    private String email;
    private Status status;

    public Responsavel(ResponsavelId id, String nome, String cpf, String email, Status status) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.status = status;
    }

    public ResponsavelId getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getEmail() { return email; }
    public Status getStatus() { return status; }

    public enum Status { ATIVO, INADIMPLENTE, INATIVO }
}
