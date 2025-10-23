package dev.com.qnota.dominio.principal.responsavel;

import java.util.Objects;
import dev.com.qnota.dominio.principal.aluno.AlunoRepositorio;
import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.aluno.AlunoServico;

public class ResponsavelServico {

    private final ResponsavelRepositorio responsavelRepo;
    private final AlunoServico alunoServico;

    public ResponsavelServico(ResponsavelRepositorio responsavelRepo, AlunoServico alunoServico) {
        this.responsavelRepo = Objects.requireNonNull(responsavelRepo);
        this.alunoServico = Objects.requireNonNull(alunoServico);
    }

    /** Cadastro com checagem de unicidade de CPF. ORM atribui o ID e o repositório o retorna. */
    public ResponsavelId cadastrar(String nome, String cpf, String email) {
        if (responsavelRepo.cpfExiste(cpf))
            throw new IllegalArgumentException("já existe responsável com esse CPF");
        var novo = new Responsavel(nome, cpf, email, Responsavel.Status.ATIVO); // sem id
        return responsavelRepo.salvar(novo); // ORM atribui e devolvemos o ResponsavelId
    }

    /** Edita nome/e-mail sem trocar CPF. */
    public void atualizarContato(ResponsavelId id, String novoNome, String novoEmail) {
        var r = responsavelRepo.porId(id);
        r.renomear(novoNome);
        r.alterarEmail(novoEmail);
        responsavelRepo.salvar(r);
    }

    public void marcarInadimplente(ResponsavelId id) {
        var r = responsavelRepo.porId(id);
        r.marcarInadimplente();
        responsavelRepo.salvar(r);
    }

    public void regularizar(ResponsavelId id) {
        var r = responsavelRepo.porId(id);
        r.regularizar();
        responsavelRepo.salvar(r);
    }

    public void inativar(ResponsavelId id) {
        var r = responsavelRepo.porId(id);
        r.inativar();
        responsavelRepo.salvar(r);
    }

    /** Exclusão (RN-21): só sem vínculos ativos. */
    public void excluir(ResponsavelId id) {
        if (responsavelRepo.estaVinculadoAAlgumAluno(id))
            throw new IllegalStateException("o responsável possui vínculos ativos");
        responsavelRepo.excluir(id);
    }

    /** Vincula responsável a um aluno. */
    public void vincularAoAluno(ResponsavelId responsavelId, AlunoId alunoId, boolean principal) {
        alunoServico.vincularResponsavel(alunoId, responsavelId, principal);
    }

    /** Desvincula responsável de um aluno. */
    public void desvincularDoAluno(ResponsavelId responsavelId, AlunoId alunoId) {
        alunoServico.desvincularResponsavel(alunoId, responsavelId);
    }
}
