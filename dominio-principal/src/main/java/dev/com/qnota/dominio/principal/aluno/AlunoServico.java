package dev.com.qnota.dominio.principal.aluno;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import dev.com.qnota.dominio.principal.responsavel.Responsavel;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelRepositorio;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelVinculoService;
import dev.com.qnota.dominio.principal.simulado.SimuladoRepositorio;
import dev.com.qnota.dominio.principal.turma.TurmaId;
import dev.com.qnota.dominio.principal.turma.TurmaRepositorio;

/** Serviço de aplicação — regras e orquestração cross-aggregate. */
public class AlunoServico implements ResponsavelVinculoService {

    private final AlunoRepositorio repo;
    private final ResponsavelRepositorio responsavelRepo;
    private final TurmaRepositorio turmaRepo;
    private final SimuladoRepositorio simuladoRepo;

    public AlunoServico(AlunoRepositorio repo,
                        ResponsavelRepositorio responsavelRepo,
                        TurmaRepositorio turmaRepo,
                        SimuladoRepositorio simuladoRepo) {
        this.repo = repo;
        this.responsavelRepo = responsavelRepo;
        this.turmaRepo = turmaRepo;
        this.simuladoRepo = simuladoRepo;
    }

    // ---------- CADASTRAR ----------
    public AlunoId cadastrar(String nome,
                             LocalDate nascimento,
                             TurmaId turma,
                             List<ResponsavelId> responsaveis,
                             ResponsavelId principal) {

        validarCadastro(nome, nascimento, turma, responsaveis, principal);
        validarCadastroResponsaveis(responsaveis, principal);

        var aluno = new Aluno(nome, nascimento, true, turma, responsaveis, principal);
        return repo.salvar(aluno);
    }

    // ---------- TRANSFERIR ----------
    public void transferir(AlunoId id, TurmaId novaTurma) {
        var aluno = repo.porId(id);

        // RN-57: não transferir se houver simulados finalizados do aluno
        if (simuladoRepo.possuiSimuladoFinalizadoParaAluno(id))
            throw new IllegalStateException("não é permitido alterar a turma do aluno com simulados finalizados");

        // RN-57a: mesmo ano letivo
        int anoAtual = turmaRepo.porId(aluno.getTurma()).getAnoLetivo();
        int anoNovo  = turmaRepo.porId(novaTurma).getAnoLetivo();
        if (anoAtual != anoNovo)
            throw new IllegalStateException("a nova turma deve estar no mesmo ano letivo");

        aluno.mudarTurma(novaTurma);
        repo.salvar(aluno);
    }

    // ---------- INATIVAR ----------
    public void inativar(AlunoId id) {
        // RN-67: não inativar com notas pendentes em simulados EM_EDICAO
        if (simuladoRepo.temNotasPendentesEmSimuladosEmEdicao(id))
            throw new IllegalStateException("existem notas pendentes de lançamento");
        var aluno = repo.porId(id);
        aluno.inativar();
        repo.salvar(aluno);
    }

    // ---------- EXCLUIR ----------
    public void excluir(AlunoId id) {
        // RN-04: não excluir se tiver notas
        if (repo.temNotas(id))
            throw new IllegalStateException("o aluno possui vínculos com simulados/nota");
        repo.remover(id);
    }

    // ---------- VÍNCULOS ----------
    public void vincularResponsavel(AlunoId id, ResponsavelId resp, boolean principal) {
        var r = responsavelRepo.porId(resp);
        if (r.getStatus() == Responsavel.Status.INADIMPLENTE)
            throw new IllegalStateException("responsável inadimplente não pode ser vinculado até regularização");

        var aluno = repo.porId(id);
        validarAdicionarResponsavel(aluno, resp, principal);

        aluno.adicionarResponsavel(resp, principal);
        repo.salvar(aluno);
    }

    public void desvincularResponsavel(AlunoId id, ResponsavelId resp) {
        var aluno = repo.porId(id);
        validarRemoverResponsavel(aluno);
        aluno.removerResponsavel(resp);
        repo.salvar(aluno);
    }

    public void definirPrincipal(AlunoId id, ResponsavelId resp) {
        var aluno = repo.porId(id);
        validarDefinirPrincipal(aluno, resp);
        aluno.definirPrincipal(resp);
        repo.salvar(aluno);
    }

    // ---------- validações cross-aggregate ----------
    private void validarCadastro(String nome, LocalDate nascimento, TurmaId turma,
                                 List<ResponsavelId> responsaveis, ResponsavelId principal) {
        Objects.requireNonNull(turma, "'turma' não pode ser nula");

        if (repo.existeOutroComMesmoNomeENascimentoNaTurma(nome, nascimento, turma))
            throw new IllegalArgumentException("já existe aluno com mesmo nome e data de nascimento na turma");

        if (responsaveis == null || responsaveis.isEmpty())
            throw new IllegalArgumentException("Aluno deve ter ao menos um responsável");

        for (ResponsavelId rid : responsaveis) {
            if (rid == null) throw new IllegalArgumentException("Responsável não pode ser nulo");
            var r = responsavelRepo.porId(rid);
            if (r.getStatus() == Responsavel.Status.INADIMPLENTE)
                throw new IllegalStateException("responsável inadimplente não pode ser vinculado até regularização");
        }

        if (principal == null || !responsaveis.contains(principal))
            throw new IllegalArgumentException("deve haver exatamente um responsável principal na lista");
    }

    // ---------- RN específicas ----------
    private void validarAdicionarResponsavel(Aluno aluno, ResponsavelId responsavelId, boolean principal) {
        if (aluno.getResponsaveis().contains(responsavelId))
            throw new IllegalStateException("já existe vínculo entre o responsável e o aluno");

        if (principal && aluno.getResponsavelPrincipal() != null)
            throw new IllegalStateException("deve haver exatamente um responsável principal");

        // Limite consistente (3)
        if (aluno.getResponsaveis().size() >= 3)
            throw new IllegalStateException("o número máximo de responsáveis por aluno é 3");
    }

    private void validarRemoverResponsavel(Aluno aluno) {
        if (aluno.getResponsaveis().size() <= 1)
            throw new IllegalStateException("o aluno deve ter pelo menos um responsável");
    }

    private void validarDefinirPrincipal(Aluno aluno, ResponsavelId responsavelId) {
        if (!aluno.getResponsaveis().contains(responsavelId))
            throw new IllegalStateException("Vínculo de responsável inexistente");
    }

    private void validarCadastroResponsaveis(List<ResponsavelId> responsaveis, ResponsavelId principal) {
        if (responsaveis == null || responsaveis.isEmpty())
            throw new IllegalArgumentException("Aluno deve ter ao menos um responsável");

        if (responsaveis.size() > 3)
            throw new IllegalArgumentException("o número máximo de responsáveis por aluno é 3");

        if (new LinkedHashSet<>(responsaveis).size() != responsaveis.size())
            throw new IllegalArgumentException("Vínculo de responsável duplicado");

        if (principal == null || !responsaveis.contains(principal))
            throw new IllegalArgumentException("deve haver exatamente um responsável principal na lista");
    }

    // ===== ResponsavelVinculoService =====
    @Override public boolean possuiVinculosAtivos(ResponsavelId responsavelId) {
        return responsavelRepo.estaVinculadoAAlgumAluno(responsavelId);
    }
    @Override public void removerVinculos(ResponsavelId responsavelId) { /* opcional/infra */ }
    @Override public void vincularResponsavel(ResponsavelId rid, AlunoId aid, boolean p) { vincularResponsavel(aid, rid, p); }
    @Override public void desvincularResponsavel(ResponsavelId rid, AlunoId aid) { desvincularResponsavel(aid, rid); }
}
