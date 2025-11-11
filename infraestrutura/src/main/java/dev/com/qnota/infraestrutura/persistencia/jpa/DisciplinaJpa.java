package dev.com.qnota.infraestrutura.persistencia.jpa;

import java.lang.reflect.Field;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.com.qnota.aplicacao.principal.disciplina.DisciplinaRepositorioAplicacao;
import dev.com.qnota.aplicacao.principal.disciplina.DisciplinaResumo;
import dev.com.qnota.dominio.principal.disciplina.Disciplina;
import dev.com.qnota.dominio.principal.disciplina.Disciplina.AreaConhecimento;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaRepositorio;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/* =======================
   ENTIDADES JPA
   ======================= */

@Entity
@Table(name = "areas_conhecimento")
class AreaConhecimentoJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "nome", nullable = false, unique = true)
    String nome;

    AreaConhecimentoJpa() {}

    AreaConhecimentoJpa(String nome) { this.nome = nome; }
}

@Entity
@Table(
    name = "disciplinas",
    uniqueConstraints = @UniqueConstraint(
        name = "ux_disc_nome_area_versao",
        columnNames = {"nome", "area_id", "versao"}
    )
)
class DisciplinaJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "nome", nullable = false)
    String nome;

    @Column(name = "versao", nullable = false)
    Integer versao;

    @Column(name = "id_versao_origem")
    Integer idVersaoOrigem;

    @Column(name = "ativo", nullable = false)
    Boolean ativo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "area_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_disc_area"))
    AreaConhecimentoJpa area;

    DisciplinaJpa() {}
}

/* =======================
   REPOSITÓRIOS SPRING DATA
   ======================= */

interface AreaConhecimentoJpaRepository extends JpaRepository<AreaConhecimentoJpa, Integer> {
    Optional<AreaConhecimentoJpa> findByNomeIgnoreCase(String nome);
}

interface DisciplinaJpaRepository extends JpaRepository<DisciplinaJpa, Integer> {

    boolean existsByNomeIgnoreCaseAndArea_NomeIgnoreCase(String nome, String areaNome);

    // Postgres: retorna boolean diretamente
    @Query(value =
        "SELECT ( " +
        "  EXISTS (SELECT 1 FROM simulado_disciplinas sd WHERE sd.disciplina_id = :id) " +
        "  OR EXISTS (SELECT 1 FROM notas_do_aluno n WHERE n.disciplina_id = :id) " +
        ")",
        nativeQuery = true)
    boolean usedAnywhere(@Param("id") int disciplinaId);

    @Query(value =
        "SELECT ( " +
        "  EXISTS ( " +
        "    SELECT 1 FROM simulado_disciplinas sd " +
        "    JOIN simulados s ON s.id = sd.simulado_id " +
        "    WHERE sd.disciplina_id = :id AND s.status = 'FINALIZADO' " +
        "  ) " +
        "  OR EXISTS ( " +
        "    SELECT 1 FROM notas_do_aluno n " +
        "    JOIN simulados s ON s.id = n.simulado_id " +
        "    WHERE n.disciplina_id = :id AND s.status = 'FINALIZADO' " +
        "  ) " +
        ")",
        nativeQuery = true)
    boolean usedInFinalizado(@Param("id") int disciplinaId);

    // Query para resumos com nome da área
    @Query(value = """
        SELECT d.id AS id,
               d.nome AS nome,
               d.versao AS versao,
               d.ativo AS ativo,
               a.nome AS areaNome
          FROM disciplinas d
     LEFT JOIN areas_conhecimento a ON a.id = d.area_id
      ORDER BY d.nome
        """, nativeQuery = true)
    List<DisciplinaResumo> findDisciplinaResumoByOrderByNome();
}

/* =======================
   IMPLEMENTAÇÃO DO REPOSITÓRIO DE DOMÍNIO
   ======================= */

@Repository
class DisciplinaRepositorioImpl implements DisciplinaRepositorio, DisciplinaRepositorioAplicacao {

    private final DisciplinaJpaRepository disciplinaRepo;
    private final AreaConhecimentoJpaRepository areaRepo;

    @Autowired
    DisciplinaRepositorioImpl(DisciplinaJpaRepository disciplinaRepo,
                              AreaConhecimentoJpaRepository areaRepo) {
        this.disciplinaRepo = disciplinaRepo;
        this.areaRepo = areaRepo;
    }

    private static void setPrivate(Object target, String fieldName, Object value) {
        try {
            Field f = Disciplina.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao atribuir campo '" + fieldName + "' em Disciplina.", e);
        }
    }

    private static AreaConhecimento toVO(AreaConhecimentoJpa a) {
        return new AreaConhecimento(a.id, a.nome);
    }

    private Disciplina toDomain(DisciplinaJpa j) {
        var voArea = toVO(j.area);
        var d = new Disciplina(j.nome, voArea);       // v1
        d.atribuirIdSeAusente(new DisciplinaId(j.id));
        setPrivate(d, "versao", j.versao);
        setPrivate(d, "idVersaoOrigem", j.idVersaoOrigem);
        setPrivate(d, "ativo", j.ativo != null ? j.ativo : Boolean.TRUE);
        return d;
    }

    private DisciplinaJpa toJpa(Disciplina d, AreaConhecimentoJpa area) {
        DisciplinaJpa j = (d.getId() != null)
            ? disciplinaRepo.findById(d.getId().value()).orElseGet(DisciplinaJpa::new)
            : new DisciplinaJpa();

        j.nome = d.getNome();
        j.versao = d.getVersao();
        j.idVersaoOrigem = d.getIdVersaoOrigem();
        j.ativo = d.isAtivo();
        j.area = area;
        return j;
    }

    private AreaConhecimentoJpa resolveArea(AreaConhecimento voArea) {
        if (voArea == null) throw new IllegalArgumentException("Area de conhecimento não pode ser nula.");
        if (voArea.id() > 0) {
            return areaRepo.findById(voArea.id())
                .orElseThrow(() -> new IllegalArgumentException("Area id=" + voArea.id() + " inexistente."));
        }
        return areaRepo.findByNomeIgnoreCase(voArea.nome())
            .orElseGet(() -> areaRepo.save(new AreaConhecimentoJpa(voArea.nome().trim())));
    }

    @Override
    @Transactional
    public DisciplinaId salvar(Disciplina d) {
        var areaJpa = resolveArea(d.getArea());
        var jpa = toJpa(d, areaJpa);
        jpa = disciplinaRepo.save(jpa); // IDENTITY (Postgres)
        d.atribuirIdSeAusente(new DisciplinaId(jpa.id));
        return new DisciplinaId(jpa.id);
    }

    @Override
    @Transactional(readOnly = true)
    public Disciplina porId(DisciplinaId id) {
        var j = disciplinaRepo.findById(id.value())
            .orElseThrow(() -> new EntityNotFoundException("Disciplina id=" + id.value() + " não encontrada."));
        return toDomain(j);
    }

    @Override
    @Transactional
    public void remover(DisciplinaId id) {
        disciplinaRepo.deleteById(id.value());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeNomeNaArea(String nome, String areaNome) {
        if (nome == null || areaNome == null) return false;
        return disciplinaRepo.existsByNomeIgnoreCaseAndArea_NomeIgnoreCase(nome.trim(), areaNome.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean foiUsadaEmAlgumSimulado(DisciplinaId id) {
        return disciplinaRepo.usedAnywhere(id.value());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean foiUsadaEmSimuladoFinalizado(DisciplinaId id) {
        return disciplinaRepo.usedInFinalizado(id.value());
    }

    /* ---------- contrato da aplicação ---------- */

    @Transactional(readOnly = true)
    @Override
    public List<DisciplinaResumo> pesquisarResumos() {
        return disciplinaRepo.findDisciplinaResumoByOrderByNome();
    }
}
