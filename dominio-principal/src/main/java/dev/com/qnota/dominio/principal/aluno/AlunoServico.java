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
        
        // Validações de negócio para cadastro
        validarCadastroResponsaveis(responsaveis, principal);
        
        var aluno = new Aluno(nome, nascimento, true, turma, responsaveis, principal);
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
        
        // Validações de negócio (RN)
        validarAdicionarResponsavel(aluno, resp, principal);
        
        aluno.adicionarResponsavel(resp, principal);
        repo.salvar(aluno);
    }

    public void desvincularResponsavel(AlunoId id, ResponsavelId resp) {
        var aluno = repo.porId(id);
        
        // Validações de negócio (RN)
        validarRemoverResponsavel(aluno, resp);
        
        aluno.removerResponsavel(resp);
        repo.salvar(aluno);
    }

    public void definirPrincipal(AlunoId id, ResponsavelId resp) {
        var aluno = repo.porId(id);
        
        // Validações de negócio (RN)
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
        
        // Verificar responsáveis nulos primeiro
        for (ResponsavelId rid : responsaveis) {
            if (rid == null) 
                throw new IllegalArgumentException("Responsável não pode ser nulo");
        }
        
        if (principal == null)
            throw new IllegalArgumentException("é obrigatório definir um responsável principal");
            
        // Verificar se o principal está na lista de responsáveis
        if (!responsaveis.contains(principal))
            throw new IllegalArgumentException("o responsável principal deve estar na lista de responsáveis");
            
        // RN-58: Deve haver exatamente um responsável principal
        long countPrincipais = responsaveis.stream().filter(r -> r.equals(principal)).count();
        if (countPrincipais != 1)
            throw new IllegalArgumentException("deve haver exatamente um responsável principal");
        
        if (new LinkedHashSet<>(responsaveis).size() != responsaveis.size())
            throw new IllegalArgumentException("Vínculo de responsável duplicado"); // sanity check leve

        for (ResponsavelId rid : responsaveis) {
            var r = responsavelRepo.porId(rid);
            if (r.getStatus() == Responsavel.Status.INADIMPLENTE)
                throw new IllegalStateException("responsável inadimplente não pode ser vinculado até regularização");
        }
        // Todas as demais invariantes ficam no próprio Aluno
    }
    
    // ---------- VALIDAÇÕES DE NEGÓCIO (RN) ----------
    
    /** RN-19/20/58: Validações para adicionar responsável */
    private void validarAdicionarResponsavel(Aluno aluno, ResponsavelId responsavelId, boolean principal) {
        // RN-20: Verificar duplicação
        if (aluno.getResponsaveis().contains(responsavelId)) {
            throw new IllegalStateException("já existe vínculo entre o responsável e o aluno");
        }
        
        // RN-58: Verificar principal único
        if (principal && aluno.getResponsavelPrincipal() != null) {
            throw new IllegalStateException("deve haver exatamente um responsável principal");
        }
        
        // RN-XX: Máximo 3 responsáveis
        if (aluno.getResponsaveis().size() >= 3) {
            throw new IllegalStateException("o número máximo de responsáveis por aluno é 3");
        }
    }
    
    /** RN-19: Validações para remover responsável */
    private void validarRemoverResponsavel(Aluno aluno, ResponsavelId responsavelId) {
        // RN-19: Deve ter pelo menos um responsável
        if (aluno.getResponsaveis().size() <= 1) {
            throw new IllegalStateException("o aluno deve ter pelo menos um responsável");
        }
    }
    
    /** RN-XX: Validações para definir principal */
    private void validarDefinirPrincipal(Aluno aluno, ResponsavelId responsavelId) {
        // Verificar se o responsável está vinculado
        if (!aluno.getResponsaveis().contains(responsavelId)) {
            throw new IllegalStateException("Vínculo de responsável inexistente");
        }
    }
    
    /** RN-19/20/58: Validações para cadastro de responsáveis */
    private void validarCadastroResponsaveis(List<ResponsavelId> responsaveis, ResponsavelId principal) {
        // RN-19: Deve ter pelo menos um responsável
        if (responsaveis == null || responsaveis.isEmpty()) {
            throw new IllegalArgumentException("Aluno deve ter ao menos um responsável");
        }
        
        // RN-XX: Máximo 3 responsáveis
        if (responsaveis.size() > 3) {
            throw new IllegalArgumentException("o número máximo de responsáveis por aluno é 3");
        }
        
        // RN-20: Verificar duplicação
        if (new LinkedHashSet<>(responsaveis).size() != responsaveis.size()) {
            throw new IllegalArgumentException("Vínculo de responsável duplicado");
        }
        
        // RN-58: Deve haver exatamente um responsável principal
        if (principal == null) {
            throw new IllegalArgumentException("é obrigatório definir um responsável principal");
        }
        
        // Verificar se o principal está na lista (após verificar se não é nulo)
        if (!responsaveis.contains(principal)) {
            throw new IllegalArgumentException("o responsável principal deve estar na lista de responsáveis");
        }
    }
}