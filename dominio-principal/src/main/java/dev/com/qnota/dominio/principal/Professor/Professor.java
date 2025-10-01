package dev.com.qnota.dominio.principal.professor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Professor {
    private final ProfessorId id;
    private String nome;
    private String cpf;
    private String email;
    private final List<String> especialidades; // << agora é lista

    public Professor(ProfessorId id, String nome, String cpf, String email, List<String> especialidades) {
        this.id = Objects.requireNonNull(id);
        this.nome = Objects.requireNonNull(nome);
        this.cpf = Objects.requireNonNull(cpf);
        this.email = Objects.requireNonNull(email);
        // cópia defensiva
        this.especialidades = new ArrayList<>(Objects.requireNonNull(especialidades));
    }

    public ProfessorId getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getEmail() { return email; }

    /** Lista imutável das especialidades do professor */
    public List<String> getEspecialidades() {
        return Collections.unmodifiableList(especialidades);
    }

    /** Conveniência: verifica se o professor possui a especialidade informada (case-insensitive). */
    public boolean possuiEspecialidade(String nomeArea) {
        return especialidades.stream().anyMatch(e -> e.equalsIgnoreCase(nomeArea));
    }

    /** Conveniência: adiciona especialidade (usado em testes) */
    public void adicionarEspecialidade(String nomeArea) {
        if (nomeArea != null && especialidades.stream().noneMatch(e -> e.equalsIgnoreCase(nomeArea))) {
            especialidades.add(nomeArea);
        }
    }
}
