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
        if (!validarCpf(cpf)) {
            throw new IllegalArgumentException("o CPF está em formato inválido");
        }
        return cpf;
    }

    /**
     * Valida se o CPF está em formato válido e possui dígitos verificadores corretos.
     * 
     * @param cpf CPF a ser validado (pode conter formatação)
     * @return true se o CPF é válido, false caso contrário
     */
    private static boolean validarCpf(String cpf) {
        if (!formatoCpfValido(cpf)) return false;
        
        String cpfLimpo = cpf.replaceAll("\\D", "");
        
        // Verifica se todos os dígitos são iguais (CPF inválido)
        if (cpfLimpo.chars().distinct().count() == 1) return false;
        
        // Valida os dígitos verificadores
        return calcularDigitoVerificador(cpfLimpo, 9) == (cpfLimpo.charAt(9) - '0') && 
               calcularDigitoVerificador(cpfLimpo, 10) == (cpfLimpo.charAt(10) - '0');
    }

    /**
     * Verifica se o CPF está em formato válido (11 dígitos numéricos).
     * 
     * @param cpf CPF a ser verificado
     * @return true se o formato é válido, false caso contrário
     */
    private static boolean formatoCpfValido(String cpf) {
        return cpf != null && cpf.replaceAll("\\D", "").matches("\\d{11}");
    }

    /**
     * Calcula o dígito verificador do CPF na posição especificada.
     * 
     * @param cpf CPF sem formatação (apenas dígitos)
     * @param posicao Posição do dígito verificador (9 ou 10)
     * @return dígito verificador calculado
     */
    private static int calcularDigitoVerificador(String cpf, int posicao) {
        int soma = 0;
        int peso = posicao + 1;
        
        for (int i = 0; i < posicao; i++) {
            soma += (cpf.charAt(i) - '0') * (peso--);
        }
        
        int resto = soma % 11;
        int digito = 11 - resto;
        
        return (digito >= 10) ? 0 : digito;
    }
}
