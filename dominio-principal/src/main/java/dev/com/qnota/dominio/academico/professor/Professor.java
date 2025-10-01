package dev.com.qnota.dominio.academico.professor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Professor {
    private final ProfessorId id;
    private String nome;
    private String cpf;
    private String email;
    private boolean ativo;
    private final List<AreaConhecimento> especialidades;

    public Professor(ProfessorId id, String nome, String cpf, String email, boolean ativo, List<AreaConhecimento> especialidades) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.ativo = ativo;
        this.especialidades = new ArrayList<>(especialidades == null ? List.of() : especialidades);
    }

    public ProfessorId getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getEmail() { return email; }
    public boolean isAtivo() { return ativo; }
    public List<AreaConhecimento> getEspecialidades() { return Collections.unmodifiableList(especialidades); }

    public record AreaConhecimento(int id, String nome) {}
}
