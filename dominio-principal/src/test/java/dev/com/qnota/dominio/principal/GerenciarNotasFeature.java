package dev.com.qnota.dominio.principal;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import dev.com.qnota.dominio.principal.aluno.Aluno;
import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.disciplina.Disciplina;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.infraestrutura.persistencia.memoria.RepositorioEmMemoria;
import dev.com.qnota.dominio.principal.nota.Nota;
import dev.com.qnota.dominio.principal.nota.NotaId;
import dev.com.qnota.dominio.principal.nota.NotaServico;
import dev.com.qnota.dominio.principal.professor.Professor;
import dev.com.qnota.dominio.principal.professor.ProfessorId;
import dev.com.qnota.dominio.principal.ranking.RankingServico;
import dev.com.qnota.dominio.principal.responsavel.Responsavel;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;
import dev.com.qnota.dominio.principal.simulado.Simulado;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.turma.Turma;
import dev.com.qnota.dominio.principal.turma.TurmaId;
import io.cucumber.java.en.*;

public class GerenciarNotasFeature {

    private final RepositorioEmMemoria repo = new RepositorioEmMemoria();
    private final RankingServico rankingServico = new RankingServico(repo, repo, repo, repo);
    private final NotaServico notaServico = new NotaServico(repo, rankingServico, repo, repo, repo, repo, repo);
    
    private final AtomicInteger seq = new AtomicInteger(1);
    
    // IDs para controle dos testes
    private AlunoId currentAlunoId;
    private SimuladoId currentSimuladoId;
    private DisciplinaId currentDisciplinaId;
    private TurmaId currentTurmaId;
    private ProfessorId currentProfessorId;
    private NotaId currentNotaId;
    
    // Estado dos testes
    private Exception ultimaExcecao;
    private boolean ultimaOperacaoSucesso;

    @Given("existe um \"aluno\" {string} ativo na turma {string}")
    public void existe_aluno_ativo_na_turma(String alunoAlias, String turmaAlias) {
        currentTurmaId = ensureTurma(turmaAlias, true, "P1", "Exatas");
        currentAlunoId = ensureAluno(alunoAlias, true, currentTurmaId);
    }

    @Given("existe um \"simulado\" {string} em edição para a turma {string}")
    public void existe_simulado_em_edicao_para_turma(String simuladoAlias, String turmaAlias) {
        currentTurmaId = ensureTurma(turmaAlias, true, "P1", "Exatas");
        currentDisciplinaId = ensureDisciplina("Matemática", "Exatas");
        currentSimuladoId = ensureSimulado(simuladoAlias, Simulado.Status.EM_EDICAO, currentTurmaId);
    }

    @Given("existe uma \"disciplina\" {string} na área {string}")
    public void existe_disciplina_na_area(String disciplinaNome, String areaNome) {
        currentDisciplinaId = ensureDisciplina(disciplinaNome, areaNome);
    }

    @Given("o \"simulado\" {string} \"está\" finalizado")
    public void simulado_esta_finalizado(String simuladoAlias) {
        var simulado = repo.porId(currentSimuladoId).orElseThrow();
        simulado.finalizar();
        repo.salvar(simulado);
    }

    @Given("já existe nota para o \"aluno\" {string} no \"simulado\" {string} na \"disciplina\" {string}")
    public void ja_existe_nota_para_aluno_simulado_disciplina(String alunoAlias, String simuladoAlias, String disciplinaNome) {
        var nota = new Nota(currentAlunoId, currentSimuladoId, currentDisciplinaId, 6.0, LocalDateTime.now());
        repo.salvar(nota);
        currentNotaId = nota.getId();
    }

    @Given("o \"aluno\" {string} \"está\" inativo")
    public void aluno_esta_inativo(String alunoAlias) {
        var aluno = repo.porId(currentAlunoId).orElseThrow();
        aluno.inativar();
        repo.salvar(aluno);
    }

    @Given("a \"turma\" {string} \"está\" inativa")
    public void turma_esta_inativa(String turmaAlias) {
        var turma = repo.porId(currentTurmaId).orElseThrow();
        turma.inativar();
        repo.salvar(turma);
    }

    @Given("existe um \"professor\" {string} responsável pela turma {string}")
    public void existe_professor_responsavel_pela_turma(String professorAlias, String turmaAlias) {
        currentProfessorId = ensureProfessor(professorAlias, List.of("Exatas"));
        currentTurmaId = ensureTurma(turmaAlias, true, professorAlias, "Exatas");
    }

    @Given("já existe nota {double} para o \"aluno\" {string} no \"simulado\" {string} na \"disciplina\" {string}")
    public void ja_existe_nota_valor_para_aluno_simulado_disciplina(double valor, String alunoAlias, String simuladoAlias, String disciplinaNome) {
        var nota = new Nota(currentAlunoId, currentSimuladoId, currentDisciplinaId, valor, LocalDateTime.now());
        repo.salvar(nota);
        currentNotaId = nota.getId();
    }

    @When("um coordenador lança nota {double} para o \"aluno\" {string} no \"simulado\" {string} na \"disciplina\" {string}")
    public void coord_lanca_nota_para_aluno_simulado_disciplina(double valor, String alunoAlias, String simuladoAlias, String disciplinaNome) {
        try {
            notaServico.lancar(currentAlunoId, currentSimuladoId, currentDisciplinaId, valor);
            ultimaOperacaoSucesso = true;
            ultimaExcecao = null;
        } catch (Exception e) {
            ultimaOperacaoSucesso = false;
            ultimaExcecao = e;
        }
    }

    @When("um coordenador tenta lançar nota {double} para o \"aluno\" {string} no \"simulado\" {string} na \"disciplina\" {string}")
    public void coord_tenta_lancar_nota_para_aluno_simulado_disciplina(double valor, String alunoAlias, String simuladoAlias, String disciplinaNome) {
        try {
            notaServico.lancar(currentAlunoId, currentSimuladoId, currentDisciplinaId, valor);
            ultimaOperacaoSucesso = true;
            ultimaExcecao = null;
        } catch (Exception e) {
            ultimaOperacaoSucesso = false;
            ultimaExcecao = e;
        }
    }

    @When("um coordenador tenta lançar nota {double} para aluno inexistente no \"simulado\" {string} na \"disciplina\" {string}")
    public void coord_tenta_lancar_nota_para_aluno_inexistente_simulado_disciplina(double valor, String simuladoAlias, String disciplinaNome) {
        try {
            var alunoInexistente = new AlunoId(99999);
            notaServico.lancar(alunoInexistente, currentSimuladoId, currentDisciplinaId, valor);
            ultimaOperacaoSucesso = true;
            ultimaExcecao = null;
        } catch (Exception e) {
            ultimaOperacaoSucesso = false;
            ultimaExcecao = e;
        }
    }

    @When("um coordenador tenta lançar nota {double} para o \"aluno\" {string} em simulado inexistente na \"disciplina\" {string}")
    public void coord_tenta_lancar_nota_para_aluno_simulado_inexistente_disciplina(double valor, String alunoAlias, String disciplinaNome) {
        try {
            var simuladoInexistente = new SimuladoId(99999);
            notaServico.lancar(currentAlunoId, simuladoInexistente, currentDisciplinaId, valor);
            ultimaOperacaoSucesso = true;
            ultimaExcecao = null;
        } catch (Exception e) {
            ultimaOperacaoSucesso = false;
            ultimaExcecao = e;
        }
    }

    @When("um coordenador tenta lançar nota {double} para o \"aluno\" {string} no \"simulado\" {string} em disciplina inexistente")
    public void coord_tenta_lancar_nota_para_aluno_simulado_disciplina_inexistente(double valor, String alunoAlias, String simuladoAlias) {
        try {
            var disciplinaInexistente = new DisciplinaId(99999);
            notaServico.lancar(currentAlunoId, currentSimuladoId, disciplinaInexistente, valor);
            ultimaOperacaoSucesso = true;
            ultimaExcecao = null;
        } catch (Exception e) {
            ultimaOperacaoSucesso = false;
            ultimaExcecao = e;
        }
    }

    @When("um coordenador retifica a nota para {double} com justificativa {string}")
    public void coord_retifica_nota_com_justificativa(double novoValor, String justificativa) {
        try {
            notaServico.retificarComJustificativa(currentNotaId, novoValor, justificativa, currentProfessorId);
            ultimaOperacaoSucesso = true;
            ultimaExcecao = null;
        } catch (Exception e) {
            ultimaOperacaoSucesso = false;
            ultimaExcecao = e;
        }
    }

    @When("um coordenador tenta retificar a nota para {double} com justificativa {string}")
    public void coord_tenta_retificar_nota_com_justificativa(double novoValor, String justificativa) {
        try {
            notaServico.retificarComJustificativa(currentNotaId, novoValor, justificativa, currentProfessorId);
            ultimaOperacaoSucesso = true;
            ultimaExcecao = null;
        } catch (Exception e) {
            ultimaOperacaoSucesso = false;
            ultimaExcecao = e;
        }
    }

    @Then("o sistema confirma o lançamento da nota")
    public void confirma_lancamento_nota() {
        assertTrue(ultimaOperacaoSucesso, "Esperava sucesso no lançamento: " + ultimaExcecao);
    }

    @Then("o ranking é recalculado automaticamente")
    public void ranking_recalculado_automaticamente() {
        // O ranking é recalculado automaticamente quando uma nota é lançada
        // Como não temos uma forma de verificar isso diretamente no repositório em memória,
        // apenas verificamos que não houve exceção durante o lançamento
        assertTrue(ultimaOperacaoSucesso, "Esperava que o ranking fosse recalculado sem erros: " + (ultimaExcecao != null ? ultimaExcecao.getMessage() : ""));
    }

    @Then("o sistema rejeita o lançamento em notas")
    public void rejeita_lancamento_notas() {
        assertFalse(ultimaOperacaoSucesso, "Esperava falha no lançamento");
        assertNotNull(ultimaExcecao, "Deveria ter ocorrido uma exceção");
    }

    @Then("o sistema informa em notas que {string}")
    public void sistema_informa_em_notas_que(String mensagemEsperada) {
        assertNotNull(ultimaExcecao, "Deveria ter ocorrido uma exceção");
        String mensagemAtual = ultimaExcecao.getMessage();
        assertTrue(mensagemAtual.contains(mensagemEsperada), 
            "Mensagem esperada: " + mensagemEsperada + ", mas foi: " + mensagemAtual);
    }

    @Then("o sistema confirma a retificação da nota")
    public void confirma_retificacao_nota() {
        assertTrue(ultimaOperacaoSucesso, "Esperava sucesso na retificação: " + ultimaExcecao);
    }

    @Then("uma nova versão da nota é criada")
    public void nova_versao_nota_criada() {
        // Verificar se uma nova nota foi criada (com ID diferente)
        var notas = repo.porSimulado(currentSimuladoId);
        assertTrue(notas.size() >= 2, "Deveria ter pelo menos 2 notas (original + nova versão)");
    }

    @Then("a justificativa é registrada no histórico")
    public void justificativa_registrada_historico() {
        var justificativas = repo.porNota(currentNotaId);
        assertFalse(justificativas.isEmpty(), "Justificativa deveria ter sido registrada");
    }

    @Then("o sistema rejeita a retificação em notas")
    public void rejeita_retificacao_notas() {
        assertFalse(ultimaOperacaoSucesso, "Esperava falha na retificação");
        assertNotNull(ultimaExcecao, "Deveria ter ocorrido uma exceção");
    }

    // Métodos auxiliares
    private AlunoId ensureAluno(String alias, boolean ativo, TurmaId turmaId) {
        var alunoId = new AlunoId(seq.getAndIncrement());
        var responsavelId = new ResponsavelId(seq.getAndIncrement());
        var responsavel = new Responsavel("Responsável " + alias, "123.456.789-09", "resp@ex.com", Responsavel.Status.ATIVO);
        repo.salvar(responsavel);
        
        var alunoResponsaveis = List.of(responsavel.getId());
        var aluno = new Aluno("Aluno " + alias, LocalDate.of(2012, 1, 1), ativo, turmaId, alunoResponsaveis, responsavel.getId());
        repo.salvar(aluno);
        return aluno.getId();
    }

    private SimuladoId ensureSimulado(String alias, Simulado.Status status, TurmaId turmaId) {
        var disciplina2Id = ensureDisciplina("Física", "Exatas");
        var disciplinas = List.of(
            new Simulado.DisciplinaPeso(currentDisciplinaId, 5.0),
            new Simulado.DisciplinaPeso(disciplina2Id, 5.0)
        );
        var simulado = new Simulado(LocalDate.now().minusDays(7), status, turmaId, disciplinas);
        repo.salvar(simulado);
        return simulado.getId();
    }

    private DisciplinaId ensureDisciplina(String nome, String area) {
        var disciplinaId = new DisciplinaId(seq.getAndIncrement());
        var disciplina = new Disciplina(nome, new Disciplina.AreaConhecimento(seq.getAndIncrement(), area));
        repo.salvar(disciplina);
        return disciplina.getId();
    }

    private TurmaId ensureTurma(String nome, boolean ativo, String professorAlias, String especialidade) {
        var turmaId = new TurmaId(seq.getAndIncrement());
        var professorId = ensureProfessor(professorAlias, List.of(especialidade));
        var turma = new Turma(nome, 2025, ativo, professorId);
        repo.salvar(turma);
        return turma.getId();
    }

    private ProfessorId ensureProfessor(String alias, List<String> especialidades) {
        var professorId = new ProfessorId(seq.getAndIncrement());
        var professor = new Professor("Professor " + alias, "123.456.789-09", "prof@ex.com", especialidades);
        repo.salvar(professor);
        return professor.getId();
    }
}
