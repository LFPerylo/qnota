package dev.com.qnota.dominio.principal.nota;

import java.time.LocalDateTime;
import java.util.Objects;

import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.aluno.AlunoRepositorio;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaRepositorio;
import dev.com.qnota.dominio.principal.justificativa.Justificativa;
import dev.com.qnota.dominio.principal.justificativa.JustificativaRepositorio;
import dev.com.qnota.dominio.principal.professor.ProfessorId;
import dev.com.qnota.dominio.principal.ranking.RankingServico;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.simulado.SimuladoRepositorio;
import dev.com.qnota.dominio.principal.turma.TurmaRepositorio;

public class NotaServico {

    private final NotaRepositorio repo;
    private final RankingServico rankingServico;
    private final AlunoRepositorio alunoRepo;
    private final SimuladoRepositorio simuladoRepo;
    private final TurmaRepositorio turmaRepo;
    private final DisciplinaRepositorio disciplinaRepo;

    // Registro de justificativas (histórico) é opcional
    private final JustificativaRepositorio justificativaRepo;

    public NotaServico(NotaRepositorio repo, RankingServico rankingServico, 
                      AlunoRepositorio alunoRepo, SimuladoRepositorio simuladoRepo, 
                      TurmaRepositorio turmaRepo, DisciplinaRepositorio disciplinaRepo) {
        this(repo, rankingServico, alunoRepo, simuladoRepo, turmaRepo, disciplinaRepo, null);
    }

    public NotaServico(NotaRepositorio repo, RankingServico rankingServico, 
                      AlunoRepositorio alunoRepo, SimuladoRepositorio simuladoRepo, 
                      TurmaRepositorio turmaRepo, DisciplinaRepositorio disciplinaRepo, JustificativaRepositorio justificativaRepo) {
        this.repo = Objects.requireNonNull(repo);
        this.rankingServico = Objects.requireNonNull(rankingServico);
        this.alunoRepo = Objects.requireNonNull(alunoRepo);
        this.simuladoRepo = Objects.requireNonNull(simuladoRepo);
        this.turmaRepo = Objects.requireNonNull(turmaRepo);
        this.disciplinaRepo = Objects.requireNonNull(disciplinaRepo);
        this.justificativaRepo = justificativaRepo; // opcional
    }

    /** Conveniência: cria e lança uma nova nota já com timestamp. */
    public void lancar(AlunoId aluno, SimuladoId simulado, DisciplinaId disciplina, double valor) {
        var agora = LocalDateTime.now();
        var n = new Nota(aluno, simulado, disciplina, valor, agora);
        lancar(n);
    }

    /**
     * Lançamento:
     * - RN-32: apenas quando simulado está EM_EDICAO
     * - RN-31: faixa [0..10] validada no agregado
     * - RN-33: evita duplicidade (aluno+simulado+disciplina)
     * - RN-31/32/33: aluno inativado não pode receber nota
     * - RN-94: turma inativada não permite novas notas
     * - RN-34: validar existência de IDs
     * - RN-98/RN-99: recalcula ranking
     */
    public void lancar(Nota n) {
        // RN-34: validar existência de IDs antes de salvar
        var aluno = alunoRepo.porId(n.getAluno()).orElseThrow(() -> new IllegalStateException("aluno não encontrado"));
        var simulado = simuladoRepo.porId(n.getSimulado()).orElseThrow(() -> new IllegalStateException("simulado não encontrado"));
        var disciplina = disciplinaRepo.porId(n.getDisciplina()).orElseThrow(() -> new IllegalStateException("disciplina não encontrada"));
        var turma = turmaRepo.porId(simulado.getTurma()).orElseThrow(() -> new IllegalStateException("turma não encontrada"));
        
        // RN-31/32/33: aluno inativado não pode receber nota
        if (!aluno.isAtivo())
            throw new IllegalStateException("RN-31/RN-32/RN-33: aluno inativado não pode receber nota.");
        
        // RN-94: turma inativada não permite novas notas
        if (!turma.isAtivo())
            throw new IllegalStateException("RN-94: turma inativada não permite novas notas/simulados.");
        
        if (!repo.simuladoEstaEmEdicao(n.getSimulado()))
            throw new IllegalStateException("RN-32: Lançamento só com simulado EM_EDICAO.");
        if (repo.porChave(n.getAluno(), n.getSimulado(), n.getDisciplina()).isPresent())
            throw new IllegalStateException("RN-33: Nota duplicada para mesma disciplina/simulado/aluno.");

        repo.salvar(n); // repositório atribui ID se necessário
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
