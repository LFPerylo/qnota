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

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
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
    
    // Query com JOIN FETCH para garantir que a área seja carregada
    @Query("SELECT d FROM DisciplinaJpa d JOIN FETCH d.area WHERE d.id = :id")
    Optional<DisciplinaJpa> findByIdWithArea(@Param("id") Integer id);

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
        if (a == null) {
            throw new IllegalArgumentException("AreaConhecimentoJpa não pode ser nulo");
        }
        // Se o ID for null (não persistido ainda), usa 0 como valor padrão
        // O ID real será atribuído quando a área for salva
        // Verificar se o ID é null antes de chamar intValue()
        // Usar unboxing seguro
        int areaId = 0;
        Integer idValue = a.id;
        if (idValue != null) {
            areaId = idValue; // Unboxing automático, mas já verificamos null
        }
        String nomeArea = (a.nome != null) ? a.nome : "";
        return new AreaConhecimento(areaId, nomeArea);
    }

    private Disciplina toDomain(DisciplinaJpa j) {
        // Garantir que a área esteja carregada antes de converter
        if (j.area == null) {
            throw new IllegalStateException("Disciplina não possui área associada.");
        }
        // Forçar o carregamento da área se necessário
        // Acessar o ID de forma segura para evitar NullPointerException
        // Primeiro, tentar acessar o nome para forçar o carregamento do proxy
        String nomeArea = null;
        try {
            nomeArea = j.area.nome;
        } catch (Exception e) {
            // Se houver erro ao acessar o nome, a área pode ser um proxy não inicializado
            throw new IllegalStateException("Erro ao acessar área da disciplina: " + e.getMessage(), e);
        }
        
        // Tentar acessar o ID de forma segura
        Integer areaId = null;
        try {
            // Acessar o ID de forma segura
            Integer tempId = j.area.id;
            if (tempId != null) {
                areaId = tempId;
            }
        } catch (Exception e) {
            // Se houver erro ao acessar o ID, tentamos buscar pelo nome
            if (nomeArea != null && !nomeArea.isEmpty()) {
                var areaExistente = areaRepo.findByNomeIgnoreCase(nomeArea);
                if (areaExistente.isPresent()) {
                    j.area = areaExistente.get();
                    Integer tempId = j.area.id;
                    if (tempId != null) {
                        areaId = tempId;
                    }
                }
            }
        }
        
        // Se ainda não tem ID, tentamos buscar pelo nome
        if (areaId == null && nomeArea != null && !nomeArea.isEmpty()) {
            var areaExistente = areaRepo.findByNomeIgnoreCase(nomeArea);
            if (areaExistente.isPresent()) {
                j.area = areaExistente.get();
            }
        }
        
        // Agora podemos converter com segurança
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
        // Buscar ou criar a área e garantir que tenha ID
        AreaConhecimentoJpa area = areaRepo.findByNomeIgnoreCase(voArea.nome())
            .orElseGet(() -> {
                AreaConhecimentoJpa novaArea = new AreaConhecimentoJpa(voArea.nome().trim());
                return areaRepo.save(novaArea);
            });
        // Garantir que a área tenha ID válido
        if (area.id == null) {
            // Se ainda não tem ID, salvar novamente para forçar a geração
            area = areaRepo.save(area);
        }
        return area;
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
        // Usar JOIN FETCH para garantir que a área seja carregada
        var jOpt = disciplinaRepo.findByIdWithArea(id.value());
        DisciplinaJpa j;
        if (jOpt.isPresent()) {
            j = jOpt.get();
        } else {
            // Fallback para findById se findByIdWithArea não encontrar
            j = disciplinaRepo.findById(id.value())
                .orElseThrow(() -> new EntityNotFoundException("Disciplina id=" + id.value() + " não encontrada."));
        }
        
        // Garantir que a área seja carregada
        if (j.area == null) {
            throw new IllegalStateException("Disciplina id=" + id.value() + " não possui área associada.");
        }
        
        // Forçar o carregamento da área acessando o nome primeiro
        // Isso garante que o proxy do Hibernate seja inicializado
        String nomeArea = null;
        try {
            nomeArea = j.area.nome;
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao acessar nome da área da disciplina id=" + id.value() + ": " + e.getMessage(), e);
        }
        
        // Tentar acessar o ID de forma segura
        Integer areaIdValue = null;
        try {
            // Acessar o ID de forma segura
            Integer tempId = j.area.id;
            if (tempId != null) {
                areaIdValue = tempId;
            }
        } catch (Exception e) {
            // Se houver erro, tentar buscar pelo nome
            if (nomeArea != null && !nomeArea.isEmpty()) {
                var areaExistente = areaRepo.findByNomeIgnoreCase(nomeArea);
                if (areaExistente.isPresent()) {
                    j.area = areaExistente.get();
                    Integer tempId = j.area.id;
                    if (tempId != null) {
                        areaIdValue = tempId;
                    }
                }
            }
        }
        
        // Se ainda não tem ID, buscar pelo nome
        if (areaIdValue == null) {
            if (nomeArea == null || nomeArea.isEmpty()) {
                throw new IllegalStateException("Disciplina id=" + id.value() + " possui área sem nome válido.");
            }
            var areaExistente = areaRepo.findByNomeIgnoreCase(nomeArea);
            if (areaExistente.isPresent()) {
                j.area = areaExistente.get();
            } else {
                throw new IllegalStateException("Disciplina id=" + id.value() + " referencia área '" + nomeArea + "' que não existe no banco de dados.");
            }
        }
        
        // Garantir que a área tenha ID válido antes de converter
        Integer finalAreaId = null;
        try {
            finalAreaId = j.area.id;
        } catch (Exception e) {
            // Se ainda houver erro, tentar buscar novamente
            if (nomeArea != null && !nomeArea.isEmpty()) {
                var areaExistente = areaRepo.findByNomeIgnoreCase(nomeArea);
                if (areaExistente.isPresent()) {
                    j.area = areaExistente.get();
                    finalAreaId = j.area.id;
                }
            }
        }
        if (finalAreaId == null) {
            throw new IllegalStateException("Disciplina id=" + id.value() + " possui área sem ID válido após processamento.");
        }
        
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
