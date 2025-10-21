package dev.com.qnota.dominio.principal;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;

import dev.com.qnota.infraestrutura.persistencia.memoria.RepositorioEmMemoria;

import dev.com.qnota.dominio.principal.aluno.Aluno;
import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.aluno.AlunoServico;

import dev.com.qnota.dominio.principal.responsavel.Responsavel;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelServico;

import dev.com.qnota.dominio.principal.turma.Turma;
import dev.com.qnota.dominio.principal.turma.TurmaId;

import dev.com.qnota.dominio.principal.simulado.Simulado;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;

import dev.com.qnota.dominio.principal.nota.Nota;
import dev.com.qnota.dominio.principal.nota.NotaId;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;

// novo import para professor
import dev.com.qnota.dominio.principal.professor.ProfessorId;

public class GerenciarAlunosFeature {

    // ===== estado por cenário =====
    private RepositorioEmMemoria repo;
    private AlunoServico alunoSrv;
    private ResponsavelServico responsavelSrv;

    private AtomicInteger seq;
    private Map<String, TurmaId> aliasTurma;
    private Map<String, Integer> anoTurma;
    private Map<String, ResponsavelId> aliasResp;

    private AlunoId currentAlunoId;
    private String currentNome;
    private LocalDate currentNascimento;
    private TurmaId currentTurmaId;
    private TurmaId currentTurmaDestinoId;

    // guarda últimos dados usados no cadastro para validar persistência
    private List<ResponsavelId> lastRespList;
    private ResponsavelId lastPrincipal;

    private Exception lastError;

    @Before
    public void reset() {
        repo = new RepositorioEmMemoria();
        alunoSrv = new AlunoServico(repo, repo, repo);
        responsavelSrv = new ResponsavelServico(repo, repo); // usaremos serviço para cadastrar responsáveis-fixture

        seq = new AtomicInteger(1);
        aliasTurma = new HashMap<>();
        anoTurma = new HashMap<>();
        aliasResp = new HashMap<>();

        currentAlunoId = null;
        currentNome = null;
        currentNascimento = null;
        currentTurmaId = null;
        currentTurmaDestinoId = null;

        lastRespList = null;
        lastPrincipal = null;

        lastError = null;
    }

    // ===== utils =====
    private ResponsavelId newRespId() { return new ResponsavelId(seq.getAndIncrement()); }
    private SimuladoId newSimId() { return new SimuladoId(seq.getAndIncrement()); }
    private NotaId newNotaId() { return new NotaId(seq.getAndIncrement()); }
    private ProfessorId newProfessorId() { return new ProfessorId(seq.getAndIncrement()); }

    // gera CPF válido (11 dígitos com DV)
    private String generateCpf(int seed) {
        int[] n = new int[11];
        int s = Math.abs(seed) + 12345;
        for (int i = 0; i < 9; i++) { s = (s * 1103515245 + 12345); n[i] = Math.floorMod(s, 10); }
        int soma = 0, peso = 10;
        for (int i = 0; i < 9; i++) soma += n[i] * (peso--);
        int r = 11 - (soma % 11);
        n[9] = (r >= 10) ? 0 : r;
        soma = 0; peso = 11;
        for (int i = 0; i < 10; i++) soma += n[i] * (peso--);
        r = 11 - (soma % 11);
        n[10] = (r >= 10) ? 0 : r;
        StringBuilder sb = new StringBuilder(11);
        for (int i = 0; i < 11; i++) sb.append(n[i]);
        return sb.toString();
    }

    private TurmaId ensureTurma(String alias, int ano) {
        return aliasTurma.computeIfAbsent(alias, a -> {
            // Como o ID é gerado pelo repositório, criar a turma e obter o ID gerado
            var turma = new Turma(alias, ano, true, newProfessorId());
            repo.salvar(turma);
            return turma.getId();
        });
    }

    private TurmaId ensureTurmaDefault(String alias) { return ensureTurma(alias, 2025); }

    private ResponsavelId ensureResp(String alias, boolean principal, String grau) {
        return aliasResp.computeIfAbsent(alias, a -> {
            var cpf = generateCpf(seq.getAndIncrement()); // CPF válido
            // cadastro de responsável via SERVIÇO (ORM gera id)
            return responsavelSrv.cadastrar(a + " Nome", cpf, a.toLowerCase()+"@ex.com");
        });
    }

    private List<ResponsavelId> buildResponsaveisValidos(int qtd, boolean marcarUmPrincipal) {
        List<ResponsavelId> lista = new ArrayList<>();
        for (int i = 1; i <= qtd; i++) {
            String alias = "R" + (aliasResp.size() + i);
            var rid = ensureResp(alias, false, "Parente");
            lista.add(rid);
        }
        return lista;
    }

    private ResponsavelId getPrincipalFromList(List<ResponsavelId> lista, boolean marcarUmPrincipal) {
        if (lista.isEmpty() || !marcarUmPrincipal) return null;
        return lista.get(0);
    }

    private List<ResponsavelId> buildResponsaveisDuplicados(String alias) {
        var rid = ensureResp(alias, false, "Parente");
        return List.of(rid, rid);
    }

    private AlunoId persistAlunoBasico(String nome, LocalDate nasc, TurmaId turma, List<ResponsavelId> r) {
        var principal = getPrincipalFromList(r, true);
        lastRespList = r;
        lastPrincipal = principal;
        return alunoSrv.cadastrar(nome, nasc, turma, r, principal); // via SERVIÇO
    }

    // helper para montar disciplinas de Simulado (RN-12 exige >= 2)
    private static Simulado.DisciplinaPeso dp(int id, double peso) {
        return new Simulado.DisciplinaPeso(new DisciplinaId(id), peso);
    }

    // ===== Givens (agora LITERAIS "aluno") =====

    @Given("um \"aluno\" com nome {string} e nascimento {string} {string} registrado na turma {string}")
    public void aluno_por_nome_data_estado_turma(String nome, String data, String estado, String turmaAlias) {
        currentNome = nome;
        currentNascimento = LocalDate.parse(data);
        currentTurmaId = ensureTurmaDefault(turmaAlias);
        if ("já está".equalsIgnoreCase(estado)) {
            currentAlunoId = persistAlunoBasico(currentNome, currentNascimento, currentTurmaId, buildResponsaveisValidos(2, true));
        } else {
            currentAlunoId = null;
        }
    }

    @Given("um \"aluno\" {string} registrado")
    public void aluno_estado_registrado(String estado) {
        if ("está".equalsIgnoreCase(estado)) {
            currentNome = "Aluno Teste";
            currentNascimento = LocalDate.of(2012, 1, 1);
            currentTurmaId = ensureTurmaDefault("7A");
            currentAlunoId = persistAlunoBasico(currentNome, currentNascimento, currentTurmaId, buildResponsaveisValidos(1, true));
        } else {
            currentAlunoId = null;
            currentNome = "Novo Aluno";
            currentNascimento = LocalDate.of(2012, 1, 1);
            currentTurmaId = ensureTurmaDefault("7A");
        }
    }

    @Given("um \"aluno\" da turma {string} {string} simulados finalizados")
    public void aluno_da_turma_possui_finalizados(String turmaOrigem, String possui) {
        currentTurmaId = ensureTurmaDefault(turmaOrigem);
        currentNome = "Aluno Teste";
        currentNascimento = LocalDate.of(2012, 1, 1);
        currentAlunoId = persistAlunoBasico(currentNome, currentNascimento, currentTurmaId, buildResponsaveisValidos(1, true));
        if ("possui".equalsIgnoreCase(possui)) {
            var s = new Simulado(
                LocalDate.now().minusDays(10),
                Simulado.Status.FINALIZADO,
                currentTurmaId,
                List.of(dp(1, 6.0), dp(2, 4.0))
            );
            repo.salvar(s);
        }
    }

    @Given("a turma de destino {string} pertence ao ano letivo {string} igual ao da turma atual")
    public void turma_destino_ano_igual(String destinoAlias, String ano) {
        int origemAno = 2025;
        anoTurma.putIfAbsent("7A", origemAno);
        ensureTurma("7A", origemAno);
        currentTurmaDestinoId = ensureTurma(destinoAlias, origemAno);
    }

    @Given("a turma de destino {string} pertence ao ano letivo {string} diferente do atual")
    public void turma_destino_ano_diferente(String destinoAlias, String ano) {
        int origemAno = 2025;
        anoTurma.putIfAbsent("7A", origemAno);
        ensureTurma("7A", origemAno);
        currentTurmaDestinoId = ensureTurma(destinoAlias, Integer.parseInt(ano));
    }

    @Given("um \"aluno\" {string} registrado e {string} notas em simulados")
    public void aluno_registrado_possui_notas(String estado, String possui) {
        aluno_estado_registrado("está");
        if ("possui".equalsIgnoreCase(possui)) {
            var sim = new Simulado(
                LocalDate.now(),
                Simulado.Status.FINALIZADO,
                currentTurmaId,
                List.of(dp(1, 6.0), dp(2, 4.0))
            );
            repo.salvar(sim);
            repo.salvar(new Nota(currentAlunoId, sim.getId(), new DisciplinaId(1), 8.0, java.time.LocalDateTime.now()));
        }
    }

    @Given("um \"aluno\" {string} ativo e {string} notas pendentes em simulados em andamento")
    public void aluno_ativo_pendencias(String estado, String possuiPend) {
        aluno_estado_registrado("está");
        if ("possui".equalsIgnoreCase(possuiPend)) {
            var simEdicao = new Simulado(
                LocalDate.now(),
                Simulado.Status.EM_EDICAO,
                currentTurmaId,
                List.of(dp(1, 5.0), dp(2, 5.0))
            );
            repo.salvar(simEdicao);
        }
    }

    @Given("existem responsáveis {string} e {string} válidos")
    public void existem_responsaveis_validos(String r1, String r2) {
        ensureResp(r1, true, "Parente");
        ensureResp(r2, false, "Parente");
    }

    @Given("existe um responsável {string} válido com {string}")
    public void existe_responsavel_valido_com(String r1, String detalhe) {
        ensureResp(r1, false, "Parente");
    }

    @Given("um \"aluno\" {string} registrado e {string}")
    public void aluno_registrado_e_flag(String estado, String flag) {
        currentAlunoId = null;
        currentNome = switch (flag) {
            case "sem nome" -> null;
            case "nome em branco" -> "   ";
            default -> "Aluno Novo";
        };
        currentNascimento = "sem data de nascimento".equals(flag) ? null : LocalDate.of(2012,1,1);
        currentTurmaId = "sem turma".equals(flag) ? null : ensureTurmaDefault("7A");
    }

    // ===== Whens (agora via SERVIÇO do Aluno) =====

    @When("um coordenador cadastra o \"aluno\" na turma {string} com responsáveis válidos")
    public void coord_cadastra_aluno_com_responsaveis_validos(String turmaAlias) {
        lastError = null;
        try {
            currentTurmaId = ensureTurmaDefault(turmaAlias);
            if (currentNome == null) currentNome = "Aluno Novo";
            if (currentNascimento == null) currentNascimento = LocalDate.of(2012,1,1);
            var lista = buildResponsaveisValidos(2, true);
            var principal = getPrincipalFromList(lista, true);
            lastRespList = lista;
            lastPrincipal = principal;
            currentAlunoId = alunoSrv.cadastrar(currentNome, currentNascimento, currentTurmaId, lista, principal);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar o mesmo \"aluno\" novamente na turma {string}")
    public void coord_tenta_cadastrar_mesmo_aluno(String turmaAlias) {
        lastError = null;
        try {
            var lista = buildResponsaveisValidos(2, true);
            var principal = getPrincipalFromList(lista, true);
            alunoSrv.cadastrar(currentNome, currentNascimento, currentTurmaId, lista, principal);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador cadastra o \"aluno\" informando exatamente {int} responsáveis")
    public void coord_cadastra_com_qtd_responsaveis(Integer qtd) {
        lastError = null;
        try {
            currentTurmaId = ensureTurmaDefault("7A");
            currentNome = "Aluno " + seq.getAndIncrement();
            currentNascimento = LocalDate.of(2012, 1, 1);
            var lista = buildResponsaveisValidos(qtd, true);
            var principal = getPrincipalFromList(lista, true);
            lastRespList = lista;
            lastPrincipal = principal;
            currentAlunoId = alunoSrv.cadastrar(currentNome, currentNascimento, currentTurmaId, lista, principal);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar o \"aluno\" informando {int} responsáveis")
    public void coord_tenta_cadastrar_com_qtd_responsaveis(Integer qtd) {
        lastError = null;
        try {
            var lista = buildResponsaveisValidos(qtd, true);
            var principal = getPrincipalFromList(lista, true);
            alunoSrv.cadastrar("Aluno X", LocalDate.of(2012,1,1), ensureTurmaDefault("7A"), lista, principal);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador cadastra o \"aluno\" com responsáveis válidos e um deles marcado como {string}")
    public void coord_cadastra_com_um_principal(String principalTxt) {
        lastError = null;
        try {
            var lista = buildResponsaveisValidos(2, true);
            // define turma só se ainda não houver
            if (currentTurmaId == null) currentTurmaId = ensureTurmaDefault("7A");
            // garante defaults caso o Given não tenha preenchido
            if (currentNome == null) currentNome = "Novo Aluno";
            if (currentNascimento == null) currentNascimento = LocalDate.of(2012,1,1);

            var principal = getPrincipalFromList(lista, true);
            lastRespList = lista;
            lastPrincipal = principal;

            currentAlunoId = alunoSrv.cadastrar(currentNome, currentNascimento, currentTurmaId, lista, principal);
        } catch (Exception e) {
            lastError = e;
        }
    }


    @When("um coordenador tenta cadastrar o \"aluno\" sem nenhum responsável marcado como {string}")
    public void coord_tenta_cadastrar_sem_principal(String principalTxt) {
        lastError = null;
        try {
            var lista = buildResponsaveisValidos(2, false);
            var principal = getPrincipalFromList(lista, false); // null
            alunoSrv.cadastrar("Aluno Sem Principal", LocalDate.of(2012,1,1), ensureTurmaDefault("7A"), lista, principal);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador transfere o \"aluno\" da turma {string} para {string}")
    public void coord_transfere_aluno(String origemAlias, String destinoAlias) {
        lastError = null;
        try {
            if (currentTurmaDestinoId == null) currentTurmaDestinoId = ensureTurmaDefault(destinoAlias);
            alunoSrv.transferir(currentAlunoId, currentTurmaDestinoId);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta transferir o \"aluno\" para {string}")
    public void coord_tenta_transferir_para(String destinoAlias) {
        lastError = null;
        try {
            currentTurmaDestinoId = ensureTurmaDefault(destinoAlias);
            alunoSrv.transferir(currentAlunoId, currentTurmaDestinoId);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta transferir o \"aluno\" para a turma {string}")
    public void coord_tenta_transferir_para_turma(String destinoAlias) {
        coord_tenta_transferir_para(destinoAlias);
    }

    @When("um coordenador exclui o \"aluno\"")
    public void coord_exclui_aluno() {
        lastError = null;
        try { alunoSrv.excluir(currentAlunoId); } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta excluir o \"aluno\"")
    public void coord_tenta_excluir_aluno() { coord_exclui_aluno(); }

    @When("um coordenador inativa o \"aluno\"")
    public void coord_inativa_aluno() {
        lastError = null;
        try { alunoSrv.inativar(currentAlunoId); } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta inativar o \"aluno\"")
    public void coord_tenta_inativar_aluno() { coord_inativa_aluno(); }

    @When("um coordenador tenta cadastrar o \"aluno\" na turma {string} {string}")
    public void coord_tenta_cadastrar_sem_responsaveis(String turmaAlias, String detalhe) {
        lastError = null;
        try {
            var lista = List.<ResponsavelId>of();
            alunoSrv.cadastrar("Aluno Sem Resp", LocalDate.of(2012,1,1), ensureTurmaDefault(turmaAlias), lista, null);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar o \"aluno\" na turma {string} informando os responsáveis {string} e {string}")
    public void coord_tenta_cadastrar_com_respos_duplicados(String turmaAlias, String r1, String r2) {
        lastError = null;
        try {
            List<ResponsavelId> lista = r1.equals(r2) ? buildResponsaveisDuplicados(r1)
                : List.of(
                    ensureResp(r1, true, "Parente"),
                    ensureResp(r2, false, "Parente")
                  );
            var principal = getPrincipalFromList(lista, true);
            alunoSrv.cadastrar("Aluno Dup", LocalDate.of(2012,1,1), ensureTurmaDefault(turmaAlias), lista, principal);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar o \"aluno\" na turma {string} com dois responsáveis marcados como {string}")
    public void coord_tenta_cadastrar_dois_principais(String turmaAlias, String principalTxt) {
        lastError = null;
        try {
            var rA = ensureResp("R1", true, "Parente");
            var rB = ensureResp("R2", false, "Parente");
            var rC = ensureResp("R3", false, "Parente");
            var lista = List.of(rA, rB);
            alunoSrv.cadastrar("Aluno Dois Principais", LocalDate.of(2012,1,1), ensureTurmaDefault(turmaAlias), lista, rC);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar o \"aluno\" na turma {string} com responsáveis válidos")
    public void coord_tenta_cadastrar_com_responsaveis_validos(String turmaAlias) {
        lastError = null;
        try {
            var lista = buildResponsaveisValidos(2, true);
            var principal = getPrincipalFromList(lista, true);
            alunoSrv.cadastrar(currentNome, currentNascimento, currentTurmaId, lista, principal);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar o \"aluno\" na turma {string} contendo um responsável {string} na lista")
    public void coord_tenta_cadastrar_com_responsavel_nulo(String turmaAlias, String marcador) {
        lastError = null;
        try {
            List<ResponsavelId> lista = new ArrayList<>();
            lista.add(ensureResp("R1", true, "Parente"));
            lista.add(null); // item nulo proposital
            alunoSrv.cadastrar("Aluno Item Nulo", LocalDate.of(2012,1,1), ensureTurmaDefault(turmaAlias), lista, null);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar o \"aluno\" na turma {string} informando o responsável {string}")
    public void coord_tenta_cadastrar_com_grau_vazio(String turmaAlias, String r1) {
        lastError = null;
        try {
            var rid = ensureResp(r1, false, "Parente");
            var lista = List.of(rid); // sem grau parentesco
            alunoSrv.cadastrar("Aluno Grau Vazio", LocalDate.of(2012,1,1), ensureTurmaDefault(turmaAlias), lista, rid);
        } catch (Exception e) { lastError = e; }
    }

    // ===== Thens (sempre verificando via REPOSITÓRIO) =====

    @Then("o sistema confirma o cadastro do \"aluno\"")
    public void confirma_cadastro_aluno() {
        assertNull(lastError, "Esperava sucesso, mas houve erro: " + (lastError == null ? "" : lastError.getMessage()));
        var salvo = repo.porId(currentAlunoId).orElseThrow(() -> new AssertionError("Aluno não persistido"));
        assertEquals(currentNome, salvo.getNome());
        assertEquals(currentTurmaId, salvo.getTurma());
        assertFalse(salvo.getResponsaveis().isEmpty(), "Aluno persistiu sem responsáveis");
        if (lastRespList != null) {
            assertTrue(salvo.getResponsaveis().containsAll(lastRespList), "Lista de responsáveis não persistiu corretamente");
        }
        if (lastPrincipal != null) {
            assertEquals(lastPrincipal, salvo.getResponsavelPrincipal(), "Principal não persistiu corretamente");
        }
    }

    @Then("o sistema rejeita o cadastro em alunos")
    public void rejeita_cadastro() { assertNotNull(lastError, "Esperava erro no cadastro"); }

    @Then("o sistema informa em alunos que {string}")
    public void o_sistema_informa_em_alunos_que(String msg) {
        assertNotNull(lastError, "Não houve erro para verificar mensagem");
        String m = (lastError.getMessage() == null) ? "" : lastError.getMessage();
        assertTrue(m.toLowerCase().contains(msg.toLowerCase()),
            "Mensagem esperada conter: \"" + msg + "\" mas foi: \"" + m + "\"");
    }

    @Then("o sistema confirma a transferência do \"aluno\"")
    public void confirma_transferencia() {
        assertNull(lastError, "Esperava sucesso na transferência: " + (lastError == null ? "" : lastError.getMessage()));
        var salvo = repo.porId(currentAlunoId).orElseThrow();
        assertEquals(currentTurmaDestinoId, salvo.getTurma(), "Turma não foi atualizada na persistência");
    }

    @Then("o sistema rejeita a transferência em alunos")
    public void rejeita_transferencia() { assertNotNull(lastError, "Esperava erro na transferência"); }

    @Then("o sistema confirma a exclusão do \"aluno\"")
    public void confirma_exclusao() {
        assertNull(lastError, "Esperava sucesso na exclusão: " + (lastError == null ? "" : lastError.getMessage()));
        assertTrue(repo.porId(currentAlunoId).isEmpty(), "Aluno ainda existe no repositório após exclusão");
    }

    @Then("o sistema rejeita a exclusão em alunos")
    public void rejeita_exclusao() { assertNotNull(lastError, "Esperava erro na exclusão"); }

    @Then("o sistema confirma a inativação do \"aluno\"")
    public void confirma_inativacao() {
        assertNull(lastError, "Esperava sucesso na inativação: " + (lastError == null ? "" : lastError.getMessage()));
        var salvo = repo.porId(currentAlunoId).orElseThrow();
        assertFalse(salvo.isAtivo(), "Aluno continuou ativo após inativação");
    }

    @Then("o sistema rejeita a inativação em alunos")
    public void rejeita_inativacao() { assertNotNull(lastError, "Esperava erro na inativação"); }
}
