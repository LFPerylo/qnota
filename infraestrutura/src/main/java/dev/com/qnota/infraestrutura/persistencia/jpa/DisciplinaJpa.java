package dev.com.qnota.infraestrutura.persistencia.jpa;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.com.qnota.dominio.principal.disciplina.Disciplina;
import dev.com.qnota.dominio.principal.disciplina.Disciplina.AreaConhecimento;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaRepositorio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/* =========================================================
 * Entidades JPA
 * ========================================================= */

@Entity
@Table(name = "areas_conhecimento")
class AreaConhecimentoJpa {
  @Id
  Integer id;

  @Column(nullable = false)
  String nome;
}

@Entity
@Table(name = "disciplinas")
class DisciplinaJpa {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Integer id;

  @Column(nullable = false)
  String nome;

  @Column(nullable = false)
  Integer versao;

  @Column(name = "idVersaoOrigem")
  Integer idVersaoOrigem;

  @Column(nullable = false)
  Boolean ativo;

  @ManyToOne
  @JoinColumn(name = "area_id", nullable = false)
  AreaConhecimentoJpa area;
}

/* =========================================================
 * Repositórios Spring Data
 * ========================================================= */

interface AreaConhecimentoJpaRepository extends JpaRepository<AreaConhecimentoJpa, Integer> {
  Optional<AreaConhecimentoJpa> findByNomeIgnoreCase(String nome);
}

interface DisciplinaJpaRepository extends JpaRepository<DisciplinaJpa, Integer> {

  // RN-121: nome único por área (case-insensitive)
  boolean existsByNomeIgnoreCaseAndArea_NomeIgnoreCase(String nome, String areaNome);

  // RN-44: foi usada em algum simulado?
  @Query(value = """
      select exists (
        select 1
          from simulado_disciplinas sd
         where sd.disciplina_id = :discId
      )
      """, nativeQuery = true)
  boolean usadaEmAlgumSimulado(Integer discId);

  // RN-62: foi usada em simulado FINALIZADO?
  @Query(value = """
      select exists (
        select 1
          from simulado_disciplinas sd
          join simulados s on s.id = sd.simulado_id
         where sd.disciplina_id = :discId
           and s.status = 'FINALIZADO'
      )
      """, nativeQuery = true)
  boolean usadaEmSimuladoFinalizado(Integer discId);
}

/* =========================================================
 * Implementação do DisciplinaRepositorio
 * ========================================================= */

@Repository
@Transactional
class DisciplinaRepositorioImpl implements DisciplinaRepositorio {

  @Autowired DisciplinaJpaRepository repo;
  @Autowired AreaConhecimentoJpaRepository areaRepo;
  @Autowired JpaMapeador mapeador;

  @Override
  public DisciplinaId salvar(Disciplina d) {
    DisciplinaJpa jpa;

    if (d.getId() == null) {
      // INSERT
      jpa = new DisciplinaJpa();
      aplicar(d, jpa);
      jpa = repo.save(jpa);
      d.atribuirIdSeAusente(new DisciplinaId(jpa.id));
      return d.getId();
    } else {
      // UPDATE
      jpa = repo.findById(d.getId().value()).orElseThrow();
      aplicar(d, jpa);
      repo.save(jpa);
      return d.getId();
    }
  }

  @Override
  @Transactional(readOnly = true)
  public Disciplina porId(DisciplinaId id) {
    var jpa = repo.findById(id.value()).orElseThrow();
    return toDomain(jpa);
  }

  @Override
  public void remover(DisciplinaId id) {
    repo.deleteById(id.value());
  }

  // ===== Regras/consultas usadas pelo serviço =====

  @Override
  @Transactional(readOnly = true)
  public boolean existeNomeNaArea(String nome, String areaNome) {
    return repo.existsByNomeIgnoreCaseAndArea_NomeIgnoreCase(nome, areaNome);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean foiUsadaEmAlgumSimulado(DisciplinaId id) {
    return repo.usadaEmAlgumSimulado(id.value());
  }

  @Override
  @Transactional(readOnly = true)
  public boolean foiUsadaEmSimuladoFinalizado(DisciplinaId id) {
    return repo.usadaEmSimuladoFinalizado(id.value());
  }

  /* =====================================================
   * mapeamentos simples (sem depender de ModelMapper)
   * ===================================================== */

  private Disciplina toDomain(DisciplinaJpa j) {
    var area = new AreaConhecimento(j.area.id, j.area.nome);
    var d = new Disciplina(j.nome, area);
    // “reconstituir” o estado correto:
    d.atribuirIdSeAusente(new DisciplinaId(j.id));
    if (!j.ativo) d.inativar();
    if (j.versao != 1 || j.idVersaoOrigem != null) {
      // forçamos os campos ‘versao’ e ‘idVersaoOrigem’ refletindo a linha
      // (sem expor setters públicos no domínio)
      // Para manter o domínio limpo, reconstituímos por reflexo leve:
      try {
        var fVersao = Disciplina.class.getDeclaredField("versao");
        fVersao.setAccessible(true);
        fVersao.setInt(d, j.versao);

        var fOrigem = Disciplina.class.getDeclaredField("idVersaoOrigem");
        fOrigem.setAccessible(true);
        fOrigem.set(d, j.idVersaoOrigem);
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Falha ao reconstituir Disciplina", e);
      }
    }
    return d;
  }

  private void aplicar(Disciplina d, DisciplinaJpa j) {
    j.nome  = d.getNome();
    j.versao = d.getVersao();
    j.idVersaoOrigem = d.getIdVersaoOrigem();
    j.ativo = d.isAtivo();

    // área: preferimos buscar por ID; se não houver, tentamos por nome
    var areaId = d.getArea().id();
    AreaConhecimentoJpa area = null;
    if (areaId != 0) {
      area = areaRepo.findById(areaId).orElse(null);
    }
    if (area == null) {
      area = areaRepo.findByNomeIgnoreCase(d.getArea().nome())
              .orElseThrow(() -> new IllegalStateException("Área de conhecimento inexistente: " + d.getArea().nome()));
    }
    j.area = area;
  }
}
