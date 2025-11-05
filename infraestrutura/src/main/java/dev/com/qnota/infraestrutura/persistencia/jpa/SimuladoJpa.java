// Título da análise: QNota - Infra (Simulado): Entidades JPA + Repositories por entidade + RepositorioImpl (mapeamento manual)

package dev.com.qnota.infraestrutura.persistencia.jpa;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query; // Spring Data
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.simulado.Simulado;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.simulado.SimuladoRepositorio;
import dev.com.qnota.dominio.principal.turma.TurmaId;
import jakarta.persistence.*;

// =====================
// ENTIDADES JPA
// =====================
@Entity
@Table(name = "simulados")
class SimuladoJpa {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  Integer id;

  @Column(name = "data_aplicacao", nullable = false)
  LocalDate dataAplicacao;

  // armazenado como VARCHAR ('EM_EDICAO' | 'FINALIZADO')
  @Column(nullable = false)
  String status;

  @Column(name = "turma_id", nullable = false)
  Integer turmaId;
}

/** Join-table com peso da disciplina no simulado. */
@Entity
@Table(name = "simulado_disciplinas")
class SimuladoDisciplinaJpa {

  @EmbeddedId
  SimuladoDisciplinaPk id;

  @Column(nullable = false)
  double peso;

  @Embeddable
  public static class SimuladoDisciplinaPk implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "simulado_id", nullable = false)
    Integer simuladoId;

    @Column(name = "disciplina_id", nullable = false)
    Integer disciplinaId;

    public SimuladoDisciplinaPk() {}
    public SimuladoDisciplinaPk(Integer simuladoId, Integer disciplinaId) {
      this.simuladoId = simuladoId; this.disciplinaId = disciplinaId;
    }
    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof SimuladoDisciplinaPk that)) return false;
      return Objects.equals(simuladoId, that.simuladoId)
          && Objects.equals(disciplinaId, that.disciplinaId);
    }
    @Override public int hashCode() { return Objects.hash(simuladoId, disciplinaId); }
  }
}

// =====================
// JPA REPOSITORIES (um por entidade)
// =====================
interface SimuladoJpaRepository extends JpaRepository<SimuladoJpa, Integer> {

  @Query(value = """
      SELECT COUNT(*) FROM simulados
       WHERE turma_id = :turmaId AND status = 'EM_EDICAO'
      """, nativeQuery = true)
  int countEmEdicaoPorTurma(@Param("turmaId") int turmaId);

  @Query(value = """
      SELECT * FROM simulados
       WHERE turma_id = :turmaId
       ORDER BY data_aplicacao DESC
      """, nativeQuery = true)
  List<SimuladoJpa> listarPorTurma(@Param("turmaId") int turmaId);

  // Professor possui algum simulado FINALIZADO?
  @Query(value = """
      SELECT CASE WHEN EXISTS (
        SELECT 1
          FROM simulados s
          JOIN turmas   t ON t.id = s.turma_id
         WHERE t.professor_id = :profId
           AND s.status       = 'FINALIZADO'
      ) THEN 1 ELSE 0 END
      """, nativeQuery = true)
  int temFinalizadoParaProfessor(@Param("profId") int professorId);

  // Aluno possui simulado FINALIZADO (com nota)?
  @Query(value = """
      SELECT CASE WHEN EXISTS (
        SELECT 1
          FROM notas_do_aluno n
          JOIN simulados     s ON s.id = n.simulado_id
         WHERE n.aluno_id = :alunoId
           AND s.status   = 'FINALIZADO'
      ) THEN 1 ELSE 0 END
      """, nativeQuery = true)
  int temFinalizadoParaAluno(@Param("alunoId") int alunoId);

  // Há simulados EM_EDICAO do aluno com notas pendentes?
  @Query(value = """
      SELECT CASE WHEN EXISTS (
        SELECT 1
          FROM simulados s
          JOIN alunos   a ON a.turma_id = s.turma_id
         WHERE a.id      = :alunoId
           AND s.status  = 'EM_EDICAO'
           AND (
               (SELECT COUNT(*) FROM simulado_disciplinas sd WHERE sd.simulado_id = s.id)
               >
               (SELECT COUNT(*) FROM notas_do_aluno n
                 WHERE n.aluno_id = a.id AND n.simulado_id = s.id)
           )
      ) THEN 1 ELSE 0 END
      """, nativeQuery = true)
  int temPendenciaEmEdicao(@Param("alunoId") int alunoId);

  // Todas as notas lançadas para TODOS os alunos ativos da turma?
  @Query(value = """
      SELECT CASE WHEN NOT EXISTS (
        SELECT 1
          FROM alunos a
         WHERE a.turma_id = (SELECT turma_id FROM simulados WHERE id = :simId)
           AND a.ativo = TRUE
           AND EXISTS (
                SELECT 1
                  FROM simulado_disciplinas sd
                 WHERE sd.simulado_id = :simId
                   AND NOT EXISTS (
                        SELECT 1
                          FROM notas_do_aluno n
                         WHERE n.aluno_id = a.id
                           AND n.simulado_id = :simId
                           AND n.disciplina_id = sd.disciplina_id
                   )
           )
      ) THEN 1 ELSE 0 END
      """, nativeQuery = true)
  int todasNotasLancadas(@Param("simId") int simuladoId);
}

interface SimuladoDisciplinaJpaRepository
    extends JpaRepository<SimuladoDisciplinaJpa, SimuladoDisciplinaJpa.SimuladoDisciplinaPk> {

  // Property path correto para @EmbeddedId (id.simuladoId)
  List<SimuladoDisciplinaJpa> findByIdSimuladoId(Integer simuladoId);

  @Modifying
  @Transactional
  void deleteByIdSimuladoId(Integer simuladoId);
}

// =====================
// IMPLEMENTAÇÃO DO REPOSITÓRIO DO DOMÍNIO (um por agregado)
// =====================
@Repository
class SimuladoRepositorioImpl implements SimuladoRepositorio {

  @Autowired SimuladoJpaRepository repositorio;
  @Autowired SimuladoDisciplinaJpaRepository sdRepo;

  /* -------- helpers de conversão -------- */
  private Simulado toDomain(SimuladoJpa jpa) {
    var linhas = sdRepo.findByIdSimuladoId(jpa.id);
    var disciplinas = linhas.stream()
        .map(sd -> new Simulado.DisciplinaPeso(new DisciplinaId(sd.id.disciplinaId), sd.peso))
        .collect(Collectors.toList());

    var s = new Simulado(
        jpa.dataAplicacao,
        Simulado.Status.valueOf(jpa.status),
        new TurmaId(jpa.turmaId),
        disciplinas
    );
    s.atribuirIdSeAusente(new SimuladoId(jpa.id));
    return s;
  }

  private SimuladoJpa toJpa(Simulado s) {
    var j = new SimuladoJpa();
    j.id            = (s.getId() != null ? s.getId().value() : null);
    j.dataAplicacao = s.getDataAplicacao();
    j.status        = s.getStatus().name();
    j.turmaId       = s.getTurma().value();
    return j;
  }

  /* -------- contrato do domínio -------- */

  @Transactional
  @Override
  public SimuladoId salvar(Simulado s) {
    // upsert do cabeçalho
    var salvo = repositorio.save(toJpa(s));
    if (s.getId() == null) {
      s.atribuirIdSeAusente(new SimuladoId(salvo.id));
    }

    // regrava pesos (limpa e insere)
    sdRepo.deleteByIdSimuladoId(salvo.id);
    if (s.getDisciplinas() != null) {
      for (var dp : s.getDisciplinas()) {
        var pk  = new SimuladoDisciplinaJpa.SimuladoDisciplinaPk(salvo.id, dp.disciplina().value());
        var row = new SimuladoDisciplinaJpa();
        row.id  = pk;
        row.peso = dp.peso();
        sdRepo.save(row);
      }
    }
    return new SimuladoId(salvo.id);
  }

  @Transactional(readOnly = true)
  @Override
  public Simulado porId(SimuladoId id) {
    var jpa = repositorio.findById(id.value()).orElseThrow();
    return toDomain(jpa);
  }

  @Transactional(readOnly = true)
  @Override
  public int contarEmEdicaoPorTurma(TurmaId turmaId) {
    return repositorio.countEmEdicaoPorTurma(turmaId.value());
  }

  @Transactional(readOnly = true)
  @Override
  public List<Simulado> listarPorTurma(TurmaId turmaId) {
    return repositorio.listarPorTurma(turmaId.value())
        .stream().map(this::toDomain).toList();
  }

  @Transactional(readOnly = true)
  @Override
  public Map<Integer, Double> pesosDoSimulado(SimuladoId id) {
    var linhas = sdRepo.findByIdSimuladoId(id.value());
    Map<Integer, Double> mapa = new LinkedHashMap<>();
    for (var l : linhas) {
      mapa.put(l.id.disciplinaId, l.peso);
    }
    return mapa;
  }

  @Transactional(readOnly = true)
  @Override
  public boolean todasNotasLancadas(SimuladoId id) {
    return repositorio.todasNotasLancadas(id.value()) == 1;
  }

  @Transactional(readOnly = true)
  @Override
  public boolean possuiSimuladoFinalizadoParaProfessor(
      dev.com.qnota.dominio.principal.professor.ProfessorId professorId) {
    return repositorio.temFinalizadoParaProfessor(professorId.value()) == 1;
  }

  @Transactional(readOnly = true)
  @Override
  public boolean possuiSimuladoFinalizadoParaAluno(
      dev.com.qnota.dominio.principal.aluno.AlunoId alunoId) {
    return repositorio.temFinalizadoParaAluno(alunoId.value()) == 1;
  }

  @Transactional(readOnly = true)
  @Override
  public boolean temNotasPendentesEmSimuladosEmEdicao(
      dev.com.qnota.dominio.principal.aluno.AlunoId alunoId) {
    return repositorio.temPendenciaEmEdicao(alunoId.value()) == 1;
  }

  @Transactional
  @Override
  public void remover(SimuladoId id) {
    sdRepo.deleteByIdSimuladoId(id.value());
    repositorio.deleteById(id.value());
  }
}
