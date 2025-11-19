package dev.com.qnota.dominio.principal.responsavel;

import java.util.Objects;
import dev.com.qnota.dominio.principal.aluno.AlunoId;

public class ResponsavelServico {

    private final ResponsavelRepositorio responsavelRepo;
    private final ResponsavelVinculoService vinculoService;

    public ResponsavelServico(ResponsavelRepositorio responsavelRepo, ResponsavelVinculoService vinculoService) {
        this.responsavelRepo = Objects.requireNonNull(responsavelRepo);
        this.vinculoService = Objects.requireNonNull(vinculoService);
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
        if (vinculoService.possuiVinculosAtivos(id))
            throw new IllegalStateException("o responsável possui vínculos ativos");
        vinculoService.removerVinculos(id);
        responsavelRepo.excluir(id);
    }

    /** Vincula responsável a um aluno. */
    public void vincularAoAluno(ResponsavelId responsavelId, AlunoId alunoId, boolean principal) {
        vinculoService.vincularResponsavel(responsavelId, alunoId, principal);
    }

    /** Desvincula responsável de um aluno. */
    public void desvincularDoAluno(ResponsavelId responsavelId, AlunoId alunoId) {
        vinculoService.desvincularResponsavel(responsavelId, alunoId);
    }
}
