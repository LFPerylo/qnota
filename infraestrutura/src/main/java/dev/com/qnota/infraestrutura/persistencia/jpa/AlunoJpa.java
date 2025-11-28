package dev.com.qnota.infraestrutura.persistencia.jpa;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;           // <- Spring Data
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.com.qnota.aplicacao.principal.aluno.AlunoRepositorioAplicacao;
import dev.com.qnota.aplicacao.principal.aluno.AlunoResumo;
import dev.com.qnota.dominio.principal.aluno.Aluno;
import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.aluno.AlunoRepositorio;

import java.util.List;
import dev.com.qnota.dominio.principal.aluno.Justificativa;
import dev.com.qnota.dominio.principal.aluno.NotaDoAluno;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.professor.ProfessorId;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.turma.TurmaId;

// ---- imports JPA SEM wildcard (evita conflito com @Query do Spring) ----
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.MapsId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/* =========================================================
 * ENTIDADES JPA (package-private)
 * ========================================================= */

@Entity
@Table(name = "alunos")
class AlunoJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(nullable = false)
    String nome;

    @Column(name = "datanascimento", nullable = false)
    LocalDate dataNascimento;

    @Column(nullable = false)
    Boolean ativo;

    @Column(name = "turma_id", nullable = false)
    Integer turmaId;

    @OneToMany(mappedBy = "aluno", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    Set<AlunoResponsavelJpa> responsaveis = new LinkedHashSet<>();

    @OneToMany(mappedBy = "aluno", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    Set<NotaAlunoJpa> notas = new LinkedHashSet<>();
}

@Embeddable
class AlunoRespIdJpa implements java.io.Serializable {
    @Column(name = "responsavel_id", nullable = false) Integer responsavelId;
    @Column(name = "aluno_id",       nullable = false) Integer alunoId;

    AlunoRespIdJpa() {}
    AlunoRespIdJpa(Integer alunoId, Integer responsavelId) {
        this.alunoId = alunoId;
        this.responsavelId = responsavelId;
    }
    @Override public int hashCode(){ return Objects.hash(alunoId, responsavelId); }
    @Override public boolean equals(Object o){
        if(this==o) return true;
        if(!(o instanceof AlunoRespIdJpa a)) return false;
        return Objects.equals(alunoId,a.alunoId) && Objects.equals(responsavelId,a.responsavelId);
    }
}

@Entity
@Table(name = "aluno_responsaveis")
class AlunoResponsavelJpa {
    @EmbeddedId AlunoRespIdJpa id;

    @MapsId("alunoId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aluno_id", nullable = false, foreignKey = @ForeignKey(name="fk_ar_aluno"))
    AlunoJpa aluno;

    @Column(nullable = false) Boolean principal = Boolean.FALSE;

    AlunoResponsavelJpa() {}
    AlunoResponsavelJpa(AlunoJpa aluno, Integer responsavelId, boolean principal){
        this.aluno = aluno;
        this.id = new AlunoRespIdJpa(aluno.id, responsavelId);
        this.principal = principal;
    }
}

@Embeddable
class NotaIdJpa implements java.io.Serializable {
    @Column(name="aluno_id",      nullable=false) Integer alunoId;
    @Column(name="simulado_id",   nullable=false) Integer simuladoId;
    @Column(name="disciplina_id", nullable=false) Integer disciplinaId;

    NotaIdJpa(){}
    NotaIdJpa(Integer aluno, Integer sim, Integer dis){
        this.alunoId=aluno; this.simuladoId=sim; this.disciplinaId=dis;
    }
    @Override public int hashCode(){ return Objects.hash(alunoId,simuladoId,disciplinaId); }
    @Override public boolean equals(Object o){
        if(this==o) return true;
        if(!(o instanceof NotaIdJpa n)) return false;
        return Objects.equals(alunoId,n.alunoId)
            && Objects.equals(simuladoId,n.simuladoId)
            && Objects.equals(disciplinaId,n.disciplinaId);
    }
}

@Entity
@Table(name = "notas_do_aluno")
class NotaAlunoJpa {
    @EmbeddedId NotaIdJpa id;

    @MapsId("alunoId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="aluno_id", nullable=false, foreignKey=@ForeignKey(name="fk_nota_aluno"))
    AlunoJpa aluno;

    @Column(nullable=false) Double valor;

    @Column(name="datalancamento", nullable=false)
    LocalDateTime dataLancamento;

    @OneToMany(mappedBy="nota", cascade=jakarta.persistence.CascadeType.ALL, orphanRemoval=true)
    Set<JustificativaJpa> justificativas = new LinkedHashSet<>();

    NotaAlunoJpa(){}
    NotaAlunoJpa(AlunoJpa aluno, Integer simuladoId, Integer disciplinaId, Double valor, LocalDateTime dataLancamento){
        this.aluno = aluno;
        this.id = new NotaIdJpa(aluno.id, simuladoId, disciplinaId);
        this.valor = valor;
        this.dataLancamento = dataLancamento;
    }
}

@Entity
@Table(name = "justificativas")
class JustificativaJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid")
    java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
        @JoinColumn(name="aluno_id",      referencedColumnName="aluno_id"),
        @JoinColumn(name="simulado_id",   referencedColumnName="simulado_id"),
        @JoinColumn(name="disciplina_id", referencedColumnName="disciplina_id")
    })
    NotaAlunoJpa nota;

    @Column(name="professor_id", nullable=false) Integer professorId;
    @Column(name="notaanterior", nullable=false) Double notaAnterior;
    @Column(name="notacorrigida", nullable=false) Double notaCorrigida;
    @Column(name="texto", nullable=false, columnDefinition="text") String texto;
    @Column(name="datahora", nullable=false) LocalDateTime dataHora;

    JustificativaJpa(){}
    JustificativaJpa(NotaAlunoJpa nota, Integer professorId, Double notaAnterior, Double notaCorrigida, String texto, LocalDateTime dataHora){
        this.nota = nota;
        this.professorId = professorId;
        this.notaAnterior = notaAnterior;
        this.notaCorrigida = notaCorrigida;
        this.texto = texto;
        this.dataHora = dataHora;
    }
}

/* =========================================================
 * REPOSITÓRIOS SPRING DATA (um por ENTIDADE)
 * ========================================================= */

interface AlunoJpaRepository extends JpaRepository<AlunoJpa, Integer> {

    @Query(value = """
        select exists(
          select 1 from alunos a
          where lower(a.nome) = lower(:nome)
            and a.datanascimento = :data
            and a.turma_id = :turmaId
        )
        """, nativeQuery = true)
    boolean existsHomonimoMesmoNascimentoNaTurma(@Param("nome") String nome,
                                                 @Param("data") LocalDate data,
                                                 @Param("turmaId") int turmaId);

    List<AlunoJpa> findByTurmaId(Integer turmaId);

    @Modifying
    @Query(value = "update alunos set turma_id = :nova where id = :alunoId", nativeQuery = true)
    int alterarTurma(@Param("alunoId") int alunoId, @Param("nova") int novaTurmaId);

    // Query para resumos com informações da turma e quantidade de responsáveis
    @Query(value = """
        SELECT a.id AS id,
               a.nome AS nome,
               a.datanascimento AS dataNascimento,
               a.ativo AS ativo,
               a.turma_id AS turmaId,
               t.nome AS turmaNome,
               COUNT(ar.responsavel_id) AS quantidadeResponsaveis
          FROM alunos a
     LEFT JOIN turmas t ON t.id = a.turma_id
     LEFT JOIN aluno_responsaveis ar ON ar.aluno_id = a.id
      GROUP BY a.id, a.nome, a.datanascimento, a.ativo, a.turma_id, t.nome
      ORDER BY a.nome
        """, nativeQuery = true)
    List<AlunoResumo> findAlunoResumoByOrderByNome();
}

interface AlunoResponsavelJpaRepository extends JpaRepository<AlunoResponsavelJpa, AlunoRespIdJpa> {
    int countByIdAlunoId(int alunoId);
    boolean existsByIdAlunoId(int alunoId);
}

interface NotaAlunoJpaRepository extends JpaRepository<NotaAlunoJpa, NotaIdJpa> {
    boolean existsByIdAlunoId(int alunoId);
    boolean existsByIdSimuladoId(int simuladoId);
}

interface JustificativaJpaRepository extends JpaRepository<JustificativaJpa, java.util.UUID> {}

/* =========================================================
 * IMPLEMENTAÇÃO DO REPOSITÓRIO DO AGREGADO
 * ========================================================= */

@Repository
class AlunoRepositorioImpl implements AlunoRepositorio, AlunoRepositorioAplicacao {

    private final AlunoJpaRepository alunoRepo;
    private final AlunoResponsavelJpaRepository respRepo;
    private final NotaAlunoJpaRepository notaRepo;

    @Autowired
    AlunoRepositorioImpl(AlunoJpaRepository alunoRepo,
                          AlunoResponsavelJpaRepository respRepo,
                          NotaAlunoJpaRepository notaRepo) {
        this.alunoRepo = alunoRepo;
        this.respRepo = respRepo;
        this.notaRepo = notaRepo;
    }

    /* ---------- helpers de (des)mapa ---------- */

    private static ResponsavelId principalDe(Set<AlunoResponsavelJpa> vinculos) {
        return vinculos.stream()
                .filter(v -> Boolean.TRUE.equals(v.principal))
                .findFirst()
                .map(v -> new ResponsavelId(v.id.responsavelId))
                .orElse(null);
    }

    private static List<ResponsavelId> responsaveisDe(Set<AlunoResponsavelJpa> vinculos) {
        return vinculos.stream()
                .map(v -> new ResponsavelId(v.id.responsavelId))
                .collect(Collectors.toList());
    }

    /** Hidrata notas/justificativas no agregado via métodos package-private. */
    private static void hidratarNotas(Aluno agregado, Set<NotaAlunoJpa> notasJpa) {
        try {
            Method addNota = Aluno.class.getDeclaredMethod(
                    "adicionarNotaInterna", SimuladoId.class, DisciplinaId.class, double.class);
            addNota.setAccessible(true);

            Method addJust = Aluno.class.getDeclaredMethod(
                    "adicionarJustificativaInterna", SimuladoId.class, DisciplinaId.class, Justificativa.class);
            addJust.setAccessible(true);

            for (var n : notasJpa) {
                var simId = new SimuladoId(n.id.simuladoId);
                var disId = new DisciplinaId(n.id.disciplinaId);
                addNota.invoke(agregado, simId, disId, n.valor);

                if (n.justificativas != null) {
                    for (var j : n.justificativas) {
                        var jj = new Justificativa(
                                j.notaAnterior, j.notaCorrigida, j.texto, j.dataHora,
                                new ProfessorId(j.professorId));
                        addJust.invoke(agregado, simId, disId, jj);
                    }
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Falha ao hidratar notas/justificativas no agregado Aluno.", e);
        }
    }

    private static Aluno toDomain(AlunoJpa j) {
        var listaResp = responsaveisDe(j.responsaveis);
        var principal = principalDe(j.responsaveis);

        var aluno = new Aluno(
                j.nome,
                j.dataNascimento,
                Boolean.TRUE.equals(j.ativo),
                new TurmaId(j.turmaId),
                listaResp,
                principal
        );
        if (j.id != null) aluno.atribuirIdSeAusente(new AlunoId(j.id));

        if (j.notas != null && !j.notas.isEmpty()) {
            hidratarNotas(aluno, j.notas);
        }
        return aluno;
    }

    private static void preencherVinculosJpa(AlunoJpa j, Aluno d) {
        j.responsaveis.clear();
        var principal = d.getResponsavelPrincipal();
        for (var v : d.getVinculos()) {
            var rid = v.responsavel().value();
            boolean isPrincipal = (principal != null && principal.value() == rid);
            j.responsaveis.add(new AlunoResponsavelJpa(j, rid, isPrincipal));
        }
    }

    private static void preencherNotasJpa(AlunoJpa j, Collection<NotaDoAluno> notas) {
        j.notas.clear();
        for (var n : notas) {
            var nj = new NotaAlunoJpa(
                    j,
                    n.getSimuladoId().value(),
                    n.getDisciplinaId().value(),
                    n.getValor(),
                    n.getDataLancamento()
            );
            if (n.getJustificativas() != null) {
                for (var jj : n.getJustificativas()) {
                    var jpaJ = new JustificativaJpa(
                            nj,
                            jj.getProfessor().value(),
                            jj.getNotaAnterior(),
                            jj.getNotaCorrigida(),
                            jj.getTexto(),
                            jj.getDataHora()
                    );
                    nj.justificativas.add(jpaJ);
                }
            }
            j.notas.add(nj);
        }
    }

    private AlunoJpa toJpa(Aluno d) {
        final AlunoJpa j;
        if (d.getId() == null) {
            j = new AlunoJpa();
        } else {
            j = alunoRepo.findById(d.getId().value())
                    .orElseThrow(() -> new EntityNotFoundException("Aluno não encontrado: id=" + d.getId().value()));
        }
        j.nome = d.getNome();
        j.dataNascimento = d.getDataNascimento();
        j.ativo = d.isAtivo();
        j.turmaId = d.getTurma().value();

        preencherVinculosJpa(j, d);
        preencherNotasJpa(j, d.getNotas());

        return j;
    }

    /* ---------- contrato do domínio ---------- */

    @Override
    @Transactional
    public AlunoId salvar(Aluno aluno) {
        var j = toJpa(aluno);
        j = alunoRepo.save(j);
        if (aluno.getId() == null) {
            aluno.atribuirIdSeAusente(new AlunoId(j.id));
        }
        return new AlunoId(j.id);
    }

    @Override
    @Transactional(readOnly = true)
    public Aluno porId(AlunoId id) {
        var j = alunoRepo.findById(id.value())
                .orElseThrow(() -> new EntityNotFoundException("Aluno não encontrado: id=" + id.value()));
        return toDomain(j);
    }

    @Override
    @Transactional
    public void remover(AlunoId id) {
        try {
            alunoRepo.deleteById(id.value());
        } catch (EmptyResultDataAccessException ignore) { }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeOutroComMesmoNomeENascimentoNaTurma(String nome, LocalDate data, TurmaId turmaId) {
        if (nome == null || data == null || turmaId == null) return false;
        return alunoRepo.existsHomonimoMesmoNascimentoNaTurma(nome.trim(), data, turmaId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public int contarResponsaveis(AlunoId id) {
        return respRepo.countByIdAlunoId(id.value());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeVinculo(AlunoId id) {
        return respRepo.existsByIdAlunoId(id.value());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Aluno> porTurma(TurmaId turmaId) {
        return alunoRepo.findByTurmaId(turmaId.value()).stream()
                .map(AlunoRepositorioImpl::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void alterarTurma(AlunoId alunoId, TurmaId novaTurmaId) {
        alunoRepo.alterarTurma(alunoId.value(), novaTurmaId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean temNotas(AlunoId alunoId) {
        return notaRepo.existsByIdAlunoId(alunoId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeNotaParaSimulado(SimuladoId simuladoId) {
        return notaRepo.existsByIdSimuladoId(simuladoId.value());
    }

    /* ---------- contrato da aplicação ---------- */

    @Transactional(readOnly = true)
    @Override
    public List<AlunoResumo> pesquisarResumos() {
        return alunoRepo.findAlunoResumoByOrderByNome();
    }
}
