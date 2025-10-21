package dev.com.qnota.dominio.principal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;

import dev.com.qnota.infraestrutura.persistencia.memoria.RepositorioEmMemoria;

import dev.com.qnota.dominio.principal.professor.Professor;
import dev.com.qnota.dominio.principal.professor.ProfessorId;
import dev.com.qnota.dominio.principal.professor.ProfessorServico;

import dev.com.qnota.dominio.principal.turma.Turma;
import dev.com.qnota.dominio.principal.turma.TurmaId;

import dev.com.qnota.dominio.principal.simulado.Simulado;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;

public class GerenciarProfessoresFeature {

    // ===== estado por cenário =====
    private RepositorioEmMemoria repo;
    private ProfessorServico professorSrv;

    private AtomicInteger seq;
    private Map<String, ProfessorId> aliasProfessor;
    private Map<String, TurmaId> aliasTurma;

    private ProfessorId currentProfessorId;
    private String currentNome;
    private String currentCpf;
    private String currentEmail;
    private List<String> currentEspecialidades;
    private ProfessorId currentSubstitutoId;

    private Exception lastError;

    @Before
    public void reset() {
        repo = new RepositorioEmMemoria();
        professorSrv = new ProfessorServico(repo);

        seq = new AtomicInteger(1);
        aliasProfessor = new HashMap<>();
        aliasTurma = new HashMap<>();

        currentProfessorId = null;
        currentNome = null;
        currentCpf = null;
        currentEmail = null;
        currentEspecialidades = null;
        currentSubstitutoId = null;

        lastError = null;
    }

    // ===== utils =====
    private ProfessorId newProfessorId() { return new ProfessorId(seq.getAndIncrement()); }
    private TurmaId newTurmaId() { return new TurmaId(seq.getAndIncrement()); }

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

    private TurmaId ensureTurma(String alias) {
        return aliasTurma.computeIfAbsent(alias, a -> {
            var professorId = ensureProfessorDefault("P1");
            var turma = new Turma(a, 2025, true, professorId);
            repo.salvar(turma);
            // Retornar o ID gerado pelo repositório
            return turma.getId();
        });
    }

    private ProfessorId persistProfessorBasico(ProfessorId id, String nome, String cpf, String email, List<String> especialidades) {
        // Como o ID é gerado pelo repositório, vamos salvar sem o ID específico
        var professor = new Professor(nome, cpf, email, especialidades);
        repo.salvar(professor);
        return professor.getId();
    }

    // helper para montar disciplinas de Simulado (RN-12 exige >= 2)
    private static Simulado.DisciplinaPeso dp(int id, double peso) {
        return new Simulado.DisciplinaPeso(new DisciplinaId(id), peso);
    }

    // ===== Givens =====

    @Given("um \"professor\" com nome {string} e especialidade {string} {string} registrado")
    public void professor_por_nome_especialidade_estado(String nome, String especialidade, String estado) {
        currentNome = nome;
        currentCpf = "12345678901";
        currentEmail = "professor@ex.com";
        currentEspecialidades = List.of(especialidade);
        if ("já está".equalsIgnoreCase(estado)) {
            currentProfessorId = persistProfessorBasico(null, currentNome, currentCpf, currentEmail, currentEspecialidades);
        } else {
            currentProfessorId = null;
        }
    }

    @Given("um \"professor\" {string} registrado")
    public void professor_estado_registrado(String estado) {
        if ("está".equalsIgnoreCase(estado)) {
            currentNome = "Professor Teste";
            currentCpf = "12345678901";
            currentEmail = "professor@ex.com";
            currentEspecialidades = List.of("Matemática", "Física");
            currentProfessorId = persistProfessorBasico(null, currentNome, currentCpf, currentEmail, currentEspecialidades);
        } else {
            currentProfessorId = null;
            currentNome = "Novo Professor";
            currentCpf = "12345678901";
            currentEmail = "professor@ex.com";
            currentEspecialidades = List.of("Matemática");
        }
    }

    @Given("um \"professor\" com nome {string} e especialidades {string} {string} registrado")
    public void professor_por_nome_especialidades_vazias_estado(String nome, String esp1, String estado) {
        currentNome = nome;
        currentCpf = "12345678901";
        currentEmail = "professor@ex.com";
        if ("vazias".equals(esp1)) {
            currentEspecialidades = List.of(); // lista vazia
        } else {
            currentEspecialidades = List.of(esp1);
        }
        if ("já está".equalsIgnoreCase(estado)) {
            currentProfessorId = persistProfessorBasico(null, currentNome, currentCpf, currentEmail, currentEspecialidades);
        } else {
            currentProfessorId = null;
        }
    }

    @Given("um \"professor\" com especialidades {string} e {string} {string} registrado")
    public void professor_por_especialidades_estado(String esp1, String esp2, String estado) {
        currentNome = "Professor Teste";
        currentCpf = "12345678901";
        currentEmail = "professor@ex.com";
        currentEspecialidades = List.of(esp1, esp2);
        if ("já está".equalsIgnoreCase(estado)) {
            currentProfessorId = persistProfessorBasico(null, currentNome, currentCpf, currentEmail, currentEspecialidades);
        } else {
            currentProfessorId = null;
        }
    }

    @Given("um \"professor\" {string} registrado e {string} {int} turmas ativas")
    public void professor_registrado_e_turmas_ativas(String estado, String possui, Integer qtdTurmas) {
        professor_estado_registrado("está");
        if ("possui".equalsIgnoreCase(possui)) {
            // Criar turmas para o professor
            for (int i = 1; i <= qtdTurmas; i++) {
                var turmaId = newTurmaId();
                repo.salvar(new Turma("Turma" + i, 2025, true, currentProfessorId));
            }
        }
    }

    @Given("um \"professor\" {string} registrado e {string} simulados finalizados")
    public void professor_registrado_possui_finalizados(String estado, String possui) {
        professor_estado_registrado("está");
        if ("possui".equalsIgnoreCase(possui)) {
            // Criar turma para o professor atual
            var turma = new Turma("Turma Teste", 2025, true, currentProfessorId);
            repo.salvar(turma);
            var turmaId = turma.getId(); // Usar o ID da turma criada
            
            var s = new Simulado(
                java.time.LocalDate.now().minusDays(10),
                Simulado.Status.FINALIZADO,
                turmaId, // Use the actual generated turmaId
                List.of(dp(1, 6.0), dp(2, 4.0))
            );
            repo.salvar(s);
        }
    }

    @Given("um \"professor\" {string} registrado e {string} turmas ativas")
    public void professor_registrado_possui_turmas_ativas(String estado, String possui) {
        professor_estado_registrado("está");
        if ("possui".equalsIgnoreCase(possui)) {
            // Criar 3 ou 4 turmas para o professor dependendo do cenário
            int qtdTurmas = "3".equals(possui) ? 3 : 4;
            for (int i = 1; i <= qtdTurmas; i++) {
                var turmaId = newTurmaId();
                repo.salvar(new Turma("Turma" + i, 2025, true, currentProfessorId));
            }
        }
    }

    @Given("existe um professor {string} válido")
    public void existe_professor_valido(String alias) {
        ensureProfessorDefault(alias);
    }

    @Given("um \"professor\" {string} registrado e {string}")
    public void professor_registrado_e_flag(String estado, String flag) {
        currentProfessorId = null;
        currentNome = switch (flag) {
            case "sem nome" -> null;
            case "nome em branco" -> "   ";
            default -> "Novo Professor";
        };
        currentCpf = "sem CPF".equals(flag) ? null : "12345678901";
        currentEmail = "sem email".equals(flag) ? null : "professor@ex.com";
        currentEspecialidades = switch (flag) {
            case "especialidades vazias" -> List.of();
            case "especialidade vazia" -> List.of("");
            default -> List.of("Matemática");
        };
    }

    @Given("um \"professor\" com nome {string} e especialidades {string} e {string} {string} registrado")
    public void professor_por_nome_especialidades_e_estado(String nome, String esp1, String esp2, String estado) {
        currentNome = nome;
        currentCpf = "12345678901";
        currentEmail = "professor@ex.com";
        currentEspecialidades = List.of(esp1, esp2);
        if ("já está".equalsIgnoreCase(estado)) {
            currentProfessorId = persistProfessorBasico(null, currentNome, currentCpf, currentEmail, currentEspecialidades);
        } else {
            currentProfessorId = null;
        }
    }

    @Given("um \"professor\" com especialidade {string} {string} registrado")
    public void professor_por_especialidade_estado(String especialidade, String estado) {
        currentNome = "Professor Teste";
        currentCpf = "12345678901";
        currentEmail = "professor@ex.com";
        currentEspecialidades = List.of(especialidade);
        if ("já está".equalsIgnoreCase(estado) || "está".equalsIgnoreCase(estado)) {
            currentProfessorId = persistProfessorBasico(null, currentNome, currentCpf, currentEmail, currentEspecialidades);
        } else {
            currentProfessorId = null;
        }
    }

    // ===== Whens =====

    @When("um coordenador cadastra o \"professor\" com dados válidos")
    public void coord_cadastra_professor_com_dados_validos() {
        lastError = null;
        try {
            // Não fazer fallback para valores padrão - usar exatamente o que foi definido nos Given
            currentProfessorId = newProfessorId();
            professorSrv.cadastrar(currentNome, currentCpf, currentEmail, currentEspecialidades);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar o \"professor\" sem especialidades")
    public void coord_tenta_cadastrar_professor_sem_especialidades() {
        lastError = null;
        try {
            currentNome = "Professor Sem Especialidades";
            currentCpf = "12345678901";
            currentEmail = "professor@ex.com";
            currentEspecialidades = List.of(); // lista vazia
            currentProfessorId = newProfessorId();
            professorSrv.cadastrar(currentNome, currentCpf, currentEmail, currentEspecialidades);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar o \"professor\" com especialidades duplicadas")
    public void coord_tenta_cadastrar_professor_com_especialidades_duplicadas() {
        lastError = null;
        try {
            currentNome = "Professor Duplicado";
            currentCpf = "12345678901";
            currentEmail = "professor@ex.com";
            // Especialidades que se tornam duplicatas após normalização (case-insensitive)
            currentEspecialidades = List.of("Matemática", "matemática"); // duplicatas case-insensitive
            currentProfessorId = newProfessorId();
            professorSrv.cadastrar(currentNome, currentCpf, currentEmail, currentEspecialidades);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador atualiza os dados de contato do \"professor\"")
    public void coord_atualiza_dados_contato_professor() {
        lastError = null;
        try {
            professorSrv.atualizarContato(currentProfessorId, "Nome Atualizado", "novo@email.com");
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador adiciona a especialidade {string} ao \"professor\"")
    public void coord_adiciona_especialidade_professor(String especialidade) {
        lastError = null;
        try {
            professorSrv.adicionarEspecialidade(currentProfessorId, especialidade);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador remove a especialidade {string} do \"professor\"")
    public void coord_remove_especialidade_professor(String especialidade) {
        lastError = null;
        try {
            if (currentProfessorId == null) {
                currentProfessorId = persistProfessorBasico(null, currentNome, currentCpf, currentEmail, currentEspecialidades);
            }
            professorSrv.removerEspecialidade(currentProfessorId, especialidade);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta remover a única especialidade do \"professor\"")
    public void coord_tenta_remover_unica_especialidade_professor() {
        lastError = null;
        try {
            if (currentProfessorId == null) {
                currentProfessorId = persistProfessorBasico(null, currentNome, currentCpf, currentEmail, currentEspecialidades);
            }
            professorSrv.removerEspecialidade(currentProfessorId, "Matemática");
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador valida o limite de turmas do \"professor\"")
    public void coord_valida_limite_turmas_professor() {
        lastError = null;
        try {
            professorSrv.validarLimiteDeTurmas(currentProfessorId);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta validar o limite de turmas do \"professor\"")
    public void coord_tenta_validar_limite_turmas_professor() {
        coord_valida_limite_turmas_professor();
    }

    @When("um coordenador exclui o \"professor\" com substituto")
    public void coord_exclui_professor_com_substituto() {
        lastError = null;
        try {
            currentSubstitutoId = ensureProfessorDefault("substituto");
            professorSrv.removerComSubstituto(currentProfessorId, currentSubstitutoId);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta excluir o \"professor\" com substituto")
    public void coord_tenta_excluir_professor_com_substituto() {
        coord_exclui_professor_com_substituto();
    }

    @When("um coordenador tenta cadastrar o \"professor\" com dados válidos")
    public void coord_tenta_cadastrar_professor_com_dados_validos() {
        coord_cadastra_professor_com_dados_validos();
    }

    @When("um coordenador tenta cadastrar o \"professor\" com especialidade vazia")
    public void coord_tenta_cadastrar_professor_com_especialidade_vazia() {
        lastError = null;
        try {
            currentNome = "Professor Especialidade Vazia";
            currentCpf = "12345678901";
            currentEmail = "professor@ex.com";
            currentEspecialidades = List.of(""); // especialidade vazia
            currentProfessorId = newProfessorId();
            professorSrv.cadastrar(currentNome, currentCpf, currentEmail, currentEspecialidades);
        } catch (Exception e) { lastError = e; }
    }

    // ===== Thens =====

    @Then("o sistema confirma o cadastro do \"professor\"")
    public void confirma_cadastro_professor() {
        assertNull(lastError, "Esperava sucesso, mas houve erro: " + (lastError == null ? "" : lastError.getMessage()));
        assertNotNull(currentProfessorId, "Sem ID atual de professor após cadastro");
        var p = repo.porId(currentProfessorId);
        assertTrue(p.isPresent(), "Professor não foi persistido");
    }

    @Then("o sistema rejeita o cadastro em professores")
    public void rejeita_cadastro() { 
        assertNotNull(lastError, "Esperava erro no cadastro"); 
    }

    @Then("o sistema informa em professores que {string}")
    public void o_sistema_informa_em_professores_que(String msg) {
        assertNotNull(lastError, "Não houve erro para verificar mensagem");
        String m = (lastError.getMessage() == null) ? "" : lastError.getMessage();
        assertTrue(m.toLowerCase().contains(msg.toLowerCase()),
            "Mensagem esperada conter: \"" + msg + "\" mas foi: \"" + m + "\"");
    }

    @Then("o sistema confirma a alteração do \"professor\"")
    public void confirma_alteracao_professor() {
        assertNull(lastError, "Esperava sucesso na alteração: " + lastError);
        var p = repo.porId(currentProfessorId).orElseThrow();
        assertNotNull(p, "Professor não encontrado após alteração");
    }

    @Then("o sistema rejeita a alteração em professores")
    public void rejeita_alteracao() { 
        assertNotNull(lastError, "Esperava erro na alteração"); 
    }

    @Then("o sistema confirma que o \"professor\" está dentro do limite")
    public void confirma_professor_dentro_limite() {
        assertNull(lastError, "Esperava sucesso na validação: " + lastError);
    }

    @Then("o sistema rejeita a validação em professores")
    public void rejeita_validacao() { 
        assertNotNull(lastError, "Esperava erro na validação"); 
    }

    @Then("o sistema confirma a exclusão do \"professor\"")
    public void confirma_exclusao_professor() {
        assertNull(lastError, "Esperava sucesso na exclusão: " + lastError);
        var p = repo.porId(currentProfessorId);
        assertTrue(p.isEmpty(), "Professor ainda presente após exclusão");
    }

    @Then("o sistema rejeita a exclusão em professores")
    public void rejeita_exclusao() { 
        assertNotNull(lastError, "Esperava erro na exclusão"); 
    }
}
