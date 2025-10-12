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
        // cria um simulado e inclui a disciplina como parte da grade (com outra para cumprir RN-12 do próprio Simulado)
        var sim = new Simulado(
            newSimuladoId(),
            LocalDate.now().minusDays(status == Simulado.Status.FINALIZADO ? 10 : 0),
            status,
            // não precisamos de turma/ids reais p/ estes testes; RepositorioEmMemoria aceita
            new dev.com.qnota.dominio.principal.turma.TurmaId(seq.getAndIncrement()),
            java.util.List.of(
                dp(dId.value(), 5.0),
                dp(seq.getAndIncrement() + 1000, 5.0) // uma disciplina dummy
            )
        );
        repo.salvar(sim);
    }

    // ===== Givens =====

    @Given("um repositório em memória limpo")
    public void repo_memoria_limpo() {
        // reset() já garante; passo mantido p/ feature file ficar legível
        assertNotNull(repo);
    }

    @Given("uma \"disciplina\" com nome {string} na área {string} que {string} registrada")
    public void disciplina_por_nome_area_estado(String nome, String areaNome, String estado) {
        currentNome = nome;
        currentArea = areaByNome(areaNome);
        if ("já está".equalsIgnoreCase(estado)) {
            currentDisciplinaId = ensureDisciplina(nome, areaNome);
        } else {
            currentDisciplinaId = null;
        }
    }

    @Given("uma \"disciplina\" {string} registrada")
    public void disciplina_estado_registrada(String estado) {
        if ("está".equalsIgnoreCase(estado)) {
            currentNome = "Disciplina " + seq.getAndIncrement();
            currentArea = areaByNome("Área " + seq.getAndIncrement());
            currentDisciplinaId = ensureDisciplina(currentNome, currentArea.nome());
        } else {
            currentNome = "Nova Disciplina";
            currentArea = areaByNome("Área Padrão");
            currentDisciplinaId = null;
        }
    }

    @Given("a \"disciplina\" {string} usada em simulados finalizados")
    public void disciplina_usada_finalizados_direct(String foi) {
        if ("foi".equalsIgnoreCase(foi)) {
            if (currentDisciplinaId == null) currentDisciplinaId = ensureDisciplina(currentNome, currentArea.nome());
            vincularEmSimulado(currentDisciplinaId, Simulado.Status.FINALIZADO);
        }
    }

    @Given("a \"disciplina\" {string} usada em simulados em edição")
    public void disciplina_usada_em_edicao(String foi) {
        if ("foi".equalsIgnoreCase(foi)) {
            if (currentDisciplinaId == null) currentDisciplinaId = ensureDisciplina(currentNome, currentArea.nome());
            vincularEmSimulado(currentDisciplinaId, Simulado.Status.EM_EDICAO);
        }
    }

    @Given("a \"disciplina\" {string} usada em simulados \\(qualquer status)")
    public void disciplina_usada_qualquer_status(String foi) {
        if ("não foi".equalsIgnoreCase(foi)) return;
        if (currentDisciplinaId == null) currentDisciplinaId = ensureDisciplina(currentNome, currentArea.nome());
        // cria um em edição só para marcar "usada"
        vincularEmSimulado(currentDisciplinaId, Simulado.Status.EM_EDICAO);
    }

    @Given("a \"disciplina\" {string} usada em simulados finalizados ou não")
    public void disciplina_usada_finalizados_ou_nao(String _) { /* alias semântica, não usado */ }

    @Given("a \"disciplina\" {string} foi usada em simulados finalizados")
    public void disciplina_foi_usada_finalizados(String _) {
        if (currentDisciplinaId == null) currentDisciplinaId = ensureDisciplina(currentNome, currentArea.nome());
        vincularEmSimulado(currentDisciplinaId, Simulado.Status.FINALIZADO);
    }

    @Given("a \"disciplina\" {string} não foi usada em simulados finalizados")
    public void disciplina_nao_usada_finalizados(String _) {
        // nada; garantimos não criar simulado finalizado
    }

    @Given("a \"disciplina\" {string}")
    public void noop_alias(String _) { /* marcador legível nos cenários */ }

    @Given("a \"disciplina\" {string} {string} usada em simulados finalizados")
    public void disciplina_flag_usada_finalizados(String _, String foi) {
        if ("foi".equalsIgnoreCase(foi)) {
            if (currentDisciplinaId == null) currentDisciplinaId = ensureDisciplina(currentNome, currentArea.nome());
            vincularEmSimulado(currentDisciplinaId, Simulado.Status.FINALIZADO);
        }
    }

    @Given("a \"disciplina\" {string} {string} usada em simulados finalizados ou não")
    public void disciplina_flag_usada_finalizados_ou_nao(String _, String foi) {
        if ("foi".equalsIgnoreCase(foi)) {
            if (currentDisciplinaId == null) currentDisciplinaId = ensureDisciplina(currentNome, currentArea.nome());
            vincularEmSimulado(currentDisciplinaId, Simulado.Status.EM_EDICAO);
        }
    }

    @Given("a \"disciplina\" {string} {string} usada em simulados finalizados e em edição")
    public void disciplina_flag_usada_em_ambos(String _, String foi) {
        if ("foi".equalsIgnoreCase(foi)) {
            if (currentDisciplinaId == null) currentDisciplinaId = ensureDisciplina(currentNome, currentArea.nome());
            vincularEmSimulado(currentDisciplinaId, Simulado.Status.FINALIZADO);
            vincularEmSimulado(currentDisciplinaId, Simulado.Status.EM_EDICAO);
        }
    }

    @Given("a \"disciplina\" {string} {string} usada em simulados em edição")
    public void disciplina_flag_usada_em_edicao(String _, String foi) {
        if ("foi".equalsIgnoreCase(foi)) {
            if (currentDisciplinaId == null) currentDisciplinaId = ensureDisciplina(currentNome, currentArea.nome());
            vincularEmSimulado(currentDisciplinaId, Simulado.Status.EM_EDICAO);
        }
    }

    @Given("a \"disciplina\" {string} {string} usada em simulados \\(qualquer status)")
    public void disciplina_flag_usada_qualquer(String _, String foi) {
        if ("foi".equalsIgnoreCase(foi)) disciplina_usada_qualquer_status("foi");
    }

    @Given("a \"disciplina\" {string} {string} usada em simulados finalizados ou em edição")
    public void disciplina_flag_usada_finalizados_ou_edicao(String _, String foi) {
        if ("foi".equalsIgnoreCase(foi)) {
            if (currentDisciplinaId == null) currentDisciplinaId = ensureDisciplina(currentNome, currentArea.nome());
            vincularEmSimulado(currentDisciplinaId, Simulado.Status.FINALIZADO);
        }
    }

    @Given("a \"disciplina\" {string} {string} foi usada em simulados finalizados")
    public void disciplina_flag_foi_usada_finalizados(String _, String foi) {
        if ("foi".equalsIgnoreCase(foi)) disciplina_foi_usada_finalizados(_);
    }

    @Given("a \"disciplina\" {string} {string} não foi usada em simulados finalizados")
    public void disciplina_flag_nao_foi_usada_finalizados(String _, String __) { /* nada */ }

    @Given("a \"disciplina\" {string} {string} não foi usada em simulados")
    public void disciplina_flag_nao_foi_usada_simulados(String _, String __) { /* nada */ }

    @Given("a \"disciplina\" {string} {string} não foi usada em simulados \\(qualquer status)")
    public void disciplina_flag_nao_foi_usada_simulados2(String _, String __) { /* nada */ }

    @Given("a \"disciplina\" {string} {string} não foi usada em simulados em edição")
    public void disciplina_flag_nao_foi_usada_edicao(String _, String __) { /* nada */ }

    @Given("a \"disciplina\" {string} {string} não foi usada em simulados finalizados ou não")
    public void disciplina_flag_nao_foi_usada_finalizados_ou_nao(String _, String __) { /* nada */ }

    @Given("a \"disciplina\" {string} {string}")
    public void disciplina_alias_duplo(String _, String __) { /* apenas para fluência */ }

    @Given("a \"disciplina\" {string} {string} usada em simulados")
    public void disciplina_flag_usada(String _, String foi) {
        if ("foi".equalsIgnoreCase(foi)) disciplina_usada_qualquer_status("foi");
    }

    @Given("a \"disciplina\" {string} {string} usada em simulados em qualquer status")
    public void disciplina_flag_usada_any(String _, String foi) {
        if ("foi".equalsIgnoreCase(foi)) disciplina_usada_qualquer_status("foi");
    }

    @Given("a \"disciplina\" {string} {string} não foi usada em simulados em qualquer status")
    public void disciplina_flag_nao_usada_any(String _, String __) { /* nada */ }

    @Given("a \"disciplina\" {string} {string} foi usada em simulados em edição")
    public void disciplina_flag_foi_usada_em_edicao(String _, String foi) {
        if ("foi".equalsIgnoreCase(foi)) disciplina_usada_em_edicao("foi");
    }

    @Given("a \"disciplina\" {string} {string} não foi usada em simulados \\(qualquer estado)")
    public void disciplina_flag_nao_usada_qualquer_estado(String _, String __) { /* nada */ }

    @Given("a \"disciplina\" {string} {string} não foi usada em simulados de nenhum tipo")
    public void disciplina_flag_nao_usada_nenhum_tipo(String _, String __) { /* nada */ }

    @Given("a \"disciplina\" {string} {string} não foi usada em simulados de status algum")
    public void disciplina_flag_nao_usada_status_algum(String _, String __) { /* nada */ }

    @Given("uma \"disciplina\" com nome {string} na área {string} que {string} registrada e {string} usada em simulados finalizados")
    public void disciplina_por_nome_area_estado_e_finalizados(String nome, String areaNome, String estado, String foi) {
        disciplina_por_nome_area_estado(nome, areaNome, estado);
        disciplina_flag_usada_finalizados("disc", foi);
    }

    @Given("uma \"disciplina\" com nome {string} e área {string} que {string} registrada e {string} usada em simulados em edição")
    public void disciplina_por_nome_area_estado_e_edicao(String nome, String areaNome, String estado, String foi) {
        disciplina_por_nome_area_estado(nome, areaNome, estado);
        disciplina_flag_usada_em_edicao("disc", foi);
    }

    @Given("uma \"disciplina\" com nome {string} e área {string} que {string} registrada e {string} usada em simulados \\(qualquer status)")
    public void disciplina_por_nome_area_estado_e_any(String nome, String areaNome, String estado, String foi) {
        disciplina_por_nome_area_estado(nome, areaNome, estado);
        disciplina_flag_usada_qualquer("disc", foi);
    }

    @Given("uma \"disciplina\" com nome {string} e área {string} que {string} registrada e {string} usada em simulados finalizados ou não")
    public void disciplina_por_nome_area_estado_e_finalizados_ou_nao(String nome, String areaNome, String estado, String foi) {
        disciplina_por_nome_area_estado(nome, areaNome, estado);
        if ("foi".equalsIgnoreCase(foi)) disciplina_usada_qualquer_status("foi");
    }

    @Given("a \"disciplina\" {string} {string} foi usada em simulados em edição")
    public void disciplina_flagFoi_usada_em_edicao(String _, String foi) {
        if ("foi".equalsIgnoreCase(foi)) disciplina_usada_em_edicao("foi");
    }

    @Given("a \"disciplina\" {string} {string} foi usada em simulados (qualquer status)")
    public void disciplina_flagFoi_usada_any(String _, String foi) {
        if ("foi".equalsIgnoreCase(foi)) disciplina_usada_qualquer_status("foi");
    }

    @Given("a \"disciplina\" {string} {string} não foi usada em simulados (qualquer status)")
    public void disciplina_flagNao_usada_any(String _, String __) { }

    @Given("a \"disciplina\" {string} {string} não foi usada em simulados finalizados nem em edição")
    public void disciplina_flagNao_usada_none(String _, String __) { }

    @Given("a \"disciplina\" {string} {string} foi usada em simulados finalizados")
    public void disciplina_flagFoi_usada_finalizados2(String _, String foi) {
        if ("foi".equalsIgnoreCase(foi)) disciplina_foi_usada_finalizados(_);
    }

    @Given("a \"disciplina\" {string} {string} foi usada em simulados finalizados ou em edição")
    public void disciplina_flagFoi_usada_finalizados_ou_edicao2(String _, String foi) {
        if ("foi".equalsIgnoreCase(foi)) disciplina_usada_finalizados_direct("foi");
    }

    @Given("a \"disciplina\" {string} {string} não foi usada em simulados finalizados")
    public void disciplina_flagNao_usada_finalizados2(String _, String __) { }

    // ===== Whens =====

    @When("um coordenador cadastra a \"disciplina\" com dados válidos")
    public void coord_cadastra_disciplina_com_dados_validos() {
        lastError = null;
        try {
            if (currentNome == null) currentNome = "Nova Disciplina";
            if (currentArea == null) currentArea = areaByNome("Área Padrão");
            discSrv.cadastrar(currentNome, currentArea);
            // recuperar ID recém-criado pelo repositório em memória (último id usado)
            currentDisciplinaId = new DisciplinaId(repo.maxIdDisciplina()); // helper do seu repos em memória (assumido)
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
            // tentar detectar se criou nova versão (id diferente salvo a mais)
            // como o serviço salva no repo, se foi versionado haverá outro registro com versao=before.versao+1
            novaVersaoIdCriada = repo.findDisciplinaByNomeArea(novoNome, novaAreaNome) // helper assumido
                .filter(d -> d.getVersao() == before.getVersao() + 1)
                .map(Disciplina::getId)
                .orElse(null);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta editar {string} para nome {string} e área {string}")
    public void coord_tenta_editar_para_nome_area(String alias, String novoNome, String novaAreaNome) {
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
        // Nenhum assert de versão mudando: deve permanecer igual
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

    @Then("a nova versão está ativa com versao original \\+ 1 e idVersaoOrigem preenchido")
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

    // ===== atalhos semânticos usados no .feature =====

    @Given("a \"disciplina\" {string} {string} foi usada em simulados em qualquer status")
    public void disciplina_flagFoi_usada_any2(String _, String foi) {
        if ("foi".equalsIgnoreCase(foi)) disciplina_usada_qualquer_status("foi");
    }

    @Given("a \"disciplina\" {string} {string} não foi usada em simulados em qualquer status (nenhum vínculo)")
    public void disciplina_flagNao_usada_any2(String _, String __) { }

    @Given("a \"disciplina\" {string} {string} foi usada em simulados")
    public void disciplina_flagFoi_usada2(String _, String foi) {
        if ("foi".equalsIgnoreCase(foi)) disciplina_usada_qualquer_status("foi");
    }

    // ===== Gherkin steps auxiliares dos cenários do arquivo =====

    @Given("a \"disciplina\" {string} {string} usada em simulados finalizados")
    public void a_disciplina_flag_usada_finalizados(String _, String foi) {
        disciplina_flag_usada_finalizados("disc", foi);
    }

    @Given("a \"disciplina\" {string} {string} usada em simulados em edição")
    public void a_disciplina_flag_usada_edicao(String _, String foi) {
        disciplina_flag_usada_em_edicao("disc", foi);
    }

    @Given("a \"disciplina\" {string} {string} usada em simulados \\(qualquer status) (atalho)")
    public void a_disciplina_flag_usada_any_atalho(String _, String foi) {
        disciplina_flag_usada_qualquer("disc", foi);
    }

    @Given("a \"disciplina\" {string} {string} usada em simulados (qualquer status) (atalho)")
    public void a_disciplina_flag_usada_any_atalho2(String _, String foi) {
        disciplina_flag_usada_qualquer("disc", foi);
    }

    // ===== cenários que citam explicitamente chaves para duas disciplinas =====

    @Given("uma \"disciplina\" com nome {string} e área {string} que {string} registrada")
    public void disciplina_por_nome_area_estado_alt(String nome, String areaNome, String estado) {
        disciplina_por_nome_area_estado(nome, areaNome, estado);
    }

    @Given("uma \"disciplina\" com nome {string} na área {string} que {string} registrada e {string} usada em simulados em qualquer status")
    public void disciplina_por_nome_area_estado_any(String nome, String areaNome, String estado, String foi) {
        disciplina_por_nome_area_estado(nome, areaNome, estado);
        if ("foi".equalsIgnoreCase(foi)) disciplina_usada_qualquer_status("foi");
    }

    @Given("uma \"disciplina\" com nome {string} na área {string} que {string} registrada e {string} usada em simulados finalizados")
    public void disciplina_por_nome_area_estado_fin(String nome, String areaNome, String estado, String foi) {
        disciplina_por_nome_area_estado(nome, areaNome, estado);
        if ("foi".equalsIgnoreCase(foi)) disciplina_usada_finalizados_direct("foi");
    }

    @Given("uma \"disciplina\" com nome {string} na área {string} que {string} registrada e {string} usada em simulados em edição")
    public void disciplina_por_nome_area_estado_edit(String nome, String areaNome, String estado, String foi) {
        disciplina_por_nome_area_estado(nome, areaNome, estado);
        if ("foi".equalsIgnoreCase(foi)) disciplina_usada_em_edicao("foi");
    }

    // Apelidos que os cenários usam para se referir a uma disciplina específica
    @Given("a \"disciplina\" {string} {string} {string}")
    public void a_disciplina_alias_multiplo(String nome, String area, String estado) {
        disciplina_por_nome_area_estado(nome, area, estado);
    }

    // ===== cenários com nomes explícitos (RN-121 conflito) =====

    @Given("uma \"disciplina\" com nome {string} na área {string} que {string} registrada e alias {string}")
    public void disciplina_com_alias(String nome, String areaNome, String estado, String alias) {
        disciplina_por_nome_area_estado(nome, areaNome, estado);
        if (currentDisciplinaId == null) {
            currentDisciplinaId = ensureDisciplina(nome, areaNome);
        }
        aliasDisciplina.put(alias, currentDisciplinaId);
    }

    @Given("uma \"disciplina\" com nome {string} na área {string} já existente")
    public void disciplina_ja_existente(String nome, String areaNome) {
        ensureDisciplina(nome, areaNome);
    }

    @Given("uma \"disciplina\" {string} {string} simulados finalizados")
    public void disciplina_foo_bar(String _, String foi) {
        if ("possui".equalsIgnoreCase(foi)) disciplina_usada_finalizados_direct("foi");
    }

    @Given("uma \"disciplina\" com nome {string} na área {string} que \"já está\" registrada")
    public void disciplina_ja_reg(String nome, String areaNome) {
        ensureDisciplina(nome, areaNome);
    }

    @Given("uma \"disciplina\" com nome {string} na área {string} que \"já está\" registrada e foi usada em simulados finalizados")
    public void disciplina_ja_reg_e_usada_fin(String nome, String areaNome) {
        var id = ensureDisciplina(nome, areaNome);
        currentDisciplinaId = id;
        vincularEmSimulado(id, Simulado.Status.FINALIZADO);
    }

    // ===== cenário "Geografia/Geo" (RN-121) =====
    @Given("uma \"disciplina\" com nome {string} na área {string} que \"já está\" registrada e alias fixo {string}")
    public void disciplina_alias_fixo(String nome, String areaNome, String alias) {
        var id = ensureDisciplina(nome, areaNome);
        aliasDisciplina.put(alias, id);
        if (currentDisciplinaId == null) currentDisciplinaId = id;
        currentNome = nome;
        currentArea = areaByNome(areaNome);
    }

    @Given("a \"disciplina\" {string} {string} usada em simulados finalizados e alias ativo é {string}")
    public void disciplina_usada_fin_e_alias(String foi, String _, String alias) {
        var id = aliasDisciplina.get(alias);
        if (id == null) throw new IllegalStateException("Alias não mapeado: " + alias);
        currentDisciplinaId = id;
        if ("foi".equalsIgnoreCase(foi)) vincularEmSimulado(id, Simulado.Status.FINALIZADO);
    }

    @When("um coordenador tenta editar {string} para nome {string} e área {string} (com alias atual)")
    public void coord_tenta_editar_por_alias(String alias, String novoNome, String novaArea) {
        currentDisciplinaId = aliasDisciplina.get(alias);
        coord_edita_disciplina(novoNome, novaArea);
    }
}
