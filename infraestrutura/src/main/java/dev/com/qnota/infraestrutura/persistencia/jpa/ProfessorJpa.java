package dev.com.qnota.infraestrutura.persistencia.jpa;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query; // Spring Data JPA
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.com.qnota.aplicacao.principal.professor.ProfessorRepositorioAplicacao;
import dev.com.qnota.aplicacao.principal.professor.ProfessorResumo;
import dev.com.qnota.dominio.principal.professor.Professor;
import dev.com.qnota.dominio.principal.professor.ProfessorId;
import dev.com.qnota.dominio.principal.professor.ProfessorRepositorio;

import java.util.List;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/* =========================
 * ENTIDADE JPA (professores)
 * ========================= */
@Entity
@Table(name = "professores")
class ProfessorJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "nome", nullable = false)
    String nome;

    @Column(name = "cpf", nullable = false, unique = true)
    String cpf;

    @Column(name = "endereco_eletronico", nullable = false)
    String email;

    // JSONB em Postgres
    @Column(name = "especialidades", columnDefinition = "jsonb")
    @Convert(converter = StringListJsonConverter.class)
    List<String> especialidades = new ArrayList<>();

    ProfessorJpa() {}
    ProfessorJpa(String nome, String cpf, String email, List<String> especialidades) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.especialidades = (especialidades != null) ? new ArrayList<>(especialidades) : new ArrayList<>();
    }
}

/* =========================
 * Converter List<String> <-> JSON
 * ========================= */
@Converter
class StringListJsonConverter implements AttributeConverter<List<String>, String> {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<String>> TYPE = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try {
            return MAPPER.writeValueAsString(attribute == null ? List.of() : attribute);
        } catch (IOException e) {
            throw new IllegalStateException("Erro serializando especialidades.", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) return new ArrayList<>();
            return MAPPER.readValue(dbData, TYPE);
        } catch (IOException e) {
            throw new IllegalStateException("Erro desserializando especialidades.", e);
        }
    }
}

/* =========================
 * REPOSITÓRIO SPRING DATA
 * ========================= */
interface ProfessorJpaRepository extends JpaRepository<ProfessorJpa, Integer> {
    Optional<ProfessorJpa> findByCpf(String cpf);

    @Query(value = "SELECT COUNT(*) FROM turmas t WHERE t.professor_id = :id AND t.ativo = TRUE", nativeQuery = true)
    int countTurmasAtivas(@Param("id") int professorId);

    @Modifying
    @Query(value = "UPDATE turmas SET professor_id = :substituto WHERE professor_id = :antigo", nativeQuery = true)
    int substituirProfessor(@Param("antigo") int antigo, @Param("substituto") int substituto);

    // Query para resumos com especialidades como string (JSON array formatado)
    @Query(value = """
        SELECT p.id AS id,
               p.nome AS nome,
               p.cpf AS cpf,
               p.endereco_eletronico AS email,
               COALESCE(p.especialidades::text, '[]') AS especialidades
          FROM professores p
      ORDER BY p.nome
        """, nativeQuery = true)
    List<ProfessorResumo> findProfessorResumoByOrderByNome();
}

/* =========================
 * IMPLEMENTAÇÃO DO DOMÍNIO
 * ========================= */
@Repository
class ProfessorRepositorioImpl implements ProfessorRepositorio, ProfessorRepositorioAplicacao {

    private final ProfessorJpaRepository repo;

    @Autowired
    ProfessorRepositorioImpl(ProfessorJpaRepository repo) {
        this.repo = repo;
    }

    // ----- mapeamento manual -----
    private Professor toDomain(ProfessorJpa j) {
        var p = new Professor(j.nome, j.cpf, j.email, j.especialidades);
        if (j.id != null) p.atribuirIdSeAusente(new ProfessorId(j.id));
        return p;
    }

    private ProfessorJpa novoJpa(Professor d) {
        return new ProfessorJpa(d.getNome(), d.getCpf(), d.getEmail(), d.getEspecialidades());
    }

    private void copiarMutaveis(Professor d, ProfessorJpa j) {
        j.nome = d.getNome();
        j.email = d.getEmail();
        j.especialidades = new ArrayList<>(d.getEspecialidades());
        // cpf é imutável por regra
    }

    // ----- contrato -----
    @Override
    @Transactional
    public ProfessorId salvar(Professor p) {
        ProfessorJpa salvo;
        if (p.getId() == null) {
            salvo = repo.save(novoJpa(p));          // INSERT (Postgres gera id)
            p.atribuirIdSeAusente(new ProfessorId(salvo.id));
        } else {
            var j = repo.findById(p.getId().value())
                        .orElseThrow(() -> new EntityNotFoundException("Professor não encontrado: id=" + p.getId().value()));
            copiarMutaveis(p, j);
            salvo = repo.save(j);                   // UPDATE
        }
        return new ProfessorId(salvo.id);
    }

    @Override
    @Transactional(readOnly = true)
    public Professor porId(ProfessorId id) {
        var j = repo.findById(id.value())
                    .orElseThrow(() -> new EntityNotFoundException("Professor não encontrado: id=" + id.value()));
        return toDomain(j);
    }

    @Override
    @Transactional(readOnly = true)
    public int contarTurmasAtivas(ProfessorId id) {
        return repo.countTurmasAtivas(id.value());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> nomesDeAreasDoProfessor(ProfessorId id) {
        var j = repo.findById(id.value())
                    .orElseThrow(() -> new EntityNotFoundException("Professor não encontrado: id=" + id.value()));
        return List.copyOf(j.especialidades);
    }

    @Override
    @Transactional
    public void substituirProfessor(ProfessorId antigo, ProfessorId substituto) {
        repo.substituirProfessor(antigo.value(), substituto.value());
    }

    /* ---------- contrato da aplicação ---------- */

    @Transactional(readOnly = true)
    @Override
    public List<ProfessorResumo> pesquisarResumos() {
        return repo.findProfessorResumoByOrderByNome();
    }
}
