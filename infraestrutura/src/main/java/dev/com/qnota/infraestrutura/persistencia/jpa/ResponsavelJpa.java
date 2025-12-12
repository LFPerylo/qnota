package dev.com.qnota.infraestrutura.persistencia.jpa;

import static jakarta.persistence.GenerationType.IDENTITY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;       // <— use SEMPRE esta (Spring)
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import dev.com.qnota.aplicacao.principal.responsavel.ResponsavelRepositorioAplicacao;
import dev.com.qnota.aplicacao.principal.responsavel.ResponsavelResumo;
import dev.com.qnota.dominio.principal.responsavel.Responsavel;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelRepositorio;

import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/* =========================
 * ENTIDADE JPA
 * ========================= */
@Entity
@Table(name = "responsaveis")
class ResponsavelJpa {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    Integer id;

    @Column(nullable = false)
    String nome;

    @Column(nullable = false, unique = true)
    String cpf;

    @Column(name = "enderecoeletronico", nullable = false)
    String enderecoEletronico;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    Responsavel.Status status;

    @Override
    public String toString() { return nome; }
}

/* =========================
 * JPA REPOSITORY (Spring)
 * ========================= */
interface ResponsavelJpaRepository extends JpaRepository<ResponsavelJpa, Integer> {

    boolean existsByCpf(String cpf);

    // Checagem de vínculo com alunos (tabela de junção do agregado Aluno)
    @Query(value = """
        SELECT EXISTS(
          SELECT 1
            FROM aluno_responsaveis ar
           WHERE ar.responsavel_id = :rid
        )
        """, nativeQuery = true)
    boolean existeVinculo(@Param("rid") int responsavelId);

    // UPDATE leve para nome/e-mail
    @Modifying
    @Query("""
        UPDATE ResponsavelJpa r
           SET r.nome = :nome,
               r.enderecoEletronico = :email
         WHERE r.id = :id
        """)
    int atualizarContato(@Param("id") int id,
                         @Param("nome") String novoNome,
                         @Param("email") String novoEmail);

    // Query para resumos com contagem de alunos vinculados
    @Query(value = """
        SELECT r.id AS id,
               r.nome AS nome,
               r.enderecoeletronico AS email,
               r.cpf AS cpf,
               COALESCE(COUNT(ar.aluno_id), 0) AS quantidadeAlunos,
               r.status AS status
          FROM responsaveis r
     LEFT JOIN aluno_responsaveis ar ON ar.responsavel_id = r.id
      GROUP BY r.id, r.nome, r.enderecoeletronico, r.cpf, r.status
      ORDER BY r.nome
        """, nativeQuery = true)
    List<ResponsavelResumo> findResponsavelResumoByOrderByNome();
}

/* ==================================
 * IMPLEMENTAÇÃO DO REPOSITÓRIO DO DOMÍNIO (mapeamento manual)
 * ================================== */
@Repository
class ResponsavelRepositorioImpl implements ResponsavelRepositorio, ResponsavelRepositorioAplicacao {

    private final ResponsavelJpaRepository repo;

    @Autowired
    ResponsavelRepositorioImpl(ResponsavelJpaRepository repo) {
        this.repo = repo;
    }

    /* ---------- mapeamento manual ---------- */

    private static Responsavel toDomain(ResponsavelJpa j) {
        var r = new Responsavel(
            j.nome,
            j.cpf,
            j.enderecoEletronico,
            j.status != null ? j.status : Responsavel.Status.ATIVO
        );
        if (j.id != null) {
            r.atribuirIdSeAusente(new ResponsavelId(j.id));
        }
        return r;
    }

    private static void fillJpaFromDomain(Responsavel d, ResponsavelJpa j) {
        j.nome = d.getNome();
        j.cpf  = d.getCpf();                 // imutável por regra, mas espelhamos
        j.enderecoEletronico = d.getEmail();
        j.status = d.getStatus();
    }

    private ResponsavelJpa toJpa(Responsavel d) {
        final ResponsavelJpa j;
        if (d.getId() == null) {
            j = new ResponsavelJpa();          // INSERT
        } else {
            j = repo.findById(d.getId().value())
                    .orElse(new ResponsavelJpa()); // se não achar, trata como novo
            j.id = d.getId().value();
        }
        fillJpaFromDomain(d, j);
        return j;
    }

    /* ---------- contrato do domínio ---------- */

    @Transactional
    @Override
    public ResponsavelId salvar(Responsavel r) {
        var salvo = repo.save(toJpa(r));
        if (r.getId() == null) {
            r.atribuirIdSeAusente(new ResponsavelId(salvo.id));
        }
        return new ResponsavelId(salvo.id);
    }

    @Transactional(readOnly = true)
    @Override
    public Responsavel porId(ResponsavelId id) {
        var j = repo.findById(id.value()).orElseThrow();
        return toDomain(j);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean cpfExiste(String cpf) {
        return repo.existsByCpf(cpf);
    }

    @Transactional
    @Override
    public void atualizarContato(ResponsavelId id, String novoNome, String novoEmail) {
        // opção 1 (leve): UPDATE direto
        repo.atualizarContato(id.value(), novoNome, novoEmail);

        // Se preferir manter o estado sincronizado a partir do domínio, use:
        // var r = porId(id); r.renomear(novoNome); r.alterarEmail(novoEmail); salvar(r);
    }

    @Transactional
    @Override
    public void excluir(ResponsavelId id) {
        repo.deleteById(id.value());
    }

    @Transactional(readOnly = true)
    @Override
    public boolean estaVinculadoAAlgumAluno(ResponsavelId id) {
        return repo.existeVinculo(id.value());
    }

    /* ---------- contrato da aplicação ---------- */

    @Transactional(readOnly = true)
    @Override
    public List<ResponsavelResumo> pesquisarResumos() {
        return repo.findResponsavelResumoByOrderByNome();
    }
}
