package dev.com.qnota.dominio.principal.nota;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.justificativa.Justificativa;
import dev.com.qnota.dominio.principal.justificativa.JustificativaId;
import dev.com.qnota.dominio.principal.justificativa.JustificativaRepositorio;
import dev.com.qnota.dominio.principal.professor.ProfessorId;
import dev.com.qnota.dominio.principal.ranking.RankingServico;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;

public class NotaServico {

    private final NotaRepositorio repo;
    private final RankingServico rankingServico;

    // Para registrar a justificativa da retificação (histórico)
    private final JustificativaRepositorio justificativaRepo; // pode ser null se não quiser registrar
    private final AtomicInteger seqJust = new AtomicInteger(1);

    public NotaServico(NotaRepositorio repo, RankingServico rankingServico) {
        this(repo, rankingServico, null);
    }

    public NotaServico(NotaRepositorio repo, RankingServico rankingServico, JustificativaRepositorio justificativaRepo) {
        this.repo = Objects.requireNonNull(repo);
        this.rankingServico = Objects.requireNonNull(rankingServico);
        this.justificativaRepo = justificativaRepo; // opcional
    }

    /** Conveniência: cria e lança uma nova nota já com timestamp. */
    public void lancar(AlunoId aluno, SimuladoId simulado, DisciplinaId disciplina, double valor) {
        var agora = LocalDateTime.now();
        var n = new Nota(null, aluno, simulado, disciplina, valor, agora);
        lancar(n);
    }

    /**
     * Lançamento:
     * - RN-32: apenas quando simulado está EM_EDICAO
     * - RN-31: faixa [0..10] validada na entidade
     * - RN-33: evita duplicidade (aluno+simulado+disciplina)
     * - RN-98/RN-99: recalcula ranking
     */
    public void lancar(Nota n) {
        if (!repo.simuladoEstaEmEdicao(n.getSimulado()))
            throw new IllegalStateException("RN-32: Lançamento só com simulado EM_EDICAO.");
        if (repo.porChave(n.getAluno(), n.getSimulado(), n.getDisciplina()).isPresent())
            throw new IllegalStateException("RN-33: Nota duplicada para mesma disciplina/simulado/aluno.");

        repo.salvar(n);
        rankingServico.recalcular(n.getSimulado());
    }

    /**
     * Retificação IN-PLACE (sem histórico). Mantida por compatibilidade,
     * mas para cumprir RN-38 use retificarComJustificativa(...).
     */
    public void retificar(NotaId id, double novoValor) {
        var n = repo.porId(id).orElseThrow();
        if (!repo.simuladoEstaEmEdicao(n.getSimulado()))
            throw new IllegalStateException("RN-39: Retificação só permitida em simulado EM_EDICAO.");

        n.alterar(novoValor); // RN-31
        repo.salvar(n);
        rankingServico.recalcular(n.getSimulado());
    }

    /**
     * RN-37/38/39: retificar criando NOVA versão e registrando justificativa.
     * - RN-39: só em simulado EM_EDICAO
     * - RN-37: justificativa obrigatória com >= 20 caracteres
     * - RN-38: nova versão (novo ID) e original preservada
     */
    public void retificarComJustificativa(NotaId idOriginal, double novoValor, String justificativa, ProfessorId professor) {
        var original = repo.porId(idOriginal).orElseThrow();

        // RN-39
        if (!repo.simuladoEstaEmEdicao(original.getSimulado()))
            throw new IllegalStateException("RN-39: Retificação só permitida em simulado EM_EDICAO.");

        // RN-37
        String txt = justificativa == null ? "" : justificativa.trim();
        if (txt.length() < 20)
            throw new IllegalArgumentException("RN-37: Justificativa deve conter ao menos 20 caracteres.");

        // RN-38: criar nova versão (novo ID). A original fica intacta.
        var nova = new Nota(
                null,
                original.getAluno(),
                original.getSimulado(),
                original.getDisciplina(),
                novoValor,
                LocalDateTime.now());
        repo.salvar(nova);

        // Registrar justificativa (se o repositório estiver disponível)
        if (justificativaRepo != null) {
            Objects.requireNonNull(professor, "professor é obrigatório na retificação");
            var j = new Justificativa(
                    new JustificativaId(seqJust.getAndIncrement()),
                    idOriginal,
                    original.getValor(),
                    novoValor,
                    txt,
                    LocalDateTime.now(),
                    professor);
            justificativaRepo.salvar(j);
        }

        rankingServico.recalcular(original.getSimulado());
    }
}
