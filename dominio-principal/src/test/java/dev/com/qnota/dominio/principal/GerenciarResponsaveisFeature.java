package dev.com.qnota.dominio.principal;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import dev.com.qnota.infraestrutura.persistencia.memoria.RepositorioEmMemoria;
import dev.com.qnota.dominio.principal.aluno.Aluno;
import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.responsavel.Responsavel;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;
import dev.com.qnota.dominio.principal.turma.TurmaId;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelServico;

public class GerenciarResponsaveisFeature {

    private RepositorioEmMemoria repo;
    private ResponsavelServico servico;

    private AtomicInteger seq;
    private Map<String, ResponsavelId> aliasResp;
    private Map<String, AlunoId> aliasAluno;
    private Map<String, String> cpfByAlias;

    private String currentCpf;
    private String lastNome;
    private String lastEmail;

    private ResponsavelId currentRespId;
    private AlunoId currentAlunoId;
    private ResponsavelId lastVinculoRespId; // p/ validar vínculo por leitura do repo

    private Exception lastError;

    @Before
    public void setup() {
        repo = new RepositorioEmMemoria();
        servico = new ResponsavelServico(repo, repo); // (responsavelRepo, alunoRepo)

        seq        = new AtomicInteger(1);
        aliasResp  = new HashMap<>();
        aliasAluno = new HashMap<>();
        cpfByAlias = new HashMap<>();

        currentCpf     = null;
        lastNome       = null;
        lastEmail      = null;

        currentRespId  = null;
        currentAlunoId = null;
        lastVinculoRespId = null;

        lastError      = null;
    }

    // ===== utils =====
    private AlunoId newAlunoId() { return new AlunoId(seq.getAndIncrement()); }
    private TurmaId defaultTurma() { return new TurmaId(1); }
    private static String normCpf(String s){ return s==null? null : s.replaceAll("\\D",""); }

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

    private String cpfForAlias(String alias) {
        return cpfByAlias.computeIfAbsent(alias, a -> generateCpf(a.hashCode()));
    }

    private Responsavel ensureResponsavelByCpf(String cpf, String nome, String email, Responsavel.Status status) {
        // reaproveita se já existir na memória
        for (var e : aliasResp.entrySet()) {
            var rOpt = repo.porId(e.getValue());
            if (rOpt.isPresent() && normCpf(rOpt.get().getCpf()).equals(normCpf(cpf))) {
                return rOpt.get();
            }
        }
        // cria diretamente (fixture) e pega o ID gerado pelo repo
        var r = new Responsavel(nome, cpf, email, status);
        repo.salvar(r);
        String alias = "R" + r.getId().value();
        aliasResp.put(alias, r.getId());
        cpfByAlias.put(alias, cpf);
        return r;
    }

    private ResponsavelId ensureRespAlias(String alias, String nome, String email, Responsavel.Status status) {
        return aliasResp.computeIfAbsent(alias, a -> {
            String cpf = cpfForAlias(a);
            var r = new Responsavel(nome, cpf, email, status);
            repo.salvar(r);
            return r.getId();
        });
    }

    /** Evita CME: não usar computeIfAbsent aninhado */
    private AlunoId ensureAlunoComVinculos(String alunoAlias, String nomeAluno,
                                           String r1Alias, boolean r1Principal,
                                           String r2Alias, boolean r2Principal) {
        AlunoId aId = aliasAluno.get(alunoAlias);
        if (aId != null) {
            return aId;
        }
        var r1Id = ensureRespAlias(r1Alias, r1Alias + " Nome",
                r1Alias.toLowerCase()+"@ex.com", Responsavel.Status.ATIVO);
        var r2Id = (r2Alias != null)
                ? ensureRespAlias(r2Alias, r2Alias + " Nome", r2Alias.toLowerCase()+"@ex.com", Responsavel.Status.ATIVO)
                : null;

        var lista = new java.util.ArrayList<ResponsavelId>();
        lista.add(r1Id);
        if (r2Id != null) lista.add(r2Id);

        var aluno = new Aluno(nomeAluno, LocalDate.of(2012,1,1), true, defaultTurma(), lista, r1Id);
        repo.salvar(aluno);
        aId = aluno.getId();
        aliasAluno.put(alunoAlias, aId);
        return aId;
    }

    // ===================== Givens =====================

    @Given("um \"responsável\" com CPF {string} {string} registrado")
    public void um_responsavel_com_cpf_registrado(String cpf, String estado) {
        lastError = null;
        currentCpf = cpf;
        if ("não está".equals(estado)) {
            aliasResp.values().forEach(id -> repo.porId(id).ifPresent(r -> {
                if (normCpf(r.getCpf()).equals(normCpf(cpf))) repo.excluir(id);
            }));
            currentRespId = null;
        } else {
            var r = ensureResponsavelByCpf(cpf, "Nome Padrão", "email@exemplo.com", Responsavel.Status.ATIVO);
            currentRespId = r.getId();
        }
    }

    @Given("um \"responsável\" {string} registrado")
    public void um_responsavel_sem_cpf_registrado(String estado) {
        lastError = null;
        String cpf = generateCpf(seq.getAndIncrement());
        currentCpf = cpf;
        if ("não está".equals(estado)) {
            currentRespId = null;
        } else {
            var r = ensureResponsavelByCpf(cpf, "Nome Padrão", "email@exemplo.com", Responsavel.Status.ATIVO);
            currentRespId = r.getId();
        }
    }

    @Given("um \"responsável\" {string} registrado e {string}")
    public void um_responsavel_registrado_e(String estado, String flag) {
        lastError = null;
        currentRespId = null;
        if ("sem CPF".equalsIgnoreCase(flag)) currentCpf = "";
    }

    @Given("não existe \"responsável\" com CPF {string}")
    public void nao_existe_responsavel_com_cpf(String cpf) {
        lastError = null;
        currentCpf = cpf;
        aliasResp.values().forEach(id -> repo.porId(id).ifPresent(r -> {
            if (normCpf(r.getCpf()).equals(normCpf(cpf))) repo.excluir(id);
        }));
        currentRespId = null;
    }

    @Given("um \"responsável\" {string} marcado como {string}")
    public void um_responsavel_marcado_como(String verboEstado, String status) {
        lastError = null;
        var cpf = generateCpf(seq.getAndIncrement());
        var r = new Responsavel("Resp", cpf, "resp@ex.com", Responsavel.Status.ATIVO);
        if ("inadimplente".equalsIgnoreCase(status)) r.marcarInadimplente();
        if ("inativo".equalsIgnoreCase(status))       r.inativar();
        repo.salvar(r);
        currentRespId = r.getId();
        currentCpf = cpf;
    }

    @Given("um \"responsável\" {string} marcado como {string} e {string} regularizado")
    public void um_responsavel_inad_e_foi_regularizado(String estava, String status, String foi) {
        var cpf = generateCpf(seq.getAndIncrement());
        var r = new Responsavel("Resp Reg", cpf, "reg@ex.com", Responsavel.Status.ATIVO);
        if ("inadimplente".equalsIgnoreCase(status)) r.marcarInadimplente();
        repo.salvar(r);
        r.regularizar();
        repo.salvar(r);
        currentRespId = r.getId();
        currentCpf = cpf;
    }

    @Given("um \"responsável\" \"está\" registrado e \"não possui\" vínculos ativos com \"alunos\"")
    public void responsavel_estah_sem_vinculos() {
        lastError = null;
        var cpf = generateCpf(seq.getAndIncrement());
        var r = new Responsavel("Sem Vinculo", cpf, "sv@ex.com", Responsavel.Status.ATIVO);
        repo.salvar(r);
        currentRespId = r.getId();
        currentCpf = cpf;
    }

    @Given("um \"responsável\" {string} registrado e {string} vínculo com o \"aluno\" {string}")
    public void um_responsavel_registrado_e_vinculo(String estado, String possui, String alunoAlias) {
        var cpf = generateCpf(seq.getAndIncrement());
        var r = new Responsavel("Com Vinculo", cpf, "cv@ex.com", Responsavel.Status.ATIVO);
        repo.salvar(r);
        currentRespId = r.getId();
        currentCpf = cpf;

        currentAlunoId = aliasAluno.get(alunoAlias);
        if (currentAlunoId == null) {
            currentAlunoId = ensureAlunoComVinculos(alunoAlias, "Aluno " + alunoAlias, "R0", true, null, false);
        }
        if ("possui".equalsIgnoreCase(possui)) {
            var aluno = repo.porId(currentAlunoId).orElseThrow();
            aluno.adicionarResponsavel(currentRespId, false);
            repo.salvar(aluno);
        }
    }

    @Given("um \"aluno\" \"está\" vinculado aos \"responsáveis\" {string} e {string}")
    public void aluno_vinculado_a_R1_e_R2(String r1, String r2) {
        currentAlunoId = ensureAlunoComVinculos("A1", "Aluno Teste", r1, true, r2, false);
    }

    @Given("um \"aluno\" \"está\" vinculado ao \"responsável\" {string}")
    public void aluno_vinculado_a_R1(String r1) {
        currentAlunoId = ensureAlunoComVinculos("A1", "Aluno Teste", r1, true, null, false);
    }

    @Given("um \"aluno\" \"está\" vinculado apenas ao \"responsável\" {string}")
    public void aluno_vinculado_apenas_a(String r1) {
        currentAlunoId = ensureAlunoComVinculos("A1", "Aluno Teste", r1, true, null, false);
    }

    @Given("o \"responsável\" {string} {string} vinculado a esse \"aluno\"")
    public void responsavel_estado_vinculo_ao_aluno_atual(String rAlias, String estado) {
        var rId = ensureRespAlias(rAlias, rAlias + " Nome", rAlias.toLowerCase()+"@ex.com", Responsavel.Status.ATIVO);
        if (currentAlunoId == null) {
            currentAlunoId = ensureAlunoComVinculos("A1", "Aluno Teste", "R0", true, null, false);
        }
        var aluno = repo.porId(currentAlunoId).orElseThrow();
        boolean jaVinculado = aluno.getResponsaveis().contains(rId);

        if ("está".equals(estado) && !jaVinculado) {
            aluno.adicionarResponsavel(rId, false);
            repo.salvar(aluno);
        }
        if ("não está".equals(estado) && jaVinculado) {
            aluno.removerResponsavel(rId);
            repo.salvar(aluno);
        }
    }

    @Given("o \"responsável\" {string} {string} vinculado ao \"aluno\" {string}")
    public void responsavel_estado_vinculo_aluno_alias(String rAlias, String estado, String alunoAlias) {
        currentAlunoId = aliasAluno.get(alunoAlias);
        if (currentAlunoId == null) {
            currentAlunoId = ensureAlunoComVinculos(alunoAlias, "Aluno "+alunoAlias, "R0", true, null, false);
        }
        responsavel_estado_vinculo_ao_aluno_atual(rAlias, estado);
    }

    @Given("o \"responsável\" {string} vinculado ao \"aluno\" {string}")
    public void responsavel_contexto_vinculado_ao_aluno(String estado, String alunoAlias) {
        currentAlunoId = aliasAluno.get(alunoAlias);
        if (currentAlunoId == null) {
            currentAlunoId = ensureAlunoComVinculos(alunoAlias, "Aluno "+alunoAlias, "R0", true, null, false);
        }
        var aluno = repo.porId(currentAlunoId).orElseThrow();
        boolean jaVinculado = aluno.getResponsaveis().contains(currentRespId);
        if ("está".equals(estado) && !jaVinculado) {
            aluno.adicionarResponsavel(currentRespId, false);
            repo.salvar(aluno);
        }
        if ("não está".equals(estado) && jaVinculado) {
            aluno.removerResponsavel(currentRespId);
            repo.salvar(aluno);
        }
    }

    // ===================== Whens =====================

    @When("um coordenador cadastra o \"responsável\" com nome {string} e e-mail {string}")
    public void cadastra_responsavel_nome_email(String nome, String email) {
        lastError = null;
        try {
            lastNome = nome;
            lastEmail = email;
            currentRespId = servico.cadastrar(nome, currentCpf, email); // captura o ID gerado
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador cadastra o \"responsável\" com nome {string}, CPF {string} e e-mail {string}")
    public void cadastra_responsavel_nome_cpf_email(String nome, String cpf, String email) {
        lastError = null;
        currentCpf = cpf;
        try {
            lastNome = nome;
            lastEmail = email;
            currentRespId = servico.cadastrar(nome, cpf, email);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar o \"responsável\" com esse CPF")
    public void tenta_cadastrar_com_mesmo_cpf() {
        lastError = null;
        try { servico.cadastrar("Nome", currentCpf, "mail@ex.com"); } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar outro \"responsável\" com o CPF {string}")
    public void tenta_cadastrar_outro_com_cpf(String cpf) {
        lastError = null;
        try { servico.cadastrar("Outro", cpf, "outro@ex.com"); } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador cadastra o \"responsável\" com CPF {string}")
    public void cadastra_responsavel_com_cpf(String cpf) {
        lastError = null;
        currentCpf = cpf;
        try {
            lastNome = "Nome Qualquer";
            lastEmail = "mail@ex.com";
            currentRespId = servico.cadastrar(lastNome, cpf, lastEmail);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar o \"responsável\" com nome {string} e CPF {string}")
    public void tenta_cadastrar_sem_email(String nome, String cpf) {
        lastError = null;
        try { servico.cadastrar(nome, cpf, ""); } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar o \"responsável\" com nome {string} e e-mail {string}")
    public void tenta_cadastrar_sem_cpf(String nome, String email) {
        lastError = null;
        try { servico.cadastrar(nome, "", email); } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar o \"responsável\" com CPF {string} e e-mail {string}")
    public void tenta_cadastrar_sem_nome(String cpf, String email) {
        lastError = null;
        try { servico.cadastrar("", cpf, email); } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador altera o nome do \"responsável\" para {string} e o e-mail para {string}")
    public void altera_nome_email(String novoNome, String novoEmail) {
        lastError = null;
        try {
            lastNome = novoNome;
            lastEmail = novoEmail;
            ResponsavelId id = currentRespId;
            if (id == null) {
                for (var entry : aliasResp.entrySet()) {
                    var rid = entry.getValue();
                    var r = repo.porId(rid).orElse(null);
                    if (r != null && normCpf(r.getCpf()).equals(normCpf(currentCpf))) { id = rid; break; }
                }
            }
            assertNotNull(id, "Responsável não encontrado para edição");
            servico.atualizarContato(id, novoNome, novoEmail);
            currentRespId = id; // garantir que teremos o id para assert posterior
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta alterar o e-mail do \"responsável\" para {string} mantendo o nome {string}")
    public void tenta_alterar_email_vazio(String email, String nome) {
        lastError = null;
        try { servico.atualizarContato(currentRespId, nome, email); } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta alterar o nome do \"responsável\" para {string} mantendo o e-mail {string}")
    public void tenta_alterar_nome_vazio(String nome, String email) {
        lastError = null;
        try { servico.atualizarContato(currentRespId, nome, email); } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador desvincula o \"responsável\" {string} do \"aluno\"")
    public void desvincula_responsavel_do_aluno(String rAlias) {
        lastError = null;
        try {
            var rId = ensureRespAlias(rAlias, rAlias + " Nome", rAlias.toLowerCase()+"@ex.com", Responsavel.Status.ATIVO);
            lastVinculoRespId = rId;
            servico.desvincularDoAluno(rId, currentAlunoId);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta desvincular o \"responsável\" {string} do \"aluno\"")
    public void tenta_desvincular_responsavel_do_aluno(String rAlias) { desvincula_responsavel_do_aluno(rAlias); }

    @When("um coordenador vincula o \"responsável\" {string} ao \"aluno\"")
    public void vincula_responsavel_ao_aluno_simples(String rAlias) {
        lastError = null;
        try {
            var rId = ensureRespAlias(rAlias, rAlias + " Nome", rAlias.toLowerCase()+"@ex.com", Responsavel.Status.ATIVO);
            lastVinculoRespId = rId;
            servico.vincularAoAluno(rId, currentAlunoId, false);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador vincula o \"responsável\" {string} ao \"aluno\" com grau {string} e {string}")
    public void vincula_responsavel_ao_aluno_com_grau(String rAlias, String grau, String principalStr) {
        lastError = null;
        try {
            var rId = ensureRespAlias(rAlias, rAlias + " Nome", rAlias.toLowerCase()+"@ex.com", Responsavel.Status.ATIVO);
            boolean principal = "principal".equalsIgnoreCase(principalStr);
            lastVinculoRespId = rId;
            servico.vincularAoAluno(rId, currentAlunoId, principal);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta vincular o \"responsável\" {string} ao \"aluno\" com grau {string} e {string}")
    public void tenta_vincular_responsavel_ao_aluno_com_grau(String rAlias, String grau, String principalStr) {
        vincula_responsavel_ao_aluno_com_grau(rAlias, grau, principalStr);
    }

    @When("um coordenador tenta vincular novamente o \"responsável\" {string} ao mesmo \"aluno\"")
    public void tenta_vincular_duplicado(String rAlias) { vincula_responsavel_ao_aluno_simples(rAlias); }

    @When("um coordenador vincula o \"responsável\" ao \"aluno\" {string} com grau {string} e {string}")
    public void vincula_esse_responsavel_ao_aluno(String alunoAlias, String grau, String principalStr) {
        lastError = null;
        try {
            currentAlunoId = aliasAluno.get(alunoAlias);
            if (currentAlunoId == null) {
                currentAlunoId = ensureAlunoComVinculos(alunoAlias, "Aluno "+alunoAlias, "R0", true, null, false);
            }
            boolean principal = "principal".equalsIgnoreCase(principalStr);
            lastVinculoRespId = currentRespId;
            servico.vincularAoAluno(currentRespId, currentAlunoId, principal);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta vincular esse \"responsável\" ao \"aluno\" {string}")
    public void tenta_vincular_esse_responsavel_ao_aluno(String alunoAlias) {
        lastError = null;
        try {
            currentAlunoId = aliasAluno.get(alunoAlias);
            if (currentAlunoId == null) {
                currentAlunoId = ensureAlunoComVinculos(alunoAlias, "Aluno "+alunoAlias, "R0", true, null, false);
            }
            lastVinculoRespId = currentRespId;
            servico.vincularAoAluno(currentRespId, currentAlunoId, false);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador exclui o \"responsável\"")
    public void exclui_responsavel() {
        lastError = null;
        try { servico.excluir(currentRespId); } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta excluir o \"responsável\"")
    public void tenta_excluir_responsavel() { exclui_responsavel(); }

    // ===================== Thens (verificando via repo) =====================

    @Then("o sistema confirma o cadastro do \"responsável\"")
    public void confirma_cadastro() {
        assertNull(lastError, "Esperava sucesso, mas houve erro: " + (lastError==null?"":lastError.getMessage()));
        var salvo = repo.porId(currentRespId).orElseThrow(() -> new AssertionError("Responsável não persistido"));
        assertEquals(normCpf(currentCpf), normCpf(salvo.getCpf()), "CPF não persistiu como esperado");
        if (lastNome != null)  assertEquals(lastNome, salvo.getNome(), "Nome não persistiu");
        if (lastEmail != null) assertEquals(lastEmail, salvo.getEmail(), "E-mail não persistiu");
    }

    @Then("o sistema confirma a atualização dos dados do \"responsável\"")
    public void confirma_atualizacao() {
        assertNull(lastError, "Esperava sucesso na atualização: " + (lastError==null?"":lastError.getMessage()));
        var salvo = repo.porId(currentRespId).orElseThrow();
        if (lastNome != null)  assertEquals(lastNome, salvo.getNome(), "Nome não foi atualizado");
        if (lastEmail != null) assertEquals(lastEmail, salvo.getEmail(), "E-mail não foi atualizado");
    }

    @Then("o sistema confirma o vínculo do \"responsável\" ao \"aluno\"")
    public void confirma_vinculo() {
        assertNull(lastError, "Esperava sucesso no vínculo: " + (lastError == null ? "" : lastError.getMessage()));
        if (currentAlunoId != null && lastVinculoRespId != null) {
            var aluno = repo.porId(currentAlunoId).orElseThrow();
            assertTrue(aluno.getResponsaveis().contains(lastVinculoRespId), "Vínculo não persistiu no aluno");
        }
    }

    @Then("o sistema confirma a desvinculação")
    public void confirma_desvinculo() {
        assertNull(lastError, "Esperava sucesso na desvinculação: " + (lastError==null?"":lastError.getMessage()));
        if (currentAlunoId != null && lastVinculoRespId != null) {
            var aluno = repo.porId(currentAlunoId).orElseThrow();
            assertFalse(aluno.getResponsaveis().contains(lastVinculoRespId), "Responsável ainda vinculado ao aluno");
        }
    }

    @Then("o sistema confirma a exclusão do \"responsável\"")
    public void confirma_exclusao() {
        assertNull(lastError, "Esperava sucesso na exclusão: " + (lastError==null?"":lastError.getMessage()));
        assertTrue(repo.porId(currentRespId).isEmpty(), "Responsável ainda existe no repositório após exclusão");
    }

    @Then("o {string} permanece com pelo menos um {string} ativo")
    public void o_permanece_com_pelo_menos_um_ativo(String entidadeAluno, String entidadeResp) {
        var aluno = repo.porId(currentAlunoId).orElse(null);
        assertNotNull(aluno, "Aluno não encontrado no contexto");
        assertFalse(aluno.getResponsaveis().isEmpty(), "Aluno ficou sem responsáveis após a operação");
    }

    @Then("o sistema rejeita o cadastro")      public void rejeita_cadastro()      { assertNotNull(lastError, "Esperava erro no cadastro"); }
    @Then("o sistema rejeita a atualização")   public void rejeita_atualizacao()   { assertNotNull(lastError, "Esperava erro na atualização"); }
    @Then("o sistema rejeita o vínculo")       public void rejeita_vinculo()       { assertNotNull(lastError, "Esperava erro no vínculo"); }
    @Then("o sistema rejeita a desvinculação") public void rejeita_desvinculo()    { assertNotNull(lastError, "Esperava erro na desvinculação"); }
    @Then("o sistema rejeita a exclusão")      public void rejeita_exclusao()      { assertNotNull(lastError, "Esperava erro na exclusão"); }

    @Then("o sistema informa que {string}")
    public void o_sistema_informa_que(String msg) {
        assertNotNull(lastError, "Não houve erro para verificar mensagem");
        assertTrue(lastError.getMessage().toLowerCase().contains(msg.toLowerCase()),
                "Mensagem diferente. Esperado conter: \"" + msg + "\" mas foi: \"" +
                        (lastError == null ? "" : lastError.getMessage()) + "\"");
    }
}
