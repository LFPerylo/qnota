package dev.com.qnota.dominio.principal;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import dev.com.qnota.infraestrutura.persistencia.memoria.RepositorioEmMemoria;
import dev.com.qnota.dominio.principal.aluno.Aluno;
import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.responsavel.Responsavel;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelServico;
import dev.com.qnota.dominio.principal.turma.TurmaId;

/**
 * Steps do Cucumber para a feature "Gerenciar responsáveis".
 * Usa o RepositorioEmMemoria como double de teste.
 */
public class GerenciarResponsaveisFeature {

    // ===== Infra fake e serviços =====
    private final RepositorioEmMemoria repo = new RepositorioEmMemoria();
    // Serviço precisa de (ResponsavelRepositorio, AlunoRepositorio).
    private final ResponsavelServico servico = new ResponsavelServico(repo, repo);

    // ===== Contexto do cenário =====
    private final AtomicInteger seq = new AtomicInteger(1);
    private final Map<String, ResponsavelId> aliasResp = new HashMap<>(); // "R1" -> id
    private final Map<String, AlunoId> aliasAluno = new HashMap<>();      // "A1" -> id
    private final Map<String, String> cpfByAlias = new HashMap<>();       // "R1" -> cpf

    private String currentCpf;                 // último CPF em foco
    private ResponsavelId currentRespId;       // responsável "em foco"
    private AlunoId currentAlunoId;            // aluno "em foco"
    private Exception lastError;               // exceção capturada

    // ===== Utilidades =====
    private ResponsavelId newRespId() { return new ResponsavelId(seq.getAndIncrement()); }
    private AlunoId newAlunoId() { return new AlunoId(seq.getAndIncrement()); }
    private TurmaId defaultTurma() { return new TurmaId(999); }

    // Gera um CPF válido (somente dígitos) para testes determinísticos
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
        // procura pelo CPF entre ids já conhecidos
        for (var e : aliasResp.entrySet()) {
            var rOpt = repo.porId(e.getValue());
            if (rOpt.isPresent() && rOpt.get().getCpf().replaceAll("\\D","").equals(cpf.replaceAll("\\D",""))) {
                return rOpt.get();
            }
        }
        // não achou, cria novo
        var id = newRespId();
        var r = new Responsavel(id, nome, cpf, email, status);
        repo.salvar(r);
        String alias = "R" + id.value();
        aliasResp.put(alias, id);
        cpfByAlias.put(alias, cpf);
        return r;
    }

    private ResponsavelId ensureRespAlias(String alias, String nome, String email, Responsavel.Status status) {
        return aliasResp.computeIfAbsent(alias, a -> {
            String cpf = cpfForAlias(a);
            var id = newRespId();
            var r = new Responsavel(id, nome, cpf, email, status);
            repo.salvar(r);
            return id;
        });
    }

    private AlunoId ensureAlunoComVinculos(String alunoAlias, String nomeAluno,
                                           String r1Alias, boolean r1Principal,
                                           String r2Alias, boolean r2Principal) {
        var aId = aliasAluno.computeIfAbsent(alunoAlias, a -> newAlunoId());
        var r1Id = ensureRespAlias(r1Alias, r1Alias + " Nome",
                r1Alias.toLowerCase()+"@ex.com", Responsavel.Status.ATIVO);
        var r2Id = (r2Alias != null)
                ? ensureRespAlias(r2Alias, r2Alias + " Nome", r2Alias.toLowerCase()+"@ex.com", Responsavel.Status.ATIVO)
                : null;

        var lista = new java.util.ArrayList<Aluno.AlunoResponsavel>();
        lista.add(new Aluno.AlunoResponsavel(r1Id, "Parente", r1Principal));
        if (r2Id != null) lista.add(new Aluno.AlunoResponsavel(r2Id, "Parente", r2Principal));

        var aluno = new Aluno(aId, nomeAluno, LocalDate.of(2012,1,1), true, defaultTurma(), lista);
        repo.salvar(aluno);
        return aId;
    }

    // ===================== Givens =====================

    // genérico: com CPF explícito e estado "está" | "não está"
    @Given("um \"responsável\" com CPF {string} {string} registrado")
    public void um_responsavel_com_cpf_registrado(String cpf, String estado) {
        lastError = null;
        currentCpf = cpf;
        if ("não está".equals(estado)) {
            // garante que não exista
            aliasResp.values().forEach(id -> repo.porId(id).ifPresent(r -> {
                if (r.getCpf().replaceAll("\\D","").equals(cpf.replaceAll("\\D",""))) repo.excluir(id);
            }));
            currentRespId = null;
        } else { // "está" | "já está"
            var r = ensureResponsavelByCpf(cpf, "Nome Padrão", "email@exemplo.com", Responsavel.Status.ATIVO);
            currentRespId = r.getId();
        }
    }

    // sem CPF (para cenários de NOT BLANK etc.)
    @Given("um \"responsável\" {string} registrado")
    public void um_responsavel_sem_cpf_registrado(String estado) {
        lastError = null;
        String cpf = generateCpf(seq.getAndIncrement());
        currentCpf = cpf;
        if ("não está".equals(estado)) {
            currentRespId = null; // nada criado
        } else { // "está"
            var r = ensureResponsavelByCpf(cpf, "Nome Padrão", "email@exemplo.com", Responsavel.Status.ATIVO);
            currentRespId = r.getId();
        }
    }

    @Given("não existe \"responsável\" com CPF {string}")
    public void nao_existe_responsavel_com_cpf(String cpf) {
        lastError = null;
        currentCpf = cpf;
        aliasResp.values().forEach(id -> repo.porId(id).ifPresent(r -> {
            if (r.getCpf().replaceAll("\\D","").equals(cpf.replaceAll("\\D",""))) repo.excluir(id);
        }));
        currentRespId = null;
    }

    @Given("um \"responsável\" {string} marcado como {string}")
    public void um_responsavel_marcado_como(String verboEstado, String status) {
        lastError = null;
        var id = newRespId();
        var cpf = generateCpf(id.value());
        var r = new Responsavel(id, "Resp", cpf, "resp@ex.com", Responsavel.Status.ATIVO);
        if ("inadimplente".equalsIgnoreCase(status)) r.marcarInadimplente();
        if ("inativo".equalsIgnoreCase(status))       r.inativar();
        repo.salvar(r);
        currentRespId = id;
        currentCpf = cpf;
    }

    @Given("um \"responsável\" {string} marcado como {string} e {string} regularizado")
    public void um_responsavel_inad_e_foi_regularizado(String estava, String status, String foi) {
        var id = newRespId();
        var cpf = generateCpf(id.value());
        var r = new Responsavel(id, "Resp Reg", cpf, "reg@ex.com", Responsavel.Status.ATIVO);
        if ("inadimplente".equalsIgnoreCase(status)) r.marcarInadimplente();
        repo.salvar(r);
        r.regularizar();
        repo.salvar(r);
        currentRespId = id;
        currentCpf = cpf;
    }

    @Given("um \"responsável\" \"está\" registrado e \"não possui\" vínculos ativos com \"alunos\"")
    public void responsavel_estah_sem_vinculos() {
        lastError = null;
        var id = newRespId();
        var cpf = generateCpf(id.value());
        var r = new Responsavel(id, "Sem Vinculo", cpf, "sv@ex.com", Responsavel.Status.ATIVO);
        repo.salvar(r);
        currentRespId = id;
        currentCpf = cpf;
        // garante que nenhum aluno conheça esse responsável (não adicionamos em aluno algum)
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
        boolean jaVinculado = aluno.getResponsaveis().stream().anyMatch(ar -> ar.responsavel().equals(rId));

        if ("está".equals(estado) && !jaVinculado) {
            var nova = new java.util.ArrayList<>(aluno.getResponsaveis());
            nova.add(new Aluno.AlunoResponsavel(rId, "Parente", false));
            aluno = new Aluno(aluno.getId(), aluno.getNome(), aluno.getDataNascimento(),
                    aluno.isAtivo(), aluno.getTurma(), nova);
            repo.salvar(aluno);
        }
        if ("não está".equals(estado) && jaVinculado) {
            aluno.removerResponsavel(rId);
            repo.salvar(aluno);
        }
    }

    @Given("o \"responsável\" {string} {string} vinculado ao \"aluno\" {string}")
    public void responsavel_estado_vinculo_aluno_alias(String rAlias, String estado, String alunoAlias) {
        currentAlunoId = aliasAluno.computeIfAbsent(
                alunoAlias, k -> ensureAlunoComVinculos(alunoAlias, "Aluno "+alunoAlias, "R0", true, null, false));
        responsavel_estado_vinculo_ao_aluno_atual(rAlias, estado);
    }

    // ===================== Whens =====================

    @When("um coordenador cadastra o \"responsável\" com nome {string} e e-mail {string}")
    public void cadastra_responsavel_nome_email(String nome, String email) {
        lastError = null;
        try {
            servico.cadastrar(newRespId(), nome, currentCpf, email);
        } catch (Exception e) { lastError = e; }
    }

    // usado pelos cenários básicos (nome/cpf/email explícitos)
    @When("um coordenador cadastra o \"responsável\" com nome {string}, CPF {string} e e-mail {string}")
    public void cadastra_responsavel_nome_cpf_email(String nome, String cpf, String email) {
        lastError = null;
        currentCpf = cpf;
        try {
            servico.cadastrar(newRespId(), nome, cpf, email);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta cadastrar o \"responsável\" com esse CPF")
    public void tenta_cadastrar_com_mesmo_cpf() {
        lastError = null;
        try {
            servico.cadastrar(newRespId(), "Nome", currentCpf, "mail@ex.com");
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador cadastra o \"responsável\" com CPF {string}")
    public void cadastra_responsavel_com_cpf(String cpf) {
        lastError = null;
        currentCpf = cpf;
        try {
            servico.cadastrar(newRespId(), "Nome Qualquer", cpf, "mail@ex.com");
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador altera o nome do \"responsável\" para {string} e o e-mail para {string}")
    public void altera_nome_email(String novoNome, String novoEmail) {
        lastError = null;
        try {
            ResponsavelId id = currentRespId;
            if (id == null) {
                for (var entry : aliasResp.entrySet()) {
                    var rid = entry.getValue();
                    var r = repo.porId(rid).orElse(null);
                    if (r != null && r.getCpf().replaceAll("\\D","").equals(currentCpf.replaceAll("\\D",""))) { id = rid; break; }
                }
            }
            assertNotNull(id, "Responsável não encontrado para edição");
            servico.atualizarContato(id, novoNome, novoEmail);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador desvincula o \"responsável\" {string} do \"aluno\"")
    public void desvincula_responsavel_do_aluno(String rAlias) {
        lastError = null;
        try {
            var rId = ensureRespAlias(rAlias, rAlias + " Nome", rAlias.toLowerCase()+"@ex.com", Responsavel.Status.ATIVO);
            servico.desvincularDoAluno(rId, currentAlunoId);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta desvincular o \"responsável\" {string} do \"aluno\"")
    public void tenta_desvincular_responsavel_do_aluno(String rAlias) {
        desvincula_responsavel_do_aluno(rAlias);
    }

    @When("um coordenador vincula o \"responsável\" {string} ao \"aluno\"")
    public void vincula_responsavel_ao_aluno_simples(String rAlias) {
        lastError = null;
        try {
            var rId = ensureRespAlias(rAlias, rAlias + " Nome", rAlias.toLowerCase()+"@ex.com", Responsavel.Status.ATIVO);
            servico.vincularAoAluno(rId, currentAlunoId, "Parente", false);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador vincula o \"responsável\" {string} ao \"aluno\" com grau {string} e {string}")
    public void vincula_responsavel_ao_aluno_com_grau(String rAlias, String grau, String principalStr) {
        lastError = null;
        try {
            var rId = ensureRespAlias(rAlias, rAlias + " Nome", rAlias.toLowerCase()+"@ex.com", Responsavel.Status.ATIVO);
            boolean principal = "principal".equalsIgnoreCase(principalStr);
            servico.vincularAoAluno(rId, currentAlunoId, grau, principal);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta vincular novamente o \"responsável\" {string} ao mesmo \"aluno\"")
    public void tenta_vincular_duplicado(String rAlias) {
        vincula_responsavel_ao_aluno_simples(rAlias);
    }

    @When("um coordenador tenta vincular esse \"responsável\" ao \"aluno\" {string}")
    public void tenta_vincular_esse_responsavel_ao_aluno(String alunoAlias) {
        lastError = null;
        try {
            currentAlunoId = aliasAluno.computeIfAbsent(
                    alunoAlias, k -> ensureAlunoComVinculos(alunoAlias, "Aluno "+alunoAlias, "R0", true, null, false));
            servico.vincularAoAluno(currentRespId, currentAlunoId, "Parente", false);
        } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador exclui o \"responsável\"")
    public void exclui_responsavel() {
        lastError = null;
        try { servico.excluir(currentRespId); } catch (Exception e) { lastError = e; }
    }

    @When("um coordenador tenta excluir o \"responsável\"")
    public void tenta_excluir_responsavel() { exclui_responsavel(); }

    // ===================== Thens =====================

    @Then("o sistema confirma o cadastro do \"responsável\"")
    public void confirma_cadastro() {
        assertNull(lastError, "Esperava sucesso, mas houve erro: " + (lastError==null?"":lastError.getMessage()));
        boolean existe = aliasResp.values().stream().anyMatch(id ->
            repo.porId(id).map(r -> r.getCpf().replaceAll("\\D","").equals(currentCpf.replaceAll("\\D",""))).orElse(false)
        );
        assertTrue(existe, "Cadastro não encontrado para CPF informado");
    }

    @Then("o sistema confirma a atualização dos dados do \"responsável\"")
    public void confirma_atualizacao() {
        assertNull(lastError, "Esperava sucesso na atualização: " + (lastError==null?"":lastError.getMessage()));
    }

    @Then("o sistema confirma o vínculo do \"responsável\" ao \"aluno\"")
    public void confirma_vinculo() {
        assertNull(lastError, "Esperava sucesso no vínculo: " + (lastError==null?"":lastError.getMessage()));
    }

    @Then("o sistema confirma a desvinculação")
    public void confirma_desvinculo() {
        assertNull(lastError, "Esperava sucesso na desvinculação: " + (lastError==null?"":lastError.getMessage()));
    }

    @Then("o sistema confirma a exclusão do \"responsável\"")
    public void confirma_exclusao() {
        assertNull(lastError, "Esperava sucesso na exclusão: " + (lastError==null?"":lastError.getMessage()));
    }

    @Then("o {string} permanece com pelo menos um {string} ativo")
    public void o_permanece_com_pelo_menos_um_ativo(String entidadeAluno, String entidadeResp) {
        var aluno = repo.porId(currentAlunoId).orElse(null);
        assertNotNull(aluno, "Aluno não encontrado no contexto");
        assertTrue(!aluno.getResponsaveis().isEmpty(), "Aluno ficou sem responsáveis após a operação");
    }

    @Then("o sistema rejeita o cadastro")
    public void rejeita_cadastro() { assertNotNull(lastError, "Esperava erro no cadastro"); }

    @Then("o sistema rejeita a atualização")
    public void rejeita_atualizacao() { assertNotNull(lastError, "Esperava erro na atualização"); }

    @Then("o sistema rejeita o vínculo")
    public void rejeita_vinculo() { assertNotNull(lastError, "Esperava erro no vínculo"); }

    @Then("o sistema rejeita a desvinculação")
    public void rejeita_desvinculo() { assertNotNull(lastError, "Esperava erro na desvinculação"); }

    @Then("o sistema rejeita a exclusão")
    public void rejeita_exclusao() { assertNotNull(lastError, "Esperava erro na exclusão"); }

    @Then("o sistema informa que {string}")
    public void o_sistema_informa_que(String msg) {
        assertNotNull(lastError, "Não houve erro para verificar mensagem");
        assertTrue(lastError.getMessage().toLowerCase().contains(msg.toLowerCase()),
                "Mensagem diferente. Esperado conter: \"" + msg + "\" mas foi: \"" +
                        (lastError == null ? "" : lastError.getMessage()) + "\"");
    }
}
