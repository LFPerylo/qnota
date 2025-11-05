// Título da análise: QNota - Infraestrutura (Turma): Entidade JPA, JpaRepository e RepositorioImpl com mapeamento manual

package dev.com.qnota.infraestrutura.persistencia.jpa;

import static jakarta.persistence.GenerationType.IDENTITY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;   // (Spring Data) — evita ambiguidade
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.com.qnota.dominio.principal.turma.Turma;
import dev.com.qnota.dominio.principal.turma.TurmaId;
import dev.com.qnota.dominio.principal.turma.TurmaRepositorio;
import dev.com.qnota.dominio.principal.professor.ProfessorId;
import jakarta.persistence.*;

/* =====================
 * ENTIDADE JPA
 * ===================== */
@Entity
@Table(name = "turmas")
class TurmaJpa {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    Integer id;

    @Column(nullable = false)
    String nome;

    @Column(nullable = false)
    int anoLetivo;

    @Column(nullable = false)
    boolean ativo;

    @Column(name = "professor_id", nullable = false)
    Integer professorId; // FK (mantemos como inteiro para desacoplar de ProfessorJpa)

    @Override public String toString() { return nome; }
}

/* =====================
 * JPA REPOSITORY
 * ===================== */
interface TurmaJpaRepository extends JpaRepository<TurmaJpa, Integer> {

    // Unicidade por nome no ano letivo (case-insensitive)
    boolean existsByNomeIgnoreCaseAndAnoLetivo(String nome, int anoLetivo);

    // Alunos ativos na turma (Postgres: boolean usa TRUE/FALSE)
    @Query(value = """
        SELECT EXISTS(
          SELECT 1 FROM alunos a
           WHERE a.turma_id = :tid
             AND a.ativo = TRUE
        )
        """, nativeQuery = true)
    boolean temAlunosAtivos(@Param("tid") int turmaId);

    // Há simulados da turma
    @Query(value = """
        SELECT EXISTS(
          SELECT 1 FROM simulados s
           WHERE s.turma_id = :tid
        )
        """, nativeQuery = true)
    boolean temSimulados(@Param("tid") int turmaId);

    // Simulados EM_EDICAO
    @Query(value = """
        SELECT EXISTS(
          SELECT 1 FROM simulados s
           WHERE s.turma_id = :tid
             AND s.status = 'EM_EDICAO'
        )
        """, nativeQuery = true)
    boolean temSimuladosEmEdicao(@Param("tid") int turmaId);

    // Simulados FINALIZADO
    @Query(value = """
        SELECT EXISTS(
          SELECT 1 FROM simulados s
           WHERE s.turma_id = :tid
             AND s.status = 'FINALIZADO'
        )
        """, nativeQuery = true)
    boolean temSimuladosFinalizados(@Param("tid") int turmaId);

    // Evita problemas de case/snake em nativo: usa JPQL com campos da entidade
    @Query("select t.anoLetivo from TurmaJpa t where t.id = :tid")
    Integer anoLetivoDe(@Param("tid") int turmaId);
}

/* =====================
 * IMPLEMENTAÇÃO DO REPOSITÓRIO DO DOMÍNIO
 * ===================== */
@Repository
class TurmaRepositorioImpl implements TurmaRepositorio {

    private final TurmaJpaRepository repo;

    @Autowired
    TurmaRepositorioImpl(TurmaJpaRepository repo) {
        this.repo = repo;
    }

    /* ---------- mapeamento manual ---------- */
    private static Turma toDomain(TurmaJpa j) {
        var t = new Turma(
            j.nome,
            j.anoLetivo,
            j.ativo,
            new ProfessorId(j.professorId)
        );
        if (j.id != null) {
            t.atribuirIdSeAusente(new TurmaId(j.id));
        }
        return t;
    }

    private static void fillJpaFromDomain(Turma d, TurmaJpa j) {
        j.nome        = d.getNome();
        j.anoLetivo   = d.getAnoLetivo();
        j.ativo       = d.isAtivo();
        j.professorId = d.getProfessor().value();
    }

    private TurmaJpa toJpa(Turma d) {
        final TurmaJpa j;
        if (d.getId() == null) {
            j = new TurmaJpa(); // INSERT
        } else {
            j = repo.findById(d.getId().value())
                    .orElseThrow(() -> new EntityNotFoundException("Turma não encontrada: id=" + d.getId().value()));
            // manter j.id coerente
        }
        fillJpaFromDomain(d, j);
        return j;
    }

    /* ---------- contrato do domínio ---------- */

    @Transactional
    @Override
    public TurmaId salvar(Turma t) {
        var salvo = repo.save(toJpa(t));
        if (t.getId() == null) {
            t.atribuirIdSeAusente(new TurmaId(salvo.id));
        }
        return new TurmaId(salvo.id);
    }

    @Transactional(readOnly = true)
    @Override
    public Turma porId(TurmaId id) {
        var j = repo.findById(id.value()).orElseThrow(() ->
            new EntityNotFoundException("Turma não encontrada: id=" + id.value()));
        return toDomain(j);
    }

    @Transactional
    @Override
    public void remover(TurmaId id) {
        repo.deleteById(id.value());
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existeNomeNoAno(String nome, int anoLetivo) {
        return repo.existsByNomeIgnoreCaseAndAnoLetivo(nome, anoLetivo);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean possuiAlunosAtivos(TurmaId id) {
        return repo.temAlunosAtivos(id.value());
    }

    @Transactional(readOnly = true)
    @Override
    public boolean possuiSimulados(TurmaId id) {
        return repo.temSimulados(id.value());
    }

    @Transactional(readOnly = true)
    @Override
    public boolean possuiSimuladosEmEdicao(TurmaId id) {
        return repo.temSimuladosEmEdicao(id.value());
    }

    @Transactional(readOnly = true)
    @Override
    public boolean possuiSimuladosFinalizados(TurmaId id) {
        return repo.temSimuladosFinalizados(id.value());
    }

    @Transactional(readOnly = true)
    @Override
    public int anoLetivoDe(TurmaId id) {
        Integer ano = repo.anoLetivoDe(id.value());
        if (ano == null) throw new EntityNotFoundException("Turma não encontrada: id=" + id.value());
        return ano;
    }
}
