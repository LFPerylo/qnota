package dev.com.qnota.dominio.principal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;

import dev.com.qnota.infraestrutura.persistencia.memoria.RepositorioEmMemoria;

import dev.com.qnota.dominio.principal.turma.Turma;
import dev.com.qnota.dominio.principal.turma.TurmaId;
import dev.com.qnota.dominio.principal.turma.TurmaServico;

import dev.com.qnota.dominio.principal.professor.Professor;
import dev.com.qnota.dominio.principal.professor.ProfessorId;

import dev.com.qnota.dominio.principal.simulado.Simulado;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;

public class GerenciarTurmasFeature {

    // ===== estado por cenário =====
    private RepositorioEmMemoria repo;
    private TurmaServico turmaSrv;

    private AtomicInteger seq;
    private Map<String, TurmaId> aliasTurma;
    private Map<String, ProfessorId> aliasProfessor;

    private TurmaId currentTurmaId;
    private String currentNome;
    private int currentAnoLetivo;
    private ProfessorId currentProfessorId;
    private ProfessorId currentProfessorDestinoId;

    private Exception lastError;

    @Before
    public void reset() {
        repo = new RepositorioEmMemoria();
        turmaSrv = new TurmaServico(repo, repo);

        seq = new AtomicInteger(1);
        aliasTurma = new HashMap<>();
        aliasProfessor = new HashMap<>();

        currentTurmaId = null;
        currentNome = null;
        currentAnoLetivo = 0;
        currentProfessorId = null;
        currentProfessorDestinoId = null;

        lastError = null;
    }

    // ===== utils =====
    private TurmaId newTurmaId() { return new TurmaId(seq.getAndIncrement()); }
    private ProfessorId newProfessorId() { return new ProfessorId(seq.getAndIncrement()); }
    private SimuladoId newSimuladoId() { return new SimuladoId(seq.getAndIncrement()); }

    private ProfessorId ensureProfessor(String alias) {
        return aliasProfessor.computeIfAbsent(alias, a -> {
            var especialidades = List.of("Matemática", "Física");
            // Como o ID é gerado pelo repositório, vamos criar o professor e obter o ID gerado
            var professor = new Professor(a + " Nome", "12345678901", a.toLowerCase()+"@ex.com", especialidades);
            repo.salvar(professor);
            // Retornar o ID gerado pelo repositório
            return professor.getId();
        });
    }

    private ProfessorId ensureProfessorDefault(String alias) { 
        return ensureProfessor(alias); 
    }

    private TurmaId persistTurmaBasica(TurmaId id, String nome, int anoLetivo, ProfessorId professor) {
        // Como o ID é gerado pelo repositório, vamos salvar sem o ID específico
        var turma = new Turma(nome, anoLetivo, true, professor);
        repo.salvar(turma);
        // Retornar o ID gerado pelo repositório
        return turma.getId();
    }

    // helper para montar disciplinas de Simulado (RN-12 exige >= 2)
    private static Simulado.DisciplinaPeso dp(int id, double peso) {
        return new Simulado.DisciplinaPeso(new DisciplinaId(id), peso);
    }

    // ===== Givens =====

    @Given("uma \"turma\" com nome {string} e ano letivo {string} {string} registrada")
    public void turma_por_nome_ano_estado(String nome, String ano, String estado) {
        currentNome = nome;
        currentAnoLetivo = Integer.parseInt(ano);
        currentProfessorId = ensureProfessorDefault("P1");
        if ("já está".equalsIgnoreCase(estado)) {
            currentTurmaId = persistTurmaBasica(null, currentNome, currentAnoLetivo, currentProfessorId);
        } else {
            currentTurmaId = null;
        }
    }

    @Given("uma \"turma\" {string} registrada")
    public void turma_estado_registrada(String estado) {
        if ("está".equalsIgnoreCase(estado)) {
            currentNome = "Turma Teste";
            currentAnoLetivo = 2025;
            currentProfessorId = ensureProfessorDefault("P1");
            currentTurmaId = persistTurmaBasica(null, currentNome, currentAnoLetivo, currentProfessorId);
        } else {
            currentTurmaId = null;
            currentNome = "Nova Turma";
            currentAnoLetivo = 2025;
            currentProfessorId = ensureProfessorDefault("P1");
        }
    }

    @Given("uma \"turma\" {string} registrada e {string} simulados finalizados")
    public void turma_registrada_possui_finalizados(String estado, String possui) {
        turma_estado_registrada("está");
        if ("possui".equalsIgnoreCase(possui)) {
            var s = new Simulado(
                java.time.LocalDate.now().minusDays(10),
                Simulado.Status.FINALIZADO,
                currentTurmaId,
                // >>> duas disciplinas (RN-12)
                List.of(dp(1, 6.0), dp(2, 4.0))
            );
            repo.salvar(s);
        }
    }

    @Given("uma \"turma\" {string} registrada e {string} simulados em edição")
    public void turma_registrada_possui_em_edicao(String estado, String possui) {
        turma_estado_registrada("está");
        if ("possui".equalsIgnoreCase(possui)) {
            var s = new Simulado(
                java.time.LocalDate.now(),
                Simulado.Status.EM_EDICAO,
                currentTurmaId,
                // >>> duas disciplinas (RN-12)
                List.of(dp(1, 5.0), dp(2, 5.0))
            );
            repo.salvar(s);
        }
    }

    @Given("uma \"turma\" {string} registrada e {string} vínculos")
    public void turma_registrada_possui_vinculos(String estado, String possui) {
        turma_estado_registrada("está");
        if ("possui".equalsIgnoreCase(possui)) {
            // Criar aluno ativo na turma
            var alunoId = new dev.com.qnota.dominio.principal.aluno.AlunoId(seq.getAndIncrement());
            var responsavelId = new dev.com.qnota.dominio.principal.responsavel.ResponsavelId(seq.getAndIncrement());
            var responsaveis = List.of(new dev.com.qnota.dominio.principal.aluno.Aluno.AlunoResponsavel(responsavelId, true));
            
            repo.salvar(new dev.com.qnota.dominio.principal.responsavel.Responsavel("Responsável", "12345678909", "resp@ex.com", dev.com.qnota.dominio.principal.responsavel.Responsavel.Status.ATIVO));
            repo.salvar(new dev.com.qnota.dominio.principal.aluno.Aluno(alunoId, "Aluno Teste", java.time.LocalDate.of(2012, 1, 1), true, currentTurmaId, responsaveis));
        }
    }

    @Given("uma \"turma\" {string} registrada e {string} simulados")
    public void turma_registrada_possui_simulados(String estado, String possui) {
        turma_registrada_possui_finalizados(estado, possui);
    }

    @Given("uma \"turma\" {string} registrada e {string} alunos ativos")
    public void turma_registrada_possui_alunos_ativos(String estado, String possui) {
        turma_registrada_possui_vinculos(estado, possui);
    }


    @Given("uma \"turma\" {string} registrada e {string}")
    public void turma_registrada_e_flag(String estado, String flag) {
        currentTurmaId = null;
        currentNome = switch (flag) {
            case "sem nome" -> null;
            case "nome em branco" -> "   ";
            case "ano letivo inválido" -> "Turma Teste";
            default -> "Nova Turma";
        };
        currentAnoLetivo = "ano letivo inválido".equals(flag) ? -1 : 2025;
        currentProfessorId = "sem professor".equals(flag) ? null : ensureProfessorDefault("P1");
    }

    // ===== Whens =====

    @When("um coordenador cadastra a \"turma\" com professor válido")
    public void coord_cadastra_turma_com_professor_valido() {
        lastError = null;
        try {
            // Não fazer fallback para valores padrão - usar exatamente o que foi definido nos Given
            var turma = new Turma(currentNome, currentAnoLetivo, true, currentProfessorId);
            turmaSrv.criar(turma);
            // Atualizar currentTurmaId com o ID gerado pelo repositório
            currentTurmaId = turma.getId();
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador cadastra uma \"turma\" com nome {string} e ano letivo {string}")
    public void coord_cadastra_uma_turma_com_nome_ano(String nome, String ano) {
        lastError = null;
        try {
            currentNome = nome;
            currentAnoLetivo = Integer.parseInt(ano);
            currentProfessorId = ensureProfessorDefault("P1");
            var turma = new Turma(currentNome, currentAnoLetivo, true, currentProfessorId);
            turmaSrv.criar(turma);
            // Atualizar currentTurmaId com o ID gerado pelo repositório
            currentTurmaId = turma.getId();
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar uma \"turma\" com nome {string} e ano letivo {string}")
    public void coord_tenta_cadastrar_uma_turma_com_nome_ano(String nome, String ano) {
        lastError = null;
        try {
            currentNome = nome;
            currentAnoLetivo = Integer.parseInt(ano);
            currentProfessorId = ensureProfessorDefault("P1");
            var turma = new Turma(currentNome, currentAnoLetivo, true, currentProfessorId);
            turmaSrv.criar(turma);
            // Atualizar currentTurmaId com o ID gerado pelo repositório
            currentTurmaId = turma.getId();
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador renomeia a \"turma\" para {string}")
    public void coord_renomeia_turma(String novoNome) {
        lastError = null;
        try {
            // Como o ID é gerado pelo repositório, vamos usar o currentTurmaId se disponível
            if (currentTurmaId != null) {
                var turma = repo.porId(currentTurmaId).orElseThrow();
                turma.renomear(novoNome);
                repo.salvar(turma);
            }
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador troca o professor da \"turma\" para {string}")
    public void coord_troca_professor_turma(String professorAlias) {
        lastError = null;
        try {
            currentProfessorDestinoId = ensureProfessorDefault(professorAlias);
            turmaSrv.trocarProfessor(currentTurmaId, currentProfessorDestinoId);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta trocar o professor da \"turma\" para {string}")
    public void coord_tenta_trocar_professor_turma(String professorAlias) {
        coord_troca_professor_turma(professorAlias);
    }

    @When("um coordenador inativa a \"turma\"")
    public void coord_inativa_turma() {
        lastError = null;
        try { 
            turmaSrv.inativar(currentTurmaId); 
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta inativar a \"turma\"")
    public void coord_tenta_inativar_turma() { 
        coord_inativa_turma(); 
    }

    @When("um coordenador exclui a \"turma\"")
    public void coord_exclui_turma() {
        lastError = null;
        try { 
            turmaSrv.excluir(currentTurmaId); 
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta excluir a \"turma\"")
    public void coord_tenta_excluir_turma() { 
        coord_exclui_turma(); 
    }

    @When("um coordenador tenta cadastrar a \"turma\" com professor válido")
    public void coord_tenta_cadastrar_turma_com_professor_valido() {
        coord_cadastra_turma_com_professor_valido();
    }

    @When("um coordenador tenta cadastrar a \"turma\" sem professor")
    public void coord_tenta_cadastrar_turma_sem_professor() {
        lastError = null;
        try {
            currentNome = "Turma Sem Professor";
            currentAnoLetivo = 2025;
            currentProfessorId = null; // professor nulo
            currentTurmaId = newTurmaId();
            var turma = new Turma(currentNome, currentAnoLetivo, true, currentProfessorId);
            turmaSrv.criar(turma);
        } catch (Exception e) { lastError = e; }
    }

    // ===== Thens =====

    @Then("o sistema confirma o cadastro da \"turma\"")
    public void confirma_cadastro_turma() {
        assertNull(lastError, "Esperava sucesso, mas houve erro: " + (lastError == null ? "" : lastError.getMessage()));
        // Como o ID é gerado pelo repositório, vamos verificar se a turma existe pelo nome e ano
        assertTrue(repo.existeNomeNoAno(currentNome, currentAnoLetivo), 
                "Turma não foi cadastrada: " + currentNome + " - " + currentAnoLetivo);
    }

    @Then("o sistema rejeita o cadastro em turmas")
    public void rejeita_cadastro() { 
        assertNotNull(lastError, "Esperava erro no cadastro"); 
    }

    @Then("o sistema informa em turmas que {string}")
    public void o_sistema_informa_em_turmas_que(String msg) {
        assertNotNull(lastError, "Não houve erro para verificar mensagem");
        String m = (lastError.getMessage() == null) ? "" : lastError.getMessage();
        assertTrue(m.toLowerCase().contains(msg.toLowerCase()),
            "Mensagem esperada conter: \"" + msg + "\" mas foi: \"" + m + "\"");
    }

    @Then("o sistema confirma a alteração da \"turma\"")
    public void confirma_alteracao_turma() {
        assertNull(lastError, "Esperava sucesso na alteração: " + lastError);
        var t = repo.porId(currentTurmaId).orElseThrow();
        assertNotNull(t, "Turma não encontrada após alteração");
    }

    @Then("o sistema rejeita a alteração em turmas")
    public void rejeita_alteracao() { 
        assertNotNull(lastError, "Esperava erro na alteração"); 
    }

    @Then("o sistema confirma a inativação da \"turma\"")
    public void confirma_inativacao_turma() {
        assertNull(lastError, "Esperava sucesso na inativação: " + lastError);
        var t = repo.porId(currentTurmaId).orElseThrow();
        assertFalse(t.isAtivo(), "Turma ainda ativa após inativação");
    }

    @Then("o sistema rejeita a inativação em turmas")
    public void rejeita_inativacao() { 
        assertNotNull(lastError, "Esperava erro na inativação"); 
    }

    @Then("o sistema confirma a exclusão da \"turma\"")
    public void confirma_exclusao_turma() {
        assertNull(lastError, "Esperava sucesso na exclusão: " + lastError);
        var t = repo.porId(currentTurmaId);
        assertTrue(t.isEmpty(), "Turma ainda presente após exclusão");
    }

    @Then("o sistema rejeita a exclusão em turmas")
    public void rejeita_exclusao() { 
        assertNotNull(lastError, "Esperava erro na exclusão"); 
    }
}
