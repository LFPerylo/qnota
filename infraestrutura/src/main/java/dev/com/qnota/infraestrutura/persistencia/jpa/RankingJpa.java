package dev.com.qnota.infraestrutura.persistencia.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.ranking.Ranking;
import dev.com.qnota.dominio.principal.ranking.RankingRepositorio;
import dev.com.qnota.dominio.principal.ranking.RankingId;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

/* =========================================================
 * Entidades JPA
 * ========================================================= */

@Entity
@Table(name = "rankings", indexes = {
  @Index(name = "uk_rankings_simulado", columnList = "simulado_id", unique = true)
})
class RankingJpa {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Integer id;

  @Column(name = "simulado_id", nullable = false)
  Integer simuladoId;

  @Column(name = "congelado", nullable = false)
  Boolean congelado;
}

@Entity
@Table(name = "ranking_linhas", indexes = {
  @Index(name = "idx_ranklin_sim_pos", columnList = "simulado_id,posicao")
})
class RankingLinhaJpa {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Integer id;

  @Column(name = "simulado_id", nullable = false)
  Integer simuladoId;

  @Column(name = "aluno_id", nullable = false)
  Integer alunoId;

  @Column(name = "media", nullable = false)
  Double media;

  @Column(name = "posicao", nullable = false)
  Integer posicao;
}

/* =========================================================
 * Repositórios Spring Data
 * ========================================================= */

interface RankingJpaRepository extends JpaRepository<RankingJpa, Integer> {
  Optional<RankingJpa> findBySimuladoId(Integer simuladoId);
  boolean existsBySimuladoIdAndCongeladoTrue(Integer simuladoId);

  @Modifying
  @Query("update RankingJpa r set r.congelado=true where r.simuladoId = :simId")
  int marcarCongelado(Integer simId);

  @Modifying
  @Query("update RankingJpa r set r.congelado=false where r.simuladoId = :simId")
  int desmarcarCongelado(Integer simId);
}

interface RankingLinhaJpaRepository extends JpaRepository<RankingLinhaJpa, Integer> {
  List<RankingLinhaJpa> findBySimuladoIdOrderByPosicaoAsc(Integer simuladoId);

  @Modifying
  @Query("delete from RankingLinhaJpa l where l.simuladoId = :simId")
  int apagarPorSimulado(Integer simId);
}

/* =========================================================
 * Implementação do RankingRepositorio
 * ========================================================= */

@Repository
@Transactional
class RankingRepositorioImpl implements RankingRepositorio {

  @Autowired RankingJpaRepository rankingRepo;
  @Autowired RankingLinhaJpaRepository linhaRepo;
  @Autowired JpaMapeador mapeador;

  /* util: garante linha em 'rankings' para o simulado informado */
  private RankingJpa garantirCabecalho(Integer simId) {
    return rankingRepo.findBySimuladoId(simId)
        .orElseGet(() -> {
          var r = new RankingJpa();
          r.simuladoId = simId;
          r.congelado  = Boolean.FALSE;
          return rankingRepo.save(r);
        });
  }

  @Override
  public void limpar(SimuladoId simulado) {
    Integer simId = simulado.value();
    linhaRepo.apagarPorSimulado(simId);
    rankingRepo.desmarcarCongelado(simId); // voltar a “não congelado”
  }

  @Override
  public void salvarPosicoes(SimuladoId simulado, List<Ranking.Linha> linhas) {
    Integer simId = simulado.value();
    garantirCabecalho(simId);

    // Apaga antigas (em caso de regravação direta)
    linhaRepo.apagarPorSimulado(simId);

    // Persiste novas
    var entidades = linhas.stream().map(l -> {
      var j = new RankingLinhaJpa();
      j.simuladoId = simId;
      j.alunoId    = l.aluno().value();
      j.media      = l.media();
      j.posicao    = l.posicao();
      return j;
    }).toList();

    linhaRepo.saveAll(entidades);
  }

  @Override
  public void congelar(SimuladoId simulado) {
    Integer simId = simulado.value();
    garantirCabecalho(simId);
    rankingRepo.marcarCongelado(simId);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean estaCongelado(SimuladoId simulado) {
    return rankingRepo.existsBySimuladoIdAndCongeladoTrue(simulado.value());
  }

  @Override
  @Transactional(readOnly = true)
  public List<Ranking.Linha> carregar(SimuladoId simulado) {
    Integer simId = simulado.value();
    var linhas = linhaRepo.findBySimuladoIdOrderByPosicaoAsc(simId);
    return linhas.stream()
        .map(j -> new Ranking.Linha(new AlunoId(j.alunoId), j.media, j.posicao))
        .toList();
  }

  /* Opcional: versão que atribui ID do agregado (se desejar trabalhar com RankingId) */
  @Override
  public Ranking salvar(Ranking ranking) {
    Integer simId = ranking.getSimulado().value();

    // upsert do cabeçalho
    var cab = garantirCabecalho(simId);
    cab.congelado = ranking.isCongelado();
    cab = rankingRepo.save(cab);

    // linhas
    linhaRepo.apagarPorSimulado(simId);
    salvarPosicoes(ranking.getSimulado(), ranking.getLinhas());

    // devolve com id preenchido (se o domínio quiser usar)
    ranking.atribuirIdSeAusente(new RankingId(cab.id));
    return ranking;
  }
}
