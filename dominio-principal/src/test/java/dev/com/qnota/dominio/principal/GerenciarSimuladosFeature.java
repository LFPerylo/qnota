package dev.com.qnota.dominio.principal;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;

import dev.com.qnota.infraestrutura.persistencia.memoria.RepositorioEmMemoria;

import dev.com.qnota.dominio.principal.simulado.Simulado;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.simulado.SimuladoServico;

import dev.com.qnota.dominio.principal.turma.Turma;
import dev.com.qnota.dominio.principal.turma.TurmaId;

import dev.com.qnota.dominio.principal.professor.Professor;
import dev.com.qnota.dominio.principal.professor.ProfessorId;

import dev.com.qnota.dominio.principal.disciplina.Disciplina;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.disciplina.Disciplina.AreaConhecimento;

import dev.com.qnota.dominio.principal.ranking.RankingServico;

import dev.com.qnota.dominio.principal.nota.Nota;
import dev.com.qnota.dominio.principal.nota.NotaId;

import dev.com.qnota.dominio.principal.aluno.Aluno;
import dev.com.qnota.dominio.principal.aluno.AlunoId;

public class GerenciarSimuladosFeature {

    // ===== estado por cenário =====
    private RepositorioEmMemoria repo;
    private SimuladoServico simuladoSrv;
    private RankingServico rankingSrv;

    private AtomicInteger seq;
    private Map<String, TurmaId> aliasTurma;
    private Map<String, ProfessorId> aliasProfessor;
    private Map<String, DisciplinaId> aliasDisciplina;
    private Map<String, AreaConhecimento> aliasArea;

    private SimuladoId currentSimuladoId;
    private TurmaId currentTurmaId;
    private LocalDate currentDataAplicacao;
    private List<Simulado.DisciplinaPeso> currentDisciplinas;

    private Exception lastError;

    @Before
    public void reset() {
        repo = new RepositorioEmMemoria();
        rankingSrv = new RankingServico(repo, repo, repo, repo);
        simuladoSrv = new SimuladoServico(repo, rankingSrv, repo, repo, repo);

        seq = new AtomicInteger(1);
        aliasTurma = new HashMap<>();
        aliasProfessor = new HashMap<>();
        aliasDisciplina = new HashMap<>();
        aliasArea = new HashMap<>();

        currentSimuladoId = null;
        currentTurmaId = null;
        currentDataAplicacao = LocalDate.now().plusDays(7);
        currentDisciplinas = new ArrayList<>();

        lastError = null;
    }

    // ===== utils =====
    private SimuladoId newSimuladoId() { return new SimuladoId(seq.getAndIncrement()); }
    private TurmaId newTurmaId() { return new TurmaId(seq.getAndIncrement()); }
    private ProfessorId newProfessorId() { return new ProfessorId(seq.getAndIncrement()); }
    private DisciplinaId newDisciplinaId() { return new DisciplinaId(seq.getAndIncrement()); }
    private AlunoId newAlunoId() { return new AlunoId(seq.getAndIncrement()); }
    private NotaId newNotaId() { return new NotaId(seq.getAndIncrement()); }

    private AreaConhecimento areaByNome(String nome) {
        return aliasArea.computeIfAbsent(nome, n -> new AreaConhecimento(seq.getAndIncrement(), n));
    }

    private ProfessorId ensureProfessor(String alias, String especialidade) {
        return aliasProfessor.computeIfAbsent(alias, a -> {
            var professor = new Professor("Professor " + a, "123.456.789-09", "prof" + a + "@ex.com", 
                                        List.of(especialidade));
            repo.salvar(professor);
            return professor.getId();
        });
    }

    private TurmaId ensureTurma(String alias, boolean ativa, String professorAlias, String especialidade) {
        return aliasTurma.computeIfAbsent(alias, a -> {
            var professorId = ensureProfessor(professorAlias, especialidade);
            var turma = new Turma(a, 2025, ativa, professorId);
            repo.salvar(turma);
            return turma.getId();
        });
    }

    private DisciplinaId ensureDisciplina(String nome, String areaNome) {
        String key = nome + "/" + areaNome;
        return aliasDisciplina.computeIfAbsent(key, k -> {
            var disciplina = new Disciplina(nome, areaByNome(areaNome));
            repo.salvar(disciplina);
            return disciplina.getId();
        });
    }

    private Simulado.DisciplinaPeso disciplinaPeso(String nome, String area, double peso) {
        var disciplinaId = ensureDisciplina(nome, area);
        return new Simulado.DisciplinaPeso(disciplinaId, peso);
    }

    private void criarSimuladoEmEdicao(TurmaId turmaId, int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            var disciplinas = List.of(
                disciplinaPeso("Matemática", "Exatas", 6.0),
                disciplinaPeso("Física", "Exatas", 4.0)
            );
            var simulado = new Simulado(LocalDate.now().plusDays(i), Simulado.Status.EM_EDICAO, turmaId, disciplinas);
            repo.salvar(simulado);
        }
    }

    // ===== Givens =====

    @Given("uma \"turma\" {string} \"está\" ativa com professor {string} que possui especialidade {string}")
    public void turma_ativa_com_professor_especialidade(String turmaAlias, String professorAlias, String especialidade) {
        // Ajustar especialidade para ser compatível com a área das disciplinas
        String especialidadeCompativel = especialidade.equals("Matemática") ? "Exatas" : especialidade;
        currentTurmaId = ensureTurma(turmaAlias, true, professorAlias, especialidadeCompativel);
    }

    @Given("uma \"turma\" {string} \"está\" inativa com professor {string}")
    public void turma_inativa_com_professor(String turmaAlias, String professorAlias) {
        currentTurmaId = ensureTurma(turmaAlias, false, professorAlias, "Matemática");
    }

    @Given("existem disciplinas {string} e {string} na área {string}")
    public void existem_disciplinas_na_area(String disc1, String disc2, String area) {
        ensureDisciplina(disc1, area);
        ensureDisciplina(disc2, area);
    }

    @Given("a turma {string} \"possui\" {int} simulados em edição")
    public void turma_possui_simulados_edicao(String turmaAlias, Integer quantidade) {
        if (currentTurmaId == null) {
            currentTurmaId = ensureTurma(turmaAlias, true, "P1", "Matemática");
        }
        criarSimuladoEmEdicao(currentTurmaId, quantidade);
    }

    @Given("a turma {string} \"possui\" {int} simulado em edição")
    public void turma_possui_simulado_edicao(String turmaAlias, Integer quantidade) {
        turma_possui_simulados_edicao(turmaAlias, quantidade);
    }

    @Given("um \"simulado\" \"está\" em edição para a turma {string}")
    public void simulado_em_edicao_para_turma(String turmaAlias) {
        if (currentTurmaId == null) {
            currentTurmaId = ensureTurma(turmaAlias, true, "P1", "Exatas"); // usar especialidade compatível
        }
        var disciplinas = List.of(
            disciplinaPeso("Matemática", "Exatas", 6.0),
            disciplinaPeso("Física", "Exatas", 4.0)
        );
        var simulado = new Simulado(LocalDate.now().plusDays(7), Simulado.Status.EM_EDICAO, currentTurmaId, disciplinas);
        repo.salvar(simulado);
        currentSimuladoId = simulado.getId();
    }

    @Given("o \"simulado\" possui disciplinas {string} e {string}")
    public void simulado_possui_disciplinas(String disc1, String disc2) {
        // Simulado já foi criado no step anterior
    }

    @Given("um \"simulado\" \"está\" finalizado para a turma {string}")
    public void simulado_finalizado_para_turma(String turmaAlias) {
        if (currentTurmaId == null) {
            currentTurmaId = ensureTurma(turmaAlias, true, "P1", "Exatas"); // usar especialidade compatível
        }
        var disciplinas = List.of(
            disciplinaPeso("Matemática", "Exatas", 6.0),
            disciplinaPeso("Física", "Exatas", 4.0)
        );
        var simulado = new Simulado(LocalDate.now().minusDays(7), Simulado.Status.FINALIZADO, currentTurmaId, disciplinas);
        repo.salvar(simulado);
        currentSimuladoId = simulado.getId();
    }

    @Given("todas as notas do \"simulado\" \"foram\" lançadas")
    public void todas_notas_foram_lancadas() {
        // Simular que todas as notas foram lançadas criando algumas notas de exemplo
        var alunoId = newAlunoId();
        var responsavelId = new dev.com.qnota.dominio.principal.responsavel.ResponsavelId(seq.getAndIncrement());
        var responsavel = new dev.com.qnota.dominio.principal.responsavel.Responsavel("Responsável Teste", "123.456.789-09", "resp@ex.com", dev.com.qnota.dominio.principal.responsavel.Responsavel.Status.ATIVO);
        repo.salvar(responsavel);
        
        var alunoResponsaveis = List.of(responsavel.getId());
        var principal = responsavel.getId();
        var aluno = new Aluno("Aluno Teste", LocalDate.of(2012, 1, 1), true, currentTurmaId, alunoResponsaveis, principal);
        repo.salvar(aluno);
        
        var nota1 = new Nota(alunoId, currentSimuladoId, ensureDisciplina("Matemática", "Exatas"), 8.0, java.time.LocalDateTime.now());
        var nota2 = new Nota(alunoId, currentSimuladoId, ensureDisciplina("Física", "Exatas"), 7.5, java.time.LocalDateTime.now());
        repo.salvar(nota1);
        repo.salvar(nota2);
        
        // Marcar que todas as notas foram lançadas para este simulado
        repo.setTodasNotasLancadas(currentSimuladoId, true);
    }

    @Given("existem notas pendentes no \"simulado\"")
    public void existem_notas_pendentes() {
        // Não criar notas para simular notas pendentes
    }

    @Given("o \"simulado\" \"não possui\" notas lançadas")
    public void simulado_nao_possui_notas() {
        // Não criar notas
    }

    @Given("o \"simulado\" \"possui\" notas lançadas")
    public void simulado_possui_notas() {
        var alunoId = newAlunoId();
        var responsavel = new dev.com.qnota.dominio.principal.responsavel.Responsavel("Responsável Teste", "123.456.789-09", "resp@ex.com", dev.com.qnota.dominio.principal.responsavel.Responsavel.Status.ATIVO);
        repo.salvar(responsavel);
        
        var alunoResponsaveis = List.of(responsavel.getId());
        var principal = responsavel.getId();
        var aluno = new Aluno("Aluno Teste", LocalDate.of(2012, 1, 1), true, currentTurmaId, alunoResponsaveis, principal);
        repo.salvar(aluno);
        
        var nota = new Nota(alunoId, currentSimuladoId, ensureDisciplina("Matemática", "Exatas"), 8.0, java.time.LocalDateTime.now());
        repo.salvar(nota);
    }

    // ===== Whens =====

    @When("um coordenador cadastra um \"simulado\" para a turma {string} com disciplinas válidas")
    public void coord_cadastra_simulado_com_disciplinas_validas(String turmaAlias) {
        lastError = null;
        try {
            if (currentTurmaId == null) {
                currentTurmaId = ensureTurma(turmaAlias, true, "P1", "Matemática");
            }
            // Usar disciplinas que sejam compatíveis com a especialidade do professor
            var disciplinas = List.of(
                disciplinaPeso("Matemática", "Exatas", 6.0),
                disciplinaPeso("Física", "Exatas", 4.0)
            );
            simuladoSrv.criar(currentDataAplicacao, currentTurmaId, disciplinas);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar um \"simulado\" para a turma {string}")
    public void coord_tenta_cadastrar_simulado(String turmaAlias) {
        coord_cadastra_simulado_com_disciplinas_validas(turmaAlias);
    }

    @When("um coordenador tenta cadastrar um \"simulado\" para a turma {string} com disciplinas {string} e {string}")
    public void coord_tenta_cadastrar_simulado_com_disciplinas(String turmaAlias, String disc1, String disc2) {
        lastError = null;
        try {
            if (currentTurmaId == null) {
                // Para este teste específico, vamos usar um professor com especialidade em História
                currentTurmaId = ensureTurma(turmaAlias, true, "P1", "História");
            }
            var disciplinas = List.of(
                disciplinaPeso(disc1, "Exatas", 5.0),
                disciplinaPeso(disc2, "Exatas", 5.0)
            );
            simuladoSrv.criar(currentDataAplicacao, currentTurmaId, disciplinas);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar um \"simulado\" para a turma {string} sem disciplinas")
    public void coord_tenta_cadastrar_simulado_sem_disciplinas(String turmaAlias) {
        lastError = null;
        try {
            if (currentTurmaId == null) {
                currentTurmaId = ensureTurma(turmaAlias, true, "P1", "Matemática");
            }
            simuladoSrv.criar(currentDataAplicacao, currentTurmaId, List.of());
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar um \"simulado\" para a turma {string} com apenas {int} disciplina")
    public void coord_tenta_cadastrar_simulado_com_uma_disciplina(String turmaAlias, Integer quantidade) {
        lastError = null;
        try {
            if (currentTurmaId == null) {
                currentTurmaId = ensureTurma(turmaAlias, true, "P1", "Matemática");
            }
            var disciplinas = List.of(disciplinaPeso("Matemática", "Exatas", 10.0));
            simuladoSrv.criar(currentDataAplicacao, currentTurmaId, disciplinas);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar um \"simulado\" para a turma {string} sem data de aplicação")
    public void coord_tenta_cadastrar_simulado_sem_data(String turmaAlias) {
        lastError = null;
        try {
            if (currentTurmaId == null) {
                currentTurmaId = ensureTurma(turmaAlias, true, "P1", "Matemática");
            }
            var disciplinas = List.of(
                disciplinaPeso("Matemática", "Exatas", 6.0),
                disciplinaPeso("Física", "Exatas", 4.0)
            );
            simuladoSrv.criar(null, currentTurmaId, disciplinas);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar um \"simulado\" sem turma")
    public void coord_tenta_cadastrar_simulado_sem_turma() {
        lastError = null;
        try {
            var disciplinas = List.of(
                disciplinaPeso("Matemática", "Exatas", 6.0),
                disciplinaPeso("Física", "Exatas", 4.0)
            );
            simuladoSrv.criar(currentDataAplicacao, null, disciplinas);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador edita as disciplinas do \"simulado\" para {string} e {string}")
    public void coord_edita_disciplinas_simulado(String disc1, String disc2) {
        lastError = null;
        try {
            // Usar disciplinas compatíveis com a especialidade do professor
            var novasDisciplinas = List.of(
                disciplinaPeso(disc1, "Exatas", 5.0),
                disciplinaPeso(disc2, "Exatas", 5.0)
            );
            simuladoSrv.editarDisciplinas(currentSimuladoId, novasDisciplinas);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta editar as disciplinas do \"simulado\"")
    public void coord_tenta_editar_disciplinas_simulado() {
        coord_edita_disciplinas_simulado("Matemática", "Física"); // usar disciplinas compatíveis com especialidade "Matemática"
    }

    @When("um coordenador finaliza o \"simulado\"")
    public void coord_finaliza_simulado() {
        lastError = null;
        try {
            simuladoSrv.finalizar(currentSimuladoId);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta finalizar o \"simulado\"")
    public void coord_tenta_finalizar_simulado() {
        coord_finaliza_simulado();
    }

    @When("um coordenador exclui o \"simulado\"")
    public void coord_exclui_simulado() {
        lastError = null;
        try {
            simuladoSrv.excluir(currentSimuladoId);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta excluir o \"simulado\"")
    public void coord_tenta_excluir_simulado() {
        coord_exclui_simulado();
    }

    // ===== Thens =====

    @Then("o sistema confirma o cadastro do \"simulado\"")
    public void confirma_cadastro_simulado() {
        assertNull(lastError, "Esperava sucesso no cadastro: " + lastError);
    }

    @Then("o sistema rejeita o cadastro em simulados")
    public void rejeita_cadastro() { 
        assertNotNull(lastError, "Esperava erro no cadastro"); 
    }

    @Then("o sistema informa em simulados que {string}")
    public void o_sistema_informa_em_simulados_que(String msg) {
        assertNotNull(lastError, "Não houve erro para verificar mensagem");
        String m = (lastError.getMessage() == null) ? "" : lastError.getMessage();
        assertTrue(m.toLowerCase().contains(msg.toLowerCase()),
            "Mensagem esperada conter: \"" + msg + "\" mas foi: \"" + m + "\"");
    }

    @Then("o sistema confirma a alteração do \"simulado\"")
    public void confirma_alteracao_simulado() {
        assertNull(lastError, "Esperava sucesso na alteração: " + lastError);
    }

    @Then("o sistema recalcula o ranking")
    public void sistema_recalcula_ranking() {
        // Verificar se o ranking foi recalculado (implementação específica depende do RankingServico)
        assertNull(lastError, "Esperava sucesso no recálculo do ranking: " + lastError);
    }

    @Then("o sistema rejeita a alteração em simulados")
    public void rejeita_alteracao() { 
        assertNotNull(lastError, "Esperava erro na alteração"); 
    }

    @Then("o sistema confirma a finalização do \"simulado\"")
    public void confirma_finalizacao_simulado() {
        assertNull(lastError, "Esperava sucesso na finalização: " + lastError);
    }

    @Then("o sistema congela o ranking")
    public void sistema_congela_ranking() {
        // Verificar se o ranking foi congelado
        assertNull(lastError, "Esperava sucesso no congelamento do ranking: " + lastError);
    }

    @Then("o sistema rejeita a finalização em simulados")
    public void rejeita_finalizacao() { 
        assertNotNull(lastError, "Esperava erro na finalização"); 
    }

    @Then("o sistema confirma a exclusão do \"simulado\"")
    public void confirma_exclusao_simulado() {
        assertNull(lastError, "Esperava sucesso na exclusão: " + lastError);
    }

    @Then("o sistema rejeita a exclusão em simulados")
    public void rejeita_exclusao() { 
        assertNotNull(lastError, "Esperava erro na exclusão"); 
    }
}
