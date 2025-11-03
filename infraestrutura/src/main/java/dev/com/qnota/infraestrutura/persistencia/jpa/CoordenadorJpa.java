package dev.com.qnota.infraestrutura.persistencia.jpa;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.com.qnota.dominio.principal.coordenador.Coordenador;
import dev.com.qnota.dominio.principal.coordenador.CoordenadorId;
import dev.com.qnota.dominio.principal.coordenador.CoordenadorRepositorio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/* =========================
   ENTIDADE JPA (Coordenador)
   ========================= */
@Entity
@Table(name = "coordenadores")
class CoordenadorJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "nome", nullable = false)
    String nome;

    @Column(name = "endereco_eletronico", nullable = false, unique = true)
    String enderecoEletronico;

    @Column(name = "senha_hash", nullable = false)
    String senhaHash;

    @Column(name = "ativo", nullable = false)
    Boolean ativo;

    CoordenadorJpa() {}

    CoordenadorJpa(String nome, String email, String senhaHash, Boolean ativo) {
        this.nome = nome;
        this.enderecoEletronico = email;
        this.senhaHash = senhaHash;
        this.ativo = ativo;
    }
}

interface CoordenadorJpaRepository extends JpaRepository<CoordenadorJpa, Integer> {
    Optional<CoordenadorJpa> findByEnderecoEletronicoIgnoreCase(String email);
    boolean existsByEnderecoEletronicoIgnoreCase(String email);
}

@Repository
class CoordenadorRepositorioImpl implements CoordenadorRepositorio {

    private final CoordenadorJpaRepository jpa;

    @Autowired
    CoordenadorRepositorioImpl(CoordenadorJpaRepository jpa) {
        this.jpa = jpa;
    }

    private CoordenadorJpa toJpa(Coordenador d) {
        var j = new CoordenadorJpa();
        j.id = (d.getId() != null ? d.getId().value() : null);
        j.nome = d.getNome();
        j.enderecoEletronico = d.getEmail();
        j.senhaHash = d.getSenhaHash();
        j.ativo = d.isAtivo();
        return j;
    }

    private Coordenador toDomain(CoordenadorJpa j) {
        var d = new Coordenador(j.nome, j.enderecoEletronico, j.senhaHash, j.ativo != null ? j.ativo : Boolean.TRUE);
        if (j.id != null) d.atribuirIdSeAusente(new CoordenadorId(j.id));
        return d;
    }

    @Override
    @Transactional
    public CoordenadorId salvar(Coordenador c) {
        CoordenadorJpa salvo;
        if (c.getId() == null) {
            salvo = jpa.save(toJpa(c));
            c.atribuirIdSeAusente(new CoordenadorId(salvo.id));
        } else {
            var existente = jpa.findById(c.getId().value())
                               .orElseThrow(() -> new IllegalArgumentException("Coordenador não encontrado: id=" + c.getId().value()));
            existente.nome = c.getNome();
            existente.enderecoEletronico = c.getEmail();
            existente.senhaHash = c.getSenhaHash();
            existente.ativo = c.isAtivo();
            salvo = jpa.save(existente);
        }
        return new CoordenadorId(salvo.id);
    }

    @Override
    @Transactional(readOnly = true)
    public Coordenador porId(CoordenadorId id) {
        return jpa.findById(id.value()).map(this::toDomain)
                  .orElseThrow(() -> new IllegalArgumentException("Coordenador não encontrado: id=" + id.value()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Coordenador> porEmail(String email) {
        return jpa.findByEnderecoEletronicoIgnoreCase(email).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean emailExiste(String email) {
        return jpa.existsByEnderecoEletronicoIgnoreCase(email);
    }

    @Override
    @Transactional
    public void excluir(CoordenadorId id) {
        try {
            jpa.deleteById(id.value());
        } catch (EmptyResultDataAccessException ignore) { }
    }
}
