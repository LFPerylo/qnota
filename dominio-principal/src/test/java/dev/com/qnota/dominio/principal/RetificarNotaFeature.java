package dev.com.qnota.dominio.principal;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.cucumber.java.en.*;

import dev.com.qnota.infraestrutura.persistencia.memoria.RepositorioEmMemoria;

import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.justificativa.Justificativa;
import dev.com.qnota.dominio.principal.justificativa.JustificativaId;
import dev.com.qnota.dominio.principal.nota.Nota;
import dev.com.qnota.dominio.principal.nota.NotaId;
import dev.com.qnota.dominio.principal.nota.NotaServico;
import dev.com.qnota.dominio.principal.professor.ProfessorId;
import dev.com.qnota.dominio.principal.ranking.RankingServico;
import dev.com.qnota.dominio.principal.simulado.Simulado;
import dev.com.qnota.dominio.principal.simulado.Simulado.DisciplinaPeso;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.turma.TurmaId;

public class RetificarNotaFeature {

    // ===== estado por cenário =====
    private RepositorioEmMemoria repo;
    private RankingServico ranking;
    private NotaServico notaSrv;

    private final AtomicInteger seq = new AtomicInteger(1);

    private SimuladoId simuladoId;
    private AlunoId alunoId;
    private DisciplinaId disciplinaId;

    private NotaId notaOriginalId;
    private double valorOriginal;
    private Double valorRetificado;

    private Exception lastError;

    // ===== utils =====
    private static DisciplinaPeso dp(int did, double peso) {
        return new DisciplinaPeso(new DisciplinaId(did), peso);
    }

    private Simulado novoSimulado(Simulado.Status status) {
        var turma = new TurmaId(seq.getAndIncrement());
        // dois pesos somando 10 para cumprir RN-12/RN-13
        var s = new Simulado(
            LocalDate.now(),
            status,
            turma,
            List.of(
                dp(100, 6.0),
                dp(101, 4.0)
            )
        );
        return s;
    }

    /** Localiza a nota do par (aluno, disciplina) neste simulado e guarda o id. */
    private void capturarNotaOriginalId() {
        var lista = repo.porSimulado(simuladoId);
        var opt = lista.stream()
                .filter(n -> n.getAluno().equals(alunoId) && n.getDisciplina().equals(disciplinaId))
                .findFirst();
        notaOriginalId = opt.map(Nota::getId).orElseThrow();
    }

    // ===== Given =====

    @Given("um simulado {string} com nota original {double}")
    public void um_simulado_com_nota_original(String estado, double valor) {
        lastError = null;

        repo = new RepositorioEmMemoria();
        ranking = new RankingServico(repo, repo, repo, repo);
        // registra justificativas no próprio 'repo'
        notaSrv = new NotaServico(repo, ranking, repo, repo, repo, repo, repo);

        // IDs padrão para este cenário
        alunoId = new AlunoId(seq.getAndIncrement());
        disciplinaId = new DisciplinaId(100); // uma das disciplinas do simulado criado

        // Criar o aluno e a turma necessários para o teste
        var professorId = new dev.com.qnota.dominio.principal.professor.ProfessorId(seq.getAndIncrement());
        var turma = new dev.com.qnota.dominio.principal.turma.Turma("Turma Teste", 2025, true, professorId);
        repo.salvar(turma);
        var turmaId = turma.getId();
        
        var responsavelId = new dev.com.qnota.dominio.principal.responsavel.ResponsavelId(seq.getAndIncrement());
        var responsavel = new dev.com.qnota.dominio.principal.responsavel.Responsavel("Responsável", "529.982.247-25", "resp@test.com", dev.com.qnota.dominio.principal.responsavel.Responsavel.Status.ATIVO);
        repo.salvar(responsavel);
        
        var aluno = new dev.com.qnota.dominio.principal.aluno.Aluno(alunoId, "Aluno Teste", java.time.LocalDate.of(2012, 1, 1), true, turmaId, 
            java.util.List.of(new dev.com.qnota.dominio.principal.aluno.Aluno.AlunoResponsavel(responsavelId, true)));
        repo.salvar(aluno);

        // Criar as disciplinas necessárias para o simulado
        var disciplina1 = new dev.com.qnota.dominio.principal.disciplina.Disciplina("Matemática", new dev.com.qnota.dominio.principal.disciplina.Disciplina.AreaConhecimento(1, "Exatas"));
        disciplina1.atribuirIdSeAusente(new DisciplinaId(100));
        repo.salvar(disciplina1);
        
        var disciplina2 = new dev.com.qnota.dominio.principal.disciplina.Disciplina("Física", new dev.com.qnota.dominio.principal.disciplina.Disciplina.AreaConhecimento(1, "Exatas"));
        disciplina2.atribuirIdSeAusente(new DisciplinaId(101));
        repo.salvar(disciplina2);

        // Cria o simulado inicialmente EM_EDICAO para poder lançar a nota via serviço - usando o turmaId criado
        var sim = new Simulado(LocalDate.now(), Simulado.Status.EM_EDICAO, turmaId, List.of(dp(100, 6.0), dp(101, 4.0)));
        repo.salvar(sim);
        simuladoId = sim.getId();

        // Lança a nota original (RN-32 cumprida pois está EM_EDICAO)
        valorOriginal = valor;
        notaSrv.lancar(alunoId, simuladoId, disciplinaId, valorOriginal);
        capturarNotaOriginalId();

        // Se o cenário pede "finalizado", finaliza depois de lançar
        if ("finalizado".equalsIgnoreCase(estado)) {
            var s = repo.porId(simuladoId).orElseThrow();
            s.finalizar();
            repo.salvar(s);
        } else {
            // qualquer outra coisa tratamos como "em edição"
            var s = repo.porId(simuladoId).orElseThrow();
            assertEquals(Simulado.Status.EM_EDICAO, s.getStatus());
        }
    }

    // ===== When =====

    @When("o coordenador retifica a nota para {double} com justificativa {string}")
    public void coordenador_retifica(double novoValor, String justificativa) {
        lastError = null;
        valorRetificado = novoValor;
        try {
            notaSrv.retificarComJustificativa(notaOriginalId, novoValor, justificativa, new ProfessorId(999));
        } catch (Exception e) {
            lastError = e;
        }
    }

    @When("o coordenador tenta retificar a nota para {double} com justificativa {string}")
    public void coordenador_tenta_retificar(double novoValor, String justificativa) {
        coordenador_retifica(novoValor, justificativa);
    }

    // ===== Then =====

    @Then("o sistema confirma retificação com nova versão armazenada")
    public void confirma_retificacao() {
        assertNull(lastError, "Esperava sucesso na retificação: " + lastError);

        // Deve haver duas notas para o mesmo (aluno, simulado, disciplina): original e nova
        var notas = repo.porSimulado(simuladoId).stream()
                .filter(n -> n.getAluno().equals(alunoId) && n.getDisciplina().equals(disciplinaId))
                .toList();

        assertEquals(2, notas.size(), "RN-38: esperado manter original e criar nova versão");

        var idsDistintos = notas.stream().map(n -> n.getId().value()).distinct().count();
        assertEquals(2, idsDistintos, "As duas versões devem ter IDs distintos");

        // Confere que uma delas mantém o valor original e a outra tem o valor retificado
        boolean achouOriginal = notas.stream().anyMatch(n -> Math.abs(n.getValor() - valorOriginal) < 1e-9);
        boolean achouNova     = notas.stream().anyMatch(n -> Math.abs(n.getValor() - valorRetificado) < 1e-9);
        assertTrue(achouOriginal && achouNova, "Deve existir uma nota com valor original e outra com o valor retificado");
    }

    @Then("a justificativa registra valores {double} -> {double}")
    public void justificativa_registra_valores(double anterior, double corrigida) {
        // A justificativa é salva amarrada ao ID da NOTA ORIGINAL
        var js = repo.porNota(notaOriginalId);
        assertTrue(!js.isEmpty(), "RN-37/38: era esperado registro de justificativa");
        Justificativa j = js.get(js.size() - 1); // pega a última registrada

        assertEquals(notaOriginalId, j.getNota(), "Justificativa deve apontar para a nota original");
        assertEquals(anterior, j.getNotaAnterior(), 1e-9, "Valor anterior divergente");
        assertEquals(corrigida, j.getNotaCorrigida(), 1e-9, "Valor corrigido divergente");
        assertNotNull(j.getProfessor(), "Professor deve estar informado");
        assertNotNull(j.getDataHora(), "Data/hora deve estar informada");
        assertTrue(j.getJustificativa().trim().length() >= 20, "Texto de justificativa deve ter pelo menos 20 caracteres");
    }

    @Then("o sistema rejeita a retificação e informa {string}")
    public void rejeita_retificacao_informa(String pedacoMensagem) {
        assertNotNull(lastError, "Era esperado erro na retificação");
        var m = lastError.getMessage();
        if (m == null) m = "";
        assertTrue(m.toLowerCase().contains(pedacoMensagem.toLowerCase()),
                "Mensagem esperada conter: \"" + pedacoMensagem + "\" mas foi: \"" + m + "\"");
    }
}
