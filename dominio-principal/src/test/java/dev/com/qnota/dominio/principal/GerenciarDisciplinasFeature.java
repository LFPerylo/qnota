package dev.com.qnota.dominio.principal;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;

import dev.com.qnota.infraestrutura.persistencia.memoria.RepositorioEmMemoria;

import dev.com.qnota.dominio.principal.disciplina.Disciplina;
import dev.com.qnota.dominio.principal.disciplina.Disciplina.AreaConhecimento;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaServico;

import dev.com.qnota.dominio.principal.simulado.Simulado;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;

public class GerenciarDisciplinasFeature {

    // ===== estado por cenário =====
    private RepositorioEmMemoria repo;
    private DisciplinaServico discSrv;

    private AtomicInteger seq;

    private Map<String, DisciplinaId> aliasDisciplina;
    private Map<String, AreaConhecimento> aliasArea;

    private DisciplinaId currentDisciplinaId;
    private String currentNome;
    private AreaConhecimento currentArea;

    private DisciplinaId novaVersaoIdCriada; // para asserts de RN-62
    private Exception lastError;

    // ===== setup =====
    @Before
    public void reset() {
        repo = new RepositorioEmMemoria();
        discSrv = new DisciplinaServico(repo);
        seq = new AtomicInteger(1);
        aliasDisciplina = new HashMap<>();
        aliasArea = new HashMap<>();
        currentDisciplinaId = null;
        currentNome = null;
        currentArea = null;
        novaVersaoIdCriada = null;
        lastError = null;
    }

    // ===== utils =====
    private DisciplinaId newDisciplinaId() { return new DisciplinaId(seq.getAndIncrement()); }
    private SimuladoId newSimuladoId() { return new SimuladoId(seq.getAndIncrement()); }

    private AreaConhecimento areaByNome(String nome) {
        return aliasArea.computeIfAbsent(nome, n -> new AreaConhecimento(seq.getAndIncrement(), n));
    }

    private DisciplinaId ensureDisciplina(String keyNome, String areaNome) {
        String alias = keyNome + "/" + areaNome;
        return aliasDisciplina.computeIfAbsent(alias, a -> {
            var id = newDisciplinaId();
            var d = new Disciplina(id, keyNome, 1, null, true, areaByNome(areaNome));
            repo.salvar(d);
            return id;
        });
    }

    private Optional<Disciplina> byId(DisciplinaId id) { return repo.porId(id); }

    private static Simulado.DisciplinaPeso dp(int did, double peso) {
        return new Simulado.DisciplinaPeso(new DisciplinaId(did), peso);
    }

    private void vincularEmSimulado(DisciplinaId dId, Simulado.Status status) {
        var sim = new Simulado(
            newSimuladoId(),
            LocalDate.now().minusDays(status == Simulado.Status.FINALIZADO ? 10 : 0),
            status,
            new dev.com.qnota.dominio.principal.turma.TurmaId(seq.getAndIncrement()),
            java.util.List.of(
                dp(dId.value(), 5.0),
                dp(seq.getAndIncrement() + 1000, 5.0) // uma disciplina dummy
            )
        );
        repo.salvar(sim);
    }

    // Localiza por nome em qualquer área previamente registrada
    private DisciplinaId findByNomeAnyAreaOrEnsure(String nome) {
        for (var e : aliasDisciplina.entrySet()) {
            if (e.getKey().startsWith(nome + "/")) return e.getValue();
        }
        // se não achar, cria em área atual (se houver) ou em uma padrão
        String area = (currentArea != null) ? currentArea.nome() : "Área Padrão";
        return ensureDisciplina(nome, area);
    }

    // ===== Givens =====

    @Given("um repositório em memória limpo")
    public void repo_memoria_limpo() {
        assertNotNull(repo);
    }

    @Given("uma \"disciplina\" com nome {string} e área {string} que {string} registrada")
    public void disciplina_por_nome_area_estado(String nome, String areaNome, String estado) {
        currentNome = nome;
        currentArea = areaByNome(areaNome);
        if ("já está".equalsIgnoreCase(estado)) {
            currentDisciplinaId = ensureDisciplina(nome, areaNome);
        } else {
            currentDisciplinaId = null;
        }
    }

    @Given("a \"disciplina\" {string} foi usada em simulados finalizados")
    public void disciplina_foi_usada_finalizados(String ignored) {
        if (currentDisciplinaId == null) currentDisciplinaId = ensureDisciplina(currentNome, currentArea.nome());
        vincularEmSimulado(currentDisciplinaId, Simulado.Status.FINALIZADO);
    }

    @Given("a \"disciplina\" {string} não foi usada em simulados finalizados")
    public void disciplina_nao_foi_usada_finalizados(String ignored) {
        // no-op
    }

    @Given("a \"disciplina\" {string} foi usada em simulados em edição")
    public void disciplina_foi_usada_em_edicao(String ignored) {
        if (currentDisciplinaId == null) currentDisciplinaId = ensureDisciplina(currentNome, currentArea.nome());
        vincularEmSimulado(currentDisciplinaId, Simulado.Status.EM_EDICAO);
    }

    @Given("a \"disciplina\" {string} não foi usada em simulados (qualquer status)")
    public void disciplina_nao_foi_usada_any_1param(String ignored) {
        // no-op
    }

    @Given("a {string} {string} usada em simulados finalizados")
    public void a_flag_usada_em_simulados_finalizados(String ignoredTipo, String foi) {
        if (currentDisciplinaId == null) {
            // se não existir contexto, cria algo padrão para poder marcar uso
            currentNome = (currentNome != null) ? currentNome : "Disciplina " + seq.getAndIncrement();
            currentArea = (currentArea != null) ? currentArea : areaByNome("Área Padrão");
            currentDisciplinaId = ensureDisciplina(currentNome, currentArea.nome());
        }
        if ("foi".equalsIgnoreCase(foi)) {
            vincularEmSimulado(currentDisciplinaId, Simulado.Status.FINALIZADO);
        }
        // "não foi" => no-op
    }

    @Given("a {string} {string} usada em simulados em edição")
    public void a_flag_usada_em_simulados_em_edicao(String ignoredTipo, String foi) {
        if (currentDisciplinaId == null) {
            currentNome = (currentNome != null) ? currentNome : "Disciplina " + seq.getAndIncrement();
            currentArea = (currentArea != null) ? currentArea : areaByNome("Área Padrão");
            currentDisciplinaId = ensureDisciplina(currentNome, currentArea.nome());
        }
        if ("foi".equalsIgnoreCase(foi)) {
            vincularEmSimulado(currentDisciplinaId, Simulado.Status.EM_EDICAO);
        }
    }

    @Given("a {string} {string} usada em simulados \\(qualquer status)")
    public void a_flag_usada_em_simulados_qualquer_status(String ignoredTipo, String foi) {
        if (currentDisciplinaId == null) {
            currentNome = (currentNome != null) ? currentNome : "Disciplina " + seq.getAndIncrement();
            currentArea = (currentArea != null) ? currentArea : areaByNome("Área Padrão");
            currentDisciplinaId = ensureDisciplina(currentNome, currentArea.nome());
        }
        if ("foi".equalsIgnoreCase(foi)) {
            // basta um vínculo (em edição) para caracterizar "usada"
            vincularEmSimulado(currentDisciplinaId, Simulado.Status.EM_EDICAO);
        }
    }

    @Given("a {string} {string} {string} usada em simulados finalizados")
    public void a_disciplina_nome_flag_usada_finalizados(String ignoredTipo, String nome, String foi) {
        // Garante que estamos falando da disciplina correta pelo nome
        currentDisciplinaId = findByNomeAnyAreaOrEnsure(nome);
        currentNome = nome; // mantém coerência de contexto
        if (currentArea == null) {
            // tenta inferir área a partir da chave salva
            for (var e : aliasDisciplina.entrySet()) {
                if (e.getValue().equals(currentDisciplinaId)) {
                    String key = e.getKey(); // "Nome/Área"
                    String area = key.substring(key.indexOf('/') + 1);
                    currentArea = areaByNome(area);
                    break;
                }
            }
        }
        if ("foi".equalsIgnoreCase(foi)) {
            vincularEmSimulado(currentDisciplinaId, Simulado.Status.FINALIZADO);
        }
    }

    // ===== Whens =====

    @When("um coordenador cadastra a \"disciplina\" com dados válidos")
    public void coord_cadastra_disciplina_com_dados_validos() {
        lastError = null;
        try {
            if (currentNome == null) currentNome = "Nova Disciplina";
            if (currentArea == null) currentArea = areaByNome("Área Padrão");
            discSrv.cadastrar(currentNome, currentArea);
            currentDisciplinaId = new DisciplinaId(repo.maxIdDisciplina());
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador cadastra a \"disciplina\" com nome {string} na área {string}")
    public void coord_cadastra_disciplina_nome_area(String nome, String areaNome) {
        lastError = null;
        try {
            discSrv.cadastrar(nome, areaByNome(areaNome));
            currentDisciplinaId = new DisciplinaId(repo.maxIdDisciplina());
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar a \"disciplina\" com nome {string} na área {string}")
    public void coord_tenta_cadastrar_disciplina_nome_area(String nome, String areaNome) {
        coord_cadastra_disciplina_nome_area(nome, areaNome);
    }

    @When("um coordenador edita a \"disciplina\" para nome {string} e área {string}")
    public void coord_edita_disciplina(String novoNome, String novaAreaNome) {
        lastError = null;
        try {
            var before = repo.porId(currentDisciplinaId).orElseThrow();
            discSrv.editar(currentDisciplinaId, novoNome, areaByNome(novaAreaNome));

            // Detecta nova versão pelo (nome, área) e versao == before.versao+1
            novaVersaoIdCriada = repo.findDisciplinaByNomeArea(novoNome, novaAreaNome)
                    .filter(d -> d.getVersao() == before.getVersao() + 1)
                    .map(Disciplina::getId)
                    .orElse(null);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta editar {string} para nome {string} e área {string}")
    public void coord_tenta_editar_para_nome_area(String ignoredAlias, String novoNome, String novaAreaNome) {
        coord_edita_disciplina(novoNome, novaAreaNome);
    }

    @When("um coordenador exclui a \"disciplina\"")
    public void coord_exclui_disciplina() {
        lastError = null;
        try { discSrv.excluir(currentDisciplinaId); } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta excluir a \"disciplina\"")
    public void coord_tenta_excluir_disciplina() { coord_exclui_disciplina(); }

    // ===== Thens =====

    @Then("o sistema confirma o cadastro da \"disciplina\" v1 ativa")
    public void confirma_cadastro_disciplina_v1() {
        assertNull(lastError, "Esperava sucesso no cadastro: " + lastError);
        var d = repo.porId(currentDisciplinaId).orElseThrow();
        assertEquals(1, d.getVersao(), "Versão inicial deve ser 1");
        assertTrue(d.isAtivo(), "Disciplina deve iniciar ativa");
        assertNull(d.getIdVersaoOrigem(), "idVersaoOrigem deve ser nulo na v1");
    }

    @Then("o sistema rejeita o cadastro em disciplinas")
    public void rejeita_cadastro() { assertNotNull(lastError, "Esperava erro no cadastro"); }

    @Then("o sistema informa em disciplinas que {string}")
    public void o_sistema_informa_em_disciplinas_que(String msg) {
        assertNotNull(lastError, "Não houve erro para verificar mensagem");
        var m = (lastError.getMessage() == null) ? "" : lastError.getMessage();
        assertTrue(m.toLowerCase().contains(msg.toLowerCase()),
            "Mensagem esperada conter: \"" + msg + "\" mas foi: \"" + m + "\"");
    }

    @Then("o sistema confirma a alteração da \"disciplina\" mantendo o mesmo id e versao")
    public void confirma_alteracao_mesmo_id() {
        assertNull(lastError, "Esperava sucesso na edição in-place: " + lastError);
        var d = repo.porId(currentDisciplinaId).orElseThrow();
        assertEquals(currentDisciplinaId, d.getId(), "ID não deve mudar em edição in-place");
    }

    @Then("o sistema confirma que a \"disciplina\" tem nome {string} e área {string}")
    public void confirma_estado_atual(String nome, String areaNome) {
        var d = repo.porId(currentDisciplinaId).orElseThrow();
        assertEquals(nome, d.getNome());
        assertEquals(areaNome, d.getArea().nome());
    }

    @Then("o sistema confirma a criação de nova versão da \"disciplina\"")
    public void confirma_criacao_nova_versao() {
        assertNull(lastError, "Esperava sucesso com versionamento: " + lastError);
        assertNotNull(novaVersaoIdCriada, "Nova versão não detectada no repositório");
    }

    @Then("a nova versão está ativa com versao original + 1 e idVersaoOrigem preenchido")
    public void nova_versao_ativa_e_origem_preenchida() {
        var original = repo.porId(currentDisciplinaId).orElseThrow();
        var nova = repo.porId(novaVersaoIdCriada).orElseThrow();
        assertEquals(original.getVersao() + 1, nova.getVersao(), "Versão não incrementou");
        assertTrue(nova.isAtivo(), "Nova versão deve nascer ativa");
        Integer origem = nova.getIdVersaoOrigem();
        assertNotNull(origem, "idVersaoOrigem deve estar preenchido");
        assertEquals(original.getId().value(), origem, "idVersaoOrigem deve apontar para a v1/origem");
    }

    @Then("a versão original permanece preservada")
    public void original_preservada() {
        var original = repo.porId(currentDisciplinaId).orElseThrow();
        assertEquals(currentNome, original.getNome(), "Original não deve ser renomeada");
        assertEquals(currentArea.nome(), original.getArea().nome(), "Área da original não deve mudar");
    }

    @Then("o sistema rejeita a alteração em disciplinas")
    public void rejeita_alteracao() { assertNotNull(lastError, "Esperava erro na edição"); }

    @Then("o sistema confirma a exclusão da \"disciplina\"")
    public void confirma_exclusao_disciplina() {
        assertNull(lastError, "Esperava sucesso na exclusão: " + lastError);
        var d = repo.porId(currentDisciplinaId);
        assertTrue(d.isEmpty(), "Disciplina ainda presente após exclusão");
    }

    @Then("o sistema rejeita a exclusão em disciplinas")
    public void rejeita_exclusao() { assertNotNull(lastError, "Esperava erro na exclusão"); }
}
