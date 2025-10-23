package dev.com.qnota.dominio.principal.professor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Professor {

    // ID gerado na infraestrutura; atribuído depois de salvar
    private ProfessorId id;

    private String nome;
    private final String cpf;   // IMUTÁVEL por regra (como em Responsável)
    private String email;
    private final List<String> especialidades; // >= 1, sem duplicatas (case-insensitive)

    /** Constrói sem ID; o repositório atribui após persistir. */
    public Professor(String nome,
                     String cpf,
                     String email,
                     List<String> especialidades) {

        this.nome  = requireNonBlank(nome,  "'nome' não pode ser vazio");
        this.cpf   = requireNonBlank(cpf,   "'cpf' não pode ser vazio");   // plugue um validador se quiser
        this.email = requireNonBlank(email, "'email' não pode ser vazio");

        Objects.requireNonNull(especialidades, "'especialidades' não pode ser nulo");
        
        // normaliza especialidades (sem regras de negócio)
        List<String> normalizada = new ArrayList<>(especialidades.size());
        for (String e : especialidades) {
            String v = requireNonBlank(e, "especialidade não pode ser vazia").trim();
            normalizada.add(v);
        }
        
        this.especialidades = new ArrayList<>(normalizada);
    }

    /** Chamado pela infraestrutura para fixar o ID gerado. */
    public void atribuirIdSeAusente(ProfessorId novoId) {
        Objects.requireNonNull(novoId, "'id' não pode ser nulo");
        if (this.id != null && !this.id.equals(novoId)) {
            throw new IllegalStateException("ID já atribuído para este professor");
        }
        this.id = novoId;
    }

    // ===== getters =====
    public ProfessorId getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }          // imutável
    public String getEmail() { return email; }
    public List<String> getEspecialidades() { return Collections.unmodifiableList(especialidades); }

    // ===== comportamentos locais =====
    public void renomear(String novoNome) {
        this.nome = requireNonBlank(novoNome, "'nome' não pode ser vazio");
    }

    public void alterarEmail(String novoEmail) {
        this.email = requireNonBlank(novoEmail, "'email' não pode ser vazio");
        // se quiser reforçar: if (!this.email.contains("@")) throw new IllegalArgumentException("email inválido");
    }

    /** Adiciona especialidade se ainda não existir (case-insensitive). */
    public void adicionarEspecialidade(String nomeArea) {
        String v = requireNonBlank(nomeArea, "especialidade não pode ser vazia");
        boolean existe = especialidades.stream().anyMatch(e -> e.equalsIgnoreCase(v));
        if (!existe) especialidades.add(v);
    }

    /** Remove especialidade. Validação RN-84 fica no ProfessorServico. */
    public void removerEspecialidade(String nomeArea) {
        String v = requireNonBlank(nomeArea, "especialidade não pode ser vazia");
        especialidades.removeIf(e -> e.equalsIgnoreCase(v));
        // Validação RN-84 removida - fica no ProfessorServico
    }

    public boolean possuiEspecialidade(String nomeArea) {
        String v = requireNonBlank(nomeArea, "especialidade não pode ser vazia");
        return especialidades.stream().anyMatch(e -> e.equalsIgnoreCase(v));
    }

    // ===== helpers =====
    private static String requireNonBlank(String s, String msg) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException(msg);
        return s.trim();
    }
}
