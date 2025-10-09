package dev.com.qnota.dominio.principal.responsavel;

import dev.com.qnota.dominio.principal.aluno.AlunoRepositorio;
import dev.com.qnota.dominio.principal.aluno.AlunoId; // mantenha só se usar em algum método


public class ResponsavelServico {

    private final ResponsavelRepositorio responsavelRepo;
    private final AlunoRepositorio alunoRepo;

    public ResponsavelServico(ResponsavelRepositorio responsavelRepo, AlunoRepositorio alunoRepo) {
        this.responsavelRepo = responsavelRepo;
        this.alunoRepo = alunoRepo;
    }

    /** Cadastro com checagem de unicidade de CPF. Entidade garante NOT NULL/CPF válido. */
    public void cadastrar(ResponsavelId id, String nome, String cpf, String email) {
        if (responsavelRepo.cpfExiste(cpf))
            throw new IllegalArgumentException("já existe responsável com esse CPF");
        var novo = new Responsavel(id, nome, cpf, email, Responsavel.Status.ATIVO);
        responsavelRepo.salvar(novo);
    }

    /** Edita nome/e-mail sem trocar CPF. */
    public void atualizarContato(ResponsavelId id, String novoNome, String novoEmail) {
        var r = responsavelRepo.porId(id).orElseThrow(() -> new IllegalStateException("responsável não encontrado"));
        r.renomear(novoNome);
        r.alterarEmail(novoEmail);
        responsavelRepo.salvar(r);
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

    // ============ VÍNCULO COM ALUNO (cobre cenários do .feature) ============

    /**
     * Vincular responsável ao aluno.
     * Regras cobertas:
     *  - RN-136: inadimplente não pode vincular (mensagem do .feature)
     *  - RN-20: não pode duplicar vínculo (mensagem do .feature)
     *  - RN-02: no máximo 3 responsáveis (mensagem do .feature)
     *  - RN-58: exatamente um principal (validado pelo agregado Aluno)
     */
    public void vincularAoAluno(ResponsavelId respId, AlunoId alunoId, String grauParentesco, boolean principal) {
        var r = responsavelRepo.porId(respId).orElseThrow(() -> new IllegalStateException("responsável não encontrado"));
        if (r.getStatus() == Responsavel.Status.INADIMPLENTE)
            throw new IllegalStateException("responsável inadimplente não pode ser vinculado até regularização"); // RN-136

        var aluno = alunoRepo.porId(alunoId).orElseThrow(() -> new IllegalStateException("aluno não encontrado"));

        // RN-20 (mensagem alinhada ao .feature)
        boolean jaVinculado = aluno.getResponsaveis().stream().anyMatch(ar -> ar.responsavel().equals(respId));
        if (jaVinculado) throw new IllegalStateException("já existe vínculo entre o responsável e o aluno");

        // RN-02 (mensagem alinhada ao .feature)
        if (aluno.getResponsaveis().size() >= 3)
            throw new IllegalStateException("o número máximo de responsáveis por aluno é 3");

        // Delega invariantes ao agregado Aluno (RN-02, RN-20, RN-58)
        aluno.adicionarResponsavel(respId, grauParentesco, principal);

        alunoRepo.salvar(aluno);
    }

    /**
     * Desvincular responsável do aluno.
     * Regras cobertas:
     *  - RN-19: aluno deve permanecer com pelo menos um responsável (mensagem do .feature)
     *  - RN-58: se remover o principal e sobrar >0, agregado promove alguém (já coberto em Aluno.removerResponsavel)
     */
    public void desvincularDoAluno(ResponsavelId respId, AlunoId alunoId) {
        var aluno = alunoRepo.porId(alunoId).orElseThrow(() -> new IllegalStateException("aluno não encontrado"));

        // RN-19 – mensagem alinhada ao .feature
        if (aluno.getResponsaveis().size() == 1
                && aluno.getResponsaveis().get(0).responsavel().equals(respId)) {
            throw new IllegalStateException("o aluno deve ter pelo menos um responsável");
        }

        aluno.removerResponsavel(respId); // garante invariantes (promove principal se necessário)
        alunoRepo.salvar(aluno);
    }

    /**
     * Define explicitamente um responsável como principal (útil p/ telas).
     * Agregado garante que ficará exatamente um principal (RN-58).
     */
    public void definirComoPrincipal(ResponsavelId respId, AlunoId alunoId) {
        var aluno = alunoRepo.porId(alunoId).orElseThrow(() -> new IllegalStateException("aluno não encontrado"));
        aluno.definirPrincipal(respId);
        alunoRepo.salvar(aluno);
    }
}
