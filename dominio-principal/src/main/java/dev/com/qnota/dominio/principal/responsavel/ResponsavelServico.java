package dev.com.qnota.dominio.principal.responsavel;

import dev.com.qnota.dominio.principal.aluno.AlunoRepositorio;
import dev.com.qnota.dominio.principal.aluno.AlunoId;

public class ResponsavelServico {

    private final ResponsavelRepositorio responsavelRepo;
    private final AlunoRepositorio alunoRepo;

    public ResponsavelServico(ResponsavelRepositorio responsavelRepo, AlunoRepositorio alunoRepo) {
        this.responsavelRepo = responsavelRepo;
        this.alunoRepo = alunoRepo;
    }

    /** Cadastro com checagem de unicidade de CPF. ID é gerado pela infraestrutura. */
    public void cadastrar(String nome, String cpf, String email) {
        if (responsavelRepo.cpfExiste(cpf))
            throw new IllegalArgumentException("já existe responsável com esse CPF");
        var novo = new Responsavel(nome, cpf, email, Responsavel.Status.ATIVO);
        responsavelRepo.salvar(novo); // repo atribui o ID
    }

    /** Edita nome/e-mail sem trocar CPF. */
    public void atualizarContato(ResponsavelId id, String novoNome, String novoEmail) {
        var r = responsavelRepo.porId(id).orElseThrow(() -> new IllegalStateException("responsável não encontrado"));
        r.renomear(novoNome);
        r.alterarEmail(novoEmail);
        responsavelRepo.salvar(r);
        // alternativamente: responsavelRepo.atualizarContato(id, novoNome, novoEmail);
    }

    public void marcarInadimplente(ResponsavelId id) {
        var r = responsavelRepo.porId(id).orElseThrow();
        r.marcarInadimplente();
        responsavelRepo.salvar(r);
    }

    public void regularizar(ResponsavelId id) {
        var r = responsavelRepo.porId(id).orElseThrow();
        r.regularizar();
        responsavelRepo.salvar(r);
    }

    public void inativar(ResponsavelId id) {
        var r = responsavelRepo.porId(id).orElseThrow();
        r.inativar();
        responsavelRepo.salvar(r);
    }

    /** Exclusão (RN-21): só sem vínculos ativos. */
    public void excluir(ResponsavelId id) {
        if (responsavelRepo.estaVinculadoAAlgumAluno(id))
            throw new IllegalStateException("o responsável possui vínculos ativos");
        responsavelRepo.excluir(id);
    }

    // ============ VÍNCULO COM ALUNO ============

    /**
     * Vincular responsável ao aluno.
     * Regras:
     *  - RN-136: inadimplente não pode vincular
     *  - RN-20: não pode duplicar vínculo
     *  - RN-02/RN-58: invariantes delegadas ao agregado Aluno
     */
    public void vincularAoAluno(ResponsavelId respId, AlunoId alunoId, String grauParentesco, boolean principal) {
        var r = responsavelRepo.porId(respId).orElseThrow(() -> new IllegalStateException("responsável não encontrado"));
        if (r.getStatus() == Responsavel.Status.INADIMPLENTE)
            throw new IllegalStateException("responsável inadimplente não pode ser vinculado até regularização");

        var aluno = alunoRepo.porId(alunoId).orElseThrow(() -> new IllegalStateException("aluno não encontrado"));

        // RN-20 (mensagem alinhada ao .feature)
        boolean jaVinculado = aluno.getResponsaveis().stream().anyMatch(ar -> ar.responsavel().equals(respId));
        if (jaVinculado) throw new IllegalStateException("já existe vínculo entre o responsável e o aluno");

        // RN-02 (mensagem alinhada ao .feature)
        if (aluno.getResponsaveis().size() >= 3)
            throw new IllegalStateException("o número máximo de responsáveis por aluno é 3");

        aluno.adicionarResponsavel(respId, grauParentesco, principal);
        alunoRepo.salvar(aluno);
    }

    /** Desvincular mantendo ao menos um responsável (RN-19). */
    public void desvincularDoAluno(ResponsavelId respId, AlunoId alunoId) {
        var aluno = alunoRepo.porId(alunoId).orElseThrow(() -> new IllegalStateException("aluno não encontrado"));

        if (aluno.getResponsaveis().size() == 1
                && aluno.getResponsaveis().get(0).responsavel().equals(respId)) {
            throw new IllegalStateException("o aluno deve ter pelo menos um responsável");
        }

        aluno.removerResponsavel(respId); // promove principal se necessário
        alunoRepo.salvar(aluno);
    }

    /** Define explicitamente um responsável como principal (RN-58). */
    public void definirComoPrincipal(ResponsavelId respId, AlunoId alunoId) {
        var aluno = alunoRepo.porId(alunoId).orElseThrow(() -> new IllegalStateException("aluno não encontrado"));
        aluno.definirPrincipal(respId);
        alunoRepo.salvar(aluno);
    }
}
