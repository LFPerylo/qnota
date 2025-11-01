package dev.com.qnota.infraestrutura.persistencia.jpa;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;               // <- use SEMPRE esta
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.com.qnota.dominio.principal.aluno.*;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.turma.TurmaId;
import jakarta.persistence.*;

/* =========================
 * ENTIDADES / EMBEDDABLES
 * ========================= */

@Entity
@Table(name = "alunos")
class AlunoJpa {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    Integer id;

    String nome;

    @Column(name = "dataNascimento")
    LocalDate dataNascimento;

    boolean ativo;

    @Column(name = "turma_id")
    Integer turmaId;

    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<AlunoResponsavelJpa> responsaveis = new LinkedHashSet<>();

    @OneToMany(mappedBy = "aluno", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<NotaAlunoJpa> notas = new LinkedHashSet<>();
}

@Embeddable
class AlunoRespIdJpa {
    @Column(name = "aluno_id")      Integer alunoId;
    @Column(name = "responsavel_id") Integer responsavelId;

    @Override public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof AlunoRespIdJpa that)) return false;
        return Objects.equals(alunoId, that.alunoId) &&
               Objects.equals(responsavelId, that.responsavelId);
    }
    @Override public int hashCode(){ return Objects.hash(alunoId, responsavelId); }
}

@Entity
@Table(name = "aluno_responsaveis")
class AlunoResponsavelJpa {
    @EmbeddedId
    AlunoRespIdJpa id;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("alunoId")
    @JoinColumn(name = "aluno_id")
    AlunoJpa aluno;

    boolean principal;
}

@Embeddable
class NotaIdJpa {
    @Column(name = "aluno_id")     Integer alunoId;
    @Column(name = "simulado_id")  Integer simuladoId;
    @Column(name = "disciplina_id")Integer disciplinaId;

    @Override public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof NotaIdJpa that)) return false;
        return Objects.equals(alunoId, that.alunoId) &&
               Objects.equals(simuladoId, that.simuladoId) &&
               Objects.equals(disciplinaId, that.disciplinaId);
    }
    @Override public int hashCode(){ return Objects.hash(alunoId, simuladoId, disciplinaId); }
}

@Entity
@Table(name = "notas_do_aluno")
class NotaAlunoJpa {
    @EmbeddedId
    NotaIdJpa id;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("alunoId")
    @JoinColumn(name = "aluno_id")
    AlunoJpa aluno;

    double valor;

    @Column(name = "dataLancamento")
    LocalDateTime dataLancamento;

    @OneToMany(mappedBy = "nota", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<JustificativaJpa> justificativas = new LinkedHashSet<>();
}

@Entity
@Table(name = "justificativas")
class JustificativaJpa {
    @Id @GeneratedValue(strategy = IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "aluno_id",      referencedColumnName = "aluno_id"),
        @JoinColumn(name = "simulado_id",   referencedColumnName = "simulado_id"),
        @JoinColumn(name = "disciplina_id", referencedColumnName = "disciplina_id")
    })
    NotaAlunoJpa nota;

    @Column(name = "professor_id") Integer professorId;
    @Column(name = "notaAnterior") double notaAnterior;
    @Column(name = "notaCorrigida")double notaCorrigida;
    String texto;
    @Column(name = "dataHora")     LocalDateTime dataHora;
}

/* ================
 * JPA REPOSITORY
 * ================ */

interface AlunoJpaRepository extends JpaRepository<AlunoJpa, Integer> {

    // Carrega tudo (vínculos + notas + justificativas) — uso em porId
    @Query("""
        SELECT a FROM AlunoJpa a
        LEFT JOIN FETCH a.responsaveis r
        LEFT JOIN FETCH a.notas n
        LEFT JOIN FETCH n.justificativas j
        WHERE a.id = :id
    """)
    Optional<AlunoJpa> carregarComTudo(@Param("id") Integer id);

    // Versão para lista por turma com coleções (evita N+1) — uso em porTurma
    @Query("""
        SELECT DISTINCT a FROM AlunoJpa a
        LEFT JOIN FETCH a.responsaveis r
        LEFT JOIN FETCH a.notas n
        LEFT JOIN FETCH n.justificativas j
        WHERE a.turmaId = :turma
    """)
    List<AlunoJpa> findByTurmaIdFetchAll(@Param("turma") Integer turmaId);

    // ------------- regras auxiliares -------------
    @Query(value = """
        SELECT COUNT(*) 
          FROM alunos 
         WHERE nome = :nome AND dataNascimento = :data AND turma_id = :turmaId
    """, nativeQuery = true)
    long countMesmoNomeNascimentoTurma(@Param("nome") String nome,
                                       @Param("data") LocalDate data,
                                       @Param("turmaId") int turmaId);

    @Query(value = "SELECT COUNT(*) FROM aluno_responsaveis WHERE aluno_id = :alunoId", nativeQuery = true)
    long countResponsaveis(@Param("alunoId") int alunoId);

    @Query(value = "SELECT COUNT(*) FROM aluno_responsaveis WHERE responsavel_id = :respId", nativeQuery = true)
    long countVinculosPorResponsavel(@Param("respId") int responsavelId);

    @Modifying
    @Query(value = "UPDATE alunos SET turma_id = :nova WHERE id = :aluno", nativeQuery = true)
    int updateTurma(@Param("aluno") int alunoId, @Param("nova") int novaTurmaId);

    // EXISTS de forma portável para MySQL -> retorna 0/1
    @Query(value = "SELECT CASE WHEN EXISTS(SELECT 1 FROM notas_do_aluno WHERE aluno_id = :alunoId) THEN 1 ELSE 0 END",
           nativeQuery = true)
    int temNotasInt(@Param("alunoId") int alunoId);

    @Query(value = "SELECT CASE WHEN EXISTS(SELECT 1 FROM notas_do_aluno WHERE simulado_id = :simuladoId) THEN 1 ELSE 0 END",
           nativeQuery = true)
    int existeNotaParaSimuladoInt(@Param("simuladoId") int simuladoId);
}

/* ===========================
 * REPOSITORIO (IMPL DOMÍNIO)
 * =========================== */

@Repository
class AlunoRepositorioImpl implements AlunoRepositorio {

    @Autowired AlunoJpaRepository repositorio;
    @Autowired JpaMapeador mapeador;

    @Transactional
    @Override
    public AlunoId salvar(Aluno aluno) {
        var jpa = mapeador.map(aluno, AlunoJpa.class);
        var salvo = repositorio.save(jpa);
        if (aluno.getId() == null) {
            aluno.atribuirIdSeAusente(new AlunoId(salvo.id));
        }
        return new AlunoId(salvo.id);
    }

    @Transactional(readOnly = true)
    @Override
    public Aluno porId(AlunoId id) {
        var jpa = repositorio.carregarComTudo(id.value())
                 .orElseGet(() -> repositorio.findById(id.value()).orElseThrow());
        return mapeador.map(jpa, Aluno.class);
    }

    @Transactional
    @Override
    public void remover(AlunoId id) {
        repositorio.deleteById(id.value());
    }

    @Override
    public boolean existeOutroComMesmoNomeENascimentoNaTurma(String nome, LocalDate data, TurmaId turmaId) {
        return repositorio.countMesmoNomeNascimentoTurma(nome, data, turmaId.value()) > 0;
    }

    @Override
    public int contarResponsaveis(AlunoId id) {
        return (int) repositorio.countResponsaveis(id.value());
    }

    @Override
    public boolean existeVinculo(AlunoId id) {
        return repositorio.countResponsaveis(id.value()) > 0;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Aluno> porTurma(TurmaId turmaId) {
        var lista = repositorio.findByTurmaIdFetchAll(turmaId.value());
        return lista.stream().map(j -> mapeador.map(j, Aluno.class)).collect(Collectors.toList());
    }

    @Override
    public void alterarTurma(AlunoId alunoId, TurmaId novaTurmaId) {
        repositorio.updateTurma(alunoId.value(), novaTurmaId.value());
    }

    @Override
    public boolean temNotas(AlunoId alunoId) {
        return repositorio.temNotasInt(alunoId.value()) == 1;
    }

    @Override
    public boolean existeNotaParaSimulado(SimuladoId simuladoId) {
        return repositorio.existeNotaParaSimuladoInt(simuladoId.value()) == 1;
    }
}
