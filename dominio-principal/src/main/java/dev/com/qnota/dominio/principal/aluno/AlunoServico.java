package dev.com.qnota.dominio.principal.aluno;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.responsavel.Responsavel;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelRepositorio;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.simulado.SimuladoRepositorio;
import dev.com.qnota.dominio.principal.simulado.Simulado;
import dev.com.qnota.dominio.principal.turma.Turma;
import dev.com.qnota.dominio.principal.turma.TurmaId;
import dev.com.qnota.dominio.principal.turma.TurmaRepositorio;

/** Serviço de aplicação — orquestra regras que dependem de outros agregados ou do conjunto. */
public class AlunoServico {

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

    // ---------- CADASTRAR (único — ORM gera o ID) ----------
    public AlunoId cadastrar(String nome,
                             LocalDate nascimento,
                             TurmaId turma,
                             List<ResponsavelId> responsaveis,
                             ResponsavelId principal) {
        validarCadastro(nome, nascimento, turma, responsaveis, principal);
        var aluno = new Aluno(nome, nascimento, true, turma, responsaveis, principal); // sem id
        return repo.salvar(aluno); // repositório/ORM atribui e devolve o AlunoId
    }

    // ---------- TRANSFERIR ----------
    public void transferir(AlunoId id, TurmaId novaTurma) {
        var aluno = repo.porId(id);

        // RN-57.1: não pode se houver simulados finalizados
        if (repo.possuiSimuladoFinalizado(id))
            throw new IllegalStateException("não é permitido alterar a turma do aluno com simulados finalizados");

        // RN-57.2: mesma série/ano letivo
        int anoAtual = turmaRepo.porId(aluno.getTurma()).getAnoLetivo();
        int anoNovo  = turmaRepo.porId(novaTurma).getAnoLetivo();
        if (anoAtual != anoNovo)
            throw new IllegalStateException("a nova turma deve estar no mesmo ano letivo");

        aluno.mudarTurma(novaTurma);
        repo.salvar(aluno);
    }

    // ---------- INATIVAR ----------
    public void inativar(AlunoId id) {
        // RN-67: não pode inativar com notas pendentes em simulados EM_EDICAO
        if (repo.temNotasPendentesEmSimuladosEmEdicao(id))
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

    // ---------- VÍNCULOS COM RESPONSÁVEL ----------
    public void vincularResponsavel(AlunoId id, ResponsavelId resp, boolean principal) {
        // RN-136: impedir vínculo com inadimplente
        var r = responsavelRepo.porId(resp);
        if (r.getStatus() == Responsavel.Status.INADIMPLENTE)
            throw new IllegalStateException("responsável inadimplente não pode ser vinculado até regularização");

        // RN-19: máximo 3 responsáveis por aluno
        var aluno = repo.porId(id);
        if (aluno.getResponsaveis().size() >= 3)
            throw new IllegalStateException("o número máximo de responsáveis por aluno é 3");

        // RN-19: não permitir duplicação
        if (aluno.getResponsaveis().contains(resp))
            throw new IllegalStateException("já existe vínculo entre o responsável e o aluno");

        // RN-58: deve haver exatamente um responsável principal
        if (principal && aluno.getResponsavelPrincipal() != null)
            throw new IllegalStateException("deve haver exatamente um responsável principal");

        aluno.adicionarResponsavel(resp, principal);
        repo.salvar(aluno);
    }

    public void desvincularResponsavel(AlunoId id, ResponsavelId resp) {
        var aluno = repo.porId(id);
        
        // RN-19: aluno deve ter pelo menos um responsável
        if (aluno.getResponsaveis().size() <= 1)
            throw new IllegalStateException("o aluno deve ter pelo menos um responsável");
        
        aluno.removerResponsavel(resp); // garante RN-19/RN-58 (auto-promove outro principal)
        repo.salvar(aluno);
    }

    public void definirPrincipal(AlunoId id, ResponsavelId resp) {
        var aluno = repo.porId(id);
        aluno.definirPrincipal(resp); // RN-58
        repo.salvar(aluno);
    }

    /** Edição “full” do agregado (mantendo invariantes no próprio Aluno). */
    public void editar(Aluno alunoEditado) {
        repo.salvar(alunoEditado);
    }

    // ---------- validações compartilhadas ----------
    private void validarCadastro(String nome, LocalDate nascimento, TurmaId turma, List<ResponsavelId> responsaveis, ResponsavelId principal) {
        Objects.requireNonNull(turma, "'turma' não pode ser nula");

        // RN-03: único por (nome + data) na turma
        if (repo.existeOutroComMesmoNomeENascimentoNaTurma(nome, nascimento, turma))
            throw new IllegalArgumentException("já existe aluno com mesmo nome e data de nascimento na turma");

        // RN-19: aluno deve ter ao menos um responsável
        if (responsaveis == null || responsaveis.isEmpty())
            throw new IllegalArgumentException("Aluno deve ter ao menos um responsável");

        // RN-19: máximo 3 responsáveis por aluno
        if (responsaveis.size() > 3)
            throw new IllegalArgumentException("o número máximo de responsáveis por aluno é 3");

        // Verificar responsáveis nulos
        for (ResponsavelId rid : responsaveis) {
            if (rid == null) throw new IllegalArgumentException("Responsável não pode ser nulo");
        }

        // RN-58: deve haver exatamente um responsável principal
        if (principal == null)
            throw new IllegalArgumentException("é obrigatório definir um responsável principal");

        // RN-19: sem duplicados - verificar ANTES da validação de múltiplos principais
        var set = new LinkedHashSet<>(responsaveis);
        boolean temDuplicados = set.size() != responsaveis.size();
        
        // RN-58: deve haver exatamente um responsável principal (não múltiplos)
        long countPrincipais = responsaveis.stream().filter(r -> r.equals(principal)).count();
        
        // Se há duplicados que resultam em múltiplos principais, mostrar mensagem de múltiplos principais
        if (temDuplicados && countPrincipais > 1) {
            throw new IllegalArgumentException("deve haver exatamente um responsável principal");
        }
        
        // Caso contrário, verificar duplicados primeiro
        if (temDuplicados) {
            throw new IllegalArgumentException("Vínculo de responsável duplicado");
        }
        
        if (countPrincipais != 1) {
            throw new IllegalArgumentException("deve haver exatamente um responsável principal");
        }

        // RN-58: o responsável principal deve estar entre os responsáveis
        if (!set.contains(principal))
            throw new IllegalArgumentException("o responsável principal deve estar entre os responsáveis");

        // RN-136: nenhum responsável inadimplente
        for (ResponsavelId rid : responsaveis) {
            var r = responsavelRepo.porId(rid);
            if (r.getStatus() == Responsavel.Status.INADIMPLENTE)
                throw new IllegalStateException("responsável inadimplente não pode ser vinculado até regularização");
        }
    }
}
