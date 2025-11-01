package dev.com.qnota.infraestrutura.persistencia.jpa;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
// IMPORTS CORRETOS PARA ANOTAÇÕES DO SPRING DATA:
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.com.qnota.dominio.principal.professor.Professor;
import dev.com.qnota.dominio.principal.professor.ProfessorId;
import dev.com.qnota.dominio.principal.professor.ProfessorRepositorio;
import jakarta.persistence.*;

@Entity
@Table(name = "PROFESSORES") // use o mesmo nome do seu script (professores)
class ProfessorJpa {
  @Id @GeneratedValue(strategy = IDENTITY)
  @Column(name="ID")
  int id;

  @Column(name="NOME", nullable=false, length=120)
  String nome;

  @Column(name="CPF", nullable=false, unique=true, length=14)
  String cpf;

  @Column(name="ENDERECOELETRONICO", nullable=false, length=160)
  String email;

  @ElementCollection
  @CollectionTable(name="PROFESSOR_ESPECIALIDADE", joinColumns=@JoinColumn(name="PROFESSOR_ID"))
  @OrderColumn(name="ORDEM")
  @Column(name="NOME", nullable=false, length=80)
  List<String> especialidades = new ArrayList<>();
}

interface ProfessorJpaRepository extends JpaRepository<ProfessorJpa, Integer> {

  // Conte as turmas ATIVAS do professor (tabela 'turmas' do seu script)
  @Query(
      value = """
              SELECT COUNT(*)
                FROM turmas
               WHERE professor_id = :profId
                 AND ativo = 1
              """,
      nativeQuery = true)
  long countTurmasAtivas(@Param("profId") int profId);

  // Substitui o professor nas turmas
  @Modifying
  @Transactional
  @Query(
      value = """
              UPDATE turmas
                 SET professor_id = :substituto
               WHERE professor_id = :antigo
              """,
      nativeQuery = true)
  int substituirProfessorNasTurmas(@Param("antigo") int antigo,
                                   @Param("substituto") int substituto);
}

@Repository
class ProfessorRepositorioImpl implements ProfessorRepositorio {

  @Autowired private ProfessorJpaRepository repo;
  @Autowired private JpaMapeador mapper;

  @Transactional
  @Override
  public ProfessorId salvar(Professor p) {
    var j = mapper.map(p, ProfessorJpa.class);
    j = repo.save(j);
    var idGerado = new ProfessorId(j.id);
    if (p.getId() == null) {
      p.atribuirIdSeAusente(idGerado);
    }
    return idGerado;
  }

  @Transactional(readOnly = true)
  @Override
  public Professor porId(ProfessorId id) {
    var j = repo.findById(id.value()).orElseThrow();
    return mapper.map(j, Professor.class);
  }

  @Transactional(readOnly = true)
  @Override
  public int contarTurmasAtivas(ProfessorId id) {
    return (int) repo.countTurmasAtivas(id.value());
  }

  @Transactional(readOnly = true)
  @Override
  public List<String> nomesDeAreasDoProfessor(ProfessorId id) {
    var j = repo.findById(id.value()).orElseThrow();
    return List.copyOf(j.especialidades);
  }

  @Transactional
  @Override
  public void substituirProfessor(ProfessorId antigo, ProfessorId substituto) {
    repo.substituirProfessorNasTurmas(antigo.value(), substituto.value());
  }
}
