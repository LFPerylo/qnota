package dev.com.qnota.infraestrutura.persistencia.jpa;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.com.qnota.dominio.principal.coordenador.Coordenador;
import dev.com.qnota.dominio.principal.coordenador.CoordenadorId;
import dev.com.qnota.dominio.principal.coordenador.CoordenadorRepositorio;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "coordenadores")
class CoordenadorJpa {
  @Id
  Integer id;

  String nome;
  String enderecoEletronico; // mapeia domain.getEmail()
  String senhaHash;
  Boolean ativo;
}

interface CoordenadorJpaRepository extends JpaRepository<CoordenadorJpa, Integer> {
  Optional<CoordenadorJpa> findByEnderecoEletronicoIgnoreCase(String enderecoEletronico);
  boolean existsByEnderecoEletronicoIgnoreCase(String enderecoEletronico);
}

@Repository
class CoordenadorRepositorioImpl implements CoordenadorRepositorio {

  @Autowired CoordenadorJpaRepository repositorio;
  @Autowired JpaMapeador mapeador;

  @Override
  public CoordenadorId salvar(Coordenador c) {
    var jpa = mapeador.map(c, CoordenadorJpa.class);
    var salvo = repositorio.save(jpa);
    var id = new CoordenadorId(salvo.id);
    c.atribuirIdSeAusente(id);
    return id;
  }

  @Override
  public Coordenador porId(CoordenadorId id) {
    var jpa = repositorio.findById(id.value()).orElseThrow(); // <-- value()
    return mapeador.map(jpa, Coordenador.class);
  }

  @Override
  public Optional<Coordenador> porEmail(String email) {
    return repositorio.findByEnderecoEletronicoIgnoreCase(email)
                      .map(j -> mapeador.map(j, Coordenador.class));
  }

  @Override
  public boolean emailExiste(String email) {
    return repositorio.existsByEnderecoEletronicoIgnoreCase(email);
  }

  @Override
  public void excluir(CoordenadorId id) {
    repositorio.deleteById(id.value());
  }
}
