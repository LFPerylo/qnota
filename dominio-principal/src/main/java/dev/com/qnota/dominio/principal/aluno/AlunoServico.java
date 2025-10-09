package dev.com.qnota.dominio.principal.aluno;

import java.time.LocalDate;
import java.util.List;

import dev.com.qnota.dominio.principal.aluno.Aluno.AlunoResponsavel;
import dev.com.qnota.dominio.principal.responsavel.Responsavel;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelRepositorio;
import dev.com.qnota.dominio.principal.turma.Turma;
import dev.com.qnota.dominio.principal.turma.TurmaId;
import dev.com.qnota.dominio.principal.turma.TurmaRepositorio;

/**
 * Serviço de aplicação — orquestra regras que dependem de outros agregados ou do conjunto.
 */
public class AlunoServico {

    private final AlunoRepositorio repo;
    private final ResponsavelRepositorio responsavelRepo;
    private final TurmaRepositorio turmaRepo;

    public AlunoServico(AlunoRepositorio repo,
                        ResponsavelRepositorio responsavelRepo,
                        TurmaRepositorio turmaRepo) {
        this.repo = repo;
        this.responsavelRepo = responsavelRepo;
        this.turmaRepo = turmaRepo;
    }

    // ---------- CADASTRAR ----------
    public void cadastrar(AlunoId id, String nome, LocalDate nascimento, TurmaId turma, List<AlunoResponsavel> responsaveis) {
        // RN-03: único por (nome + data) na turma
        if (repo.existeOutroComMesmoNomeENascimentoNaTurma(nome, nascimento, turma))
            throw new IllegalArgumentException("RN-03: Já existe aluno com mesmo nome e nascimento na turma.");

        // RN-136: nenhum responsável inadimplente
        for (AlunoResponsavel ar : responsaveis) {
            var r = responsavelRepo.porId(ar.responsavel()).orElseThrow();
            if (r.getStatus() == Responsavel.Status.INADIMPLENTE)
                throw new IllegalStateException("RN-136: Responsável inadimplente não pode ser vinculado.");
        }

        // Cria o agregado — ele valida RN-02, RN-19, RN-20, RN-58
        var aluno = new Aluno(id, nome, nascimento, true, turma, responsaveis);
        repo.salvar(aluno);
    }

    // ---------- TRANSFERIR ----------
    public void transferir(AlunoId id, TurmaId novaTurma) {
        var aluno = repo.porId(id).orElseThrow();

        // RN-57.1: não pode se houver simulados finalizados
        if (repo.possuiSimuladoFinalizado(id))
            throw new IllegalStateException("RN-57.1: Não pode transferir com simulados finalizados.");

        // RN-57.2: mesma série/ano letivo
        int anoAtual = turmaRepo.porId(aluno.getTurma()).map(Turma::getAnoLetivo).orElseThrow();
        int anoNovo  = turmaRepo.porId(novaTurma).map(Turma::getAnoLetivo).orElseThrow();
        if (anoAtual != anoNovo)
            throw new IllegalStateException("RN-57.2: Nova turma deve ser do mesmo ano letivo.");

        aluno.mudarTurma(novaTurma);
        repo.salvar(aluno);
    }

    // ---------- INATIVAR ----------
    public void inativar(AlunoId id) {
        // RN-67: não pode inativar com notas pendentes em simulados EM_EDICAO
        if (repo.temNotasPendentesEmSimuladosEmEdicao(id))
            throw new IllegalStateException("RN-67: Não é possível inativar com notas pendentes em simulados em edição.");

        var aluno = repo.porId(id).orElseThrow();
        aluno.inativar();
        repo.salvar(aluno);
    }

    // ---------- EXCLUIR ----------
    public void excluir(AlunoId id) {
        // RN-04: não excluir se tiver notas
        if (repo.temNotas(id))
            throw new IllegalStateException("RN-04: Aluno não pode ser excluído pois possui notas registradas.");
        repo.remover(id);
    }

    // ---------- VÍNCULOS COM RESPONSÁVEL ----------
    public void vincularResponsavel(AlunoId id, ResponsavelId resp, String grauParentesco, boolean principal) {
        // RN-136: impedir vínculo com inadimplente
        var r = responsavelRepo.porId(resp).orElseThrow();
        if (r.getStatus() == Responsavel.Status.INADIMPLENTE)
            throw new IllegalStateException("RN-136: Responsável inadimplente não pode ser vinculado.");

        var aluno = repo.porId(id).orElseThrow();
        aluno.adicionarResponsavel(resp, grauParentesco, principal);
        repo.salvar(aluno);
    }

    public void desvincularResponsavel(AlunoId id, ResponsavelId resp) {
        var aluno = repo.porId(id).orElseThrow();
        aluno.removerResponsavel(resp); // garante RN-19 e RN-58 (auto-promove outro principal)
        repo.salvar(aluno);
    }

    /** Edição “full” do agregado (mantendo invariantes no próprio Aluno). */
    public void editar(Aluno alunoEditado) {
        // regras cruzadas — se necessário, aplique aqui (ex.: rever RN-136 numa troca de responsáveis)
        // Invariantes internas são checadas pelo próprio Aluno no construtor/substituição.
        repo.salvar(alunoEditado);
    }
}
