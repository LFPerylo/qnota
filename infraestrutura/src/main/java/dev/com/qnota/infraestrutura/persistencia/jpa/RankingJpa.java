package dev.com.qnota.infraestrutura.persistencia.jpa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.com.qnota.aplicacao.principal.ranking.RankingRepositorioAplicacao;
import dev.com.qnota.aplicacao.principal.ranking.RankingResumo;
import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.ranking.Ranking;
import dev.com.qnota.dominio.principal.ranking.RankingId;
import dev.com.qnota.dominio.principal.ranking.RankingRepositorio;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/* =========================
   ENTIDADES JPA
   ========================= */

@Entity
@Table(name = "rankings")
class RankingJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(nullable = false)
    Boolean congelado = Boolean.FALSE;

    @Column(name = "simulado_id", nullable = false, unique = true)
    Integer simuladoId;

    @jakarta.persistence.OneToMany(mappedBy = "ranking",
        cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    Set<RankingLinhaJpa> linhas = new LinkedHashSet<>();
}

@Embeddable
class RankingLinhaIdJpa implements Serializable {
    @Column(name = "ranking_id", nullable = false)
    Integer rankingId;

    @Column(name = "aluno_id", nullable = false)
    Integer alunoId;

    public RankingLinhaIdJpa() {}

    public RankingLinhaIdJpa(Integer rankingId, Integer alunoId) {
        this.rankingId = rankingId;
        this.alunoId = alunoId;
    }

    @Override public int hashCode() { return java.util.Objects.hash(rankingId, alunoId); }
    @Override public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RankingLinhaIdJpa other)) return false;
        return java.util.Objects.equals(rankingId, other.rankingId)
            && java.util.Objects.equals(alunoId, other.alunoId);
    }
}

@Entity
@Table(name = "ranking_linhas",
       uniqueConstraints = @UniqueConstraint(name = "ux_rl_rank_pos",
                                             columnNames = {"ranking_id","posicao"}))
class RankingLinhaJpa {
    @EmbeddedId
    RankingLinhaIdJpa id;

    @jakarta.persistence.MapsId("rankingId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ranking_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_rl_rank"))
    RankingJpa ranking;

    @Column(name = "media", nullable = false)
    Double media;

    @Column(name = "posicao", nullable = false)
    Integer posicao;

    public RankingLinhaJpa() {}

    public RankingLinhaJpa(RankingJpa ranking, Integer alunoId, double media, int posicao) {
        this.ranking = ranking;
        this.id = new RankingLinhaIdJpa(ranking.id, alunoId);
        this.media = media;
        this.posicao = posicao;
    }
}

/* =========================
   REPOSITÓRIOS SPRING DATA
   ========================= */

interface RankingJpaRepository extends JpaRepository<RankingJpa, Integer> {
    Optional<RankingJpa> findBySimuladoId(Integer simuladoId);

    boolean existsBySimuladoId(Integer simuladoId);

    @Modifying
    @Transactional
    @Query("update RankingJpa r set r.congelado = TRUE where r.simuladoId = :simuladoId")
    int marcarCongeladoPorSimulado(@org.springframework.data.repository.query.Param("simuladoId") Integer simuladoId);

    // Query para resumos com informações do simulado e quantidade de linhas
    @Query(value = """
        SELECT r.id AS id,
               r.simulado_id AS simuladoId,
               TO_CHAR(s.data_aplicacao, 'DD/MM/YYYY') AS simuladoDataAplicacao,
               t.nome AS simuladoTurmaNome,
               r.congelado AS congelado,
               COUNT(rl.aluno_id) AS quantidadeLinhas
          FROM rankings r
     LEFT JOIN simulados s ON s.id = r.simulado_id
     LEFT JOIN turmas t ON t.id = s.turma_id
     LEFT JOIN ranking_linhas rl ON rl.ranking_id = r.id
       GROUP BY r.id, r.simulado_id, s.data_aplicacao, t.nome, r.congelado
       ORDER BY s.data_aplicacao DESC
        """, nativeQuery = true)
    List<RankingResumo> findRankingResumoByOrderByDataAplicacaoDesc();
}

interface RankingLinhaJpaRepository extends JpaRepository<RankingLinhaJpa, RankingLinhaIdJpa> {

    @Modifying
    @Transactional
    @Query("delete from RankingLinhaJpa rl where rl.ranking.id = :rankingId")
    int deleteAllByRankingId(@org.springframework.data.repository.query.Param("rankingId") Integer rankingId);

    @Query("select rl from RankingLinhaJpa rl where rl.ranking.id = :rankingId order by rl.posicao asc")
    List<RankingLinhaJpa> findAllByRankingIdOrderByPosicaoAsc(@org.springframework.data.repository.query.Param("rankingId") Integer rankingId);
}

/* =========================
   IMPLEMENTAÇÃO DO REPOSITÓRIO DE DOMÍNIO
   ========================= */

@Repository
class RankingRepositorioImpl implements RankingRepositorio, RankingRepositorioAplicacao {

    @Autowired RankingJpaRepository rankingRepo;
    @Autowired RankingLinhaJpaRepository linhaRepo;

    @Transactional
    protected RankingJpa garantirRankingDoSimulado(@NonNull SimuladoId simulado) {
        return rankingRepo.findBySimuladoId(simulado.value())
                .orElseGet(() -> {
                    var r = new RankingJpa();
                    r.simuladoId = simulado.value();
                    r.congelado = Boolean.FALSE;
                    return rankingRepo.save(r);
                });
    }

    @Override
    @Transactional
    public void limpar(SimuladoId simulado) {
        var r = garantirRankingDoSimulado(simulado);
        linhaRepo.deleteAllByRankingId(r.id);
        if (Boolean.TRUE.equals(r.congelado)) {
            r.congelado = Boolean.FALSE;
            rankingRepo.save(r);
        }
    }

    @Override
    @Transactional
    public void salvarPosicoes(SimuladoId simulado, List<Ranking.Linha> linhas) {
        var r = garantirRankingDoSimulado(simulado);
        linhaRepo.deleteAllByRankingId(r.id);

        if (linhas != null) {
            for (var l : linhas) {
                linhaRepo.save(new RankingLinhaJpa(r, l.aluno().value(), l.media(), l.posicao()));
            }
        }
        if (Boolean.TRUE.equals(r.congelado)) {
            r.congelado = Boolean.FALSE;
            rankingRepo.save(r);
        }
    }

    @Override
    @Transactional
    public void congelar(SimuladoId simulado) {
        var updated = rankingRepo.marcarCongeladoPorSimulado(simulado.value());
        if (updated == 0) {
            var r = garantirRankingDoSimulado(simulado);
            r.congelado = Boolean.TRUE;
            rankingRepo.save(r);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaCongelado(SimuladoId simulado) {
        return rankingRepo.findBySimuladoId(simulado.value())
                .map(r -> Boolean.TRUE.equals(r.congelado))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ranking.Linha> carregar(SimuladoId simulado) {
        var opt = rankingRepo.findBySimuladoId(simulado.value());
        if (opt.isEmpty()) return List.of();

        var r = opt.get();
        var jpaLinhas = linhaRepo.findAllByRankingIdOrderByPosicaoAsc(r.id);
        var out = new ArrayList<Ranking.Linha>(jpaLinhas.size());
        for (var jl : jpaLinhas) {
            out.add(new Ranking.Linha(new AlunoId(jl.id.alunoId), jl.media, jl.posicao));
        }
        return out;
    }

    @Override
    @Transactional
    public Ranking salvar(Ranking ranking) {
        var simId = ranking.getSimulado();
        var r = garantirRankingDoSimulado(simId);

        linhaRepo.deleteAllByRankingId(r.id);
        for (var l : ranking.getLinhas()) {
            linhaRepo.save(new RankingLinhaJpa(r, l.aluno().value(), l.media(), l.posicao()));
        }

        if (ranking.isCongelado()) {
            r.congelado = Boolean.TRUE;
            rankingRepo.save(r);
        } else if (Boolean.TRUE.equals(r.congelado)) {
            r.congelado = Boolean.FALSE;
            rankingRepo.save(r);
        }

        if (ranking.getId() == null) {
            ranking.atribuirIdSeAusente(new RankingId(r.id));
        }
        return ranking;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Ranking> carregarAgregado(SimuladoId simulado) {
        var opt = rankingRepo.findBySimuladoId(simulado.value());
        if (opt.isEmpty()) return Optional.of(new Ranking(simulado, List.of()));
        var r = opt.get();
        var linhas = carregar(simulado);
        var agg = new Ranking(simulado, linhas);
        if (Boolean.TRUE.equals(r.congelado)) agg.congelar();
        agg.atribuirIdSeAusente(new RankingId(r.id));
        return Optional.of(agg);
    }

    /* ---------- contrato da aplicação ---------- */

    @Transactional(readOnly = true)
    @Override
    public List<RankingResumo> pesquisarResumos() {
        return rankingRepo.findRankingResumoByOrderByDataAplicacaoDesc();
    }
}
