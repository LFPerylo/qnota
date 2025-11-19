package dev.com.qnota.dominio.principal.coordenador;

import java.util.Objects;

public class Coordenador {

    // ID atribuído pelo repositório/ORM após persistir
    private CoordenadorId id;

    private String nome;
    private String email;
    private String senhaHash;
    private boolean ativo;

    /** Constrói sem ID; ORM atribui depois via repositório. */
    public Coordenador(String nome, String email, String senhaHash, boolean ativo) {
        this.nome      = requireNonBlank(nome,   "'nome' não pode ser vazio");
        this.email     = requireEmail(email);
        this.senhaHash = requireNonBlank(senhaHash, "'senhaHash' não pode ser vazia");
        this.ativo     = ativo;
    }

    /** Infra chama após persistir para fixar o ID gerado. */
    public void atribuirIdSeAusente(CoordenadorId novoId) {
        Objects.requireNonNull(novoId, "'id' não pode ser nulo");
        if (this.id != null && !this.id.equals(novoId)) {
            throw new IllegalStateException("ID já atribuído para este coordenador");
        }
        this.id = novoId;
    }

    // ===== getters =====
    public CoordenadorId getId()   { return id; }
    public String getNome()        { return nome; }
    public String getEmail()       { return email; }
    public String getSenhaHash()   { return senhaHash; }
    public boolean isAtivo()       { return ativo; }

    // ===== comportamentos locais =====
    public void renomear(String novoNome) { this.nome = requireNonBlank(novoNome, "'nome' não pode ser vazio"); }
    public void alterarEmail(String novoEmail) { this.email = requireEmail(novoEmail); }
    /** Espera hash já calculado (hash é responsabilidade da infraestrutura/serviço). */
    public void alterarSenhaHash(String novoHash) { this.senhaHash = requireNonBlank(novoHash, "'senhaHash' não pode ser vazia"); }
    public void ativar()   { this.ativo = true;  }
    public void inativar() { this.ativo = false; }

    // ===== helpers =====
    private static String requireNonBlank(String s, String msg) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException(msg);
        return s.trim();
    }
    private static String requireEmail(String email) {
        String e = requireNonBlank(email, "'email' não pode ser vazio");
        if (!e.contains("@")) throw new IllegalArgumentException("email inválido");
        return e;
    }
}
