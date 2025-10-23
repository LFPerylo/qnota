package dev.com.qnota.dominio.principal.aluno;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import dev.com.qnota.dominio.principal.responsavel.Responsavel;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelRepositorio;
import dev.com.qnota.dominio.principal.turma.Turma;
import dev.com.qnota.dominio.principal.turma.TurmaId;
import dev.com.qnota.dominio.principal.turma.TurmaRepositorio;

/** Serviço de aplicação — regras entre agregados. */
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
    public AlunoId cadastrar(String nome,
                             LocalDate nascimento,
                             TurmaId turma,
                             List<ResponsavelId> responsaveis,
                             ResponsavelId principal) {
        validarCadastro(nome, nascimento, turma, responsaveis, principal);
        var aluno = new Aluno(nome, nascimento, true, turma, responsaveis, principal); // invariantes no agregado
        return repo.salvar(aluno);
    }

    // ---------- TRANSFERIR ----------
    public void transferir(AlunoId id, TurmaId novaTurma) {
        var aluno = repo.porId(id);

        if (repo.possuiSimuladoFinalizado(id))
            throw new IllegalStateException("não é permitido alterar a turma do aluno com simulados finalizados");

        int anoAtual = turmaRepo.porId(aluno.getTurma()).getAnoLetivo();
        int anoNovo  = turmaRepo.porId(novaTurma).getAnoLetivo();
        if (anoAtual != anoNovo)
            throw new IllegalStateException("a nova turma deve estar no mesmo ano letivo");

        aluno.mudarTurma(novaTurma);
        repo.salvar(aluno);
    }

    // ---------- INATIVAR ----------
    public void inativar(AlunoId id) {
        if (repo.temNotasPendentesEmSimuladosEmEdicao(id))
            throw new IllegalStateException("existem notas pendentes de lançamento");
        var aluno = repo.porId(id);
        aluno.inativar();
        repo.salvar(aluno);
    }

    // ---------- EXCLUIR ----------
    public void excluir(AlunoId id) {
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
        aluno.adicionarResponsavel(resp, principal); // regras de cardinalidade no agregado
        repo.salvar(aluno);
    }

    public void desvincularResponsavel(AlunoId id, ResponsavelId resp) {
        var aluno = repo.porId(id);
        aluno.removerResponsavel(resp);
        repo.salvar(aluno);
    }

    public void definirPrincipal(AlunoId id, ResponsavelId resp) {
        var aluno = repo.porId(id);
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
        
        // Verificar responsáveis nulos primeiro
        for (ResponsavelId rid : responsaveis) {
            if (rid == null) 
                throw new IllegalArgumentException("Responsável não pode ser nulo");
        }
        
        if (new LinkedHashSet<>(responsaveis).size() != responsaveis.size())
            throw new IllegalArgumentException("Vínculo de responsável duplicado"); // sanity check leve

        if (principal == null)
            throw new IllegalArgumentException("é obrigatório definir um responsável principal");

        for (ResponsavelId rid : responsaveis) {
            var r = responsavelRepo.porId(rid);
            if (r.getStatus() == Responsavel.Status.INADIMPLENTE)
                throw new IllegalStateException("responsável inadimplente não pode ser vinculado até regularização");
        }
        // Todas as demais invariantes ficam no próprio Aluno
    }
}