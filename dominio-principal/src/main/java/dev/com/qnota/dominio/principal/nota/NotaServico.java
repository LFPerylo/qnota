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
import dev.com.qnota.dominio.principal.simulado.Simulado;
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
    private final JustificativaRepositorio justificativaRepo; // opcional

    public NotaServico(NotaRepositorio repo,
                       RankingServico rankingServico,
                       AlunoRepositorio alunoRepo,
                       SimuladoRepositorio simuladoRepo,
                       TurmaRepositorio turmaRepo,
                       DisciplinaRepositorio disciplinaRepo) {
        this(repo, rankingServico, alunoRepo, simuladoRepo, turmaRepo, disciplinaRepo, null);
    }

    public NotaServico(NotaRepositorio repo,
                       RankingServico rankingServico,
                       AlunoRepositorio alunoRepo,
                       SimuladoRepositorio simuladoRepo,
                       TurmaRepositorio turmaRepo,
                       DisciplinaRepositorio disciplinaRepo,
                       JustificativaRepositorio justificativaRepo) {
        this.repo = Objects.requireNonNull(repo);
        this.rankingServico = Objects.requireNonNull(rankingServico);
        this.alunoRepo = Objects.requireNonNull(alunoRepo);
        this.simuladoRepo = Objects.requireNonNull(simuladoRepo);
        this.turmaRepo = Objects.requireNonNull(turmaRepo);
        this.disciplinaRepo = Objects.requireNonNull(disciplinaRepo);
        this.justificativaRepo = justificativaRepo; // pode ser null
    }

    /** Conveniência: cria a entidade e delega para o lançamento “principal”. */
    public NotaId lancar(AlunoId aluno, SimuladoId simulado, DisciplinaId disciplina, double valor) {
        var n = new Nota(aluno, simulado, disciplina, valor, LocalDateTime.now());
        return lancar(n);
    }

    /**
     * Lançamento:
     * - RN-34: valida existência de aluno/simulado/disciplina/turma
     * - RN-31/32/33: aluno inativo não recebe nota; simulado deve estar EM_EDICAO; evita duplicidade
     * - RN-94: turma inativa não permite novas notas
     * - RN-98/RN-99: recalcula ranking após salvar
     */
    public NotaId lancar(Nota n) {
        // RN-34: garantir que IDs existem
        var aluno    = alunoRepo.porId(n.getAluno()).orElseThrow(() -> new IllegalStateException("aluno não encontrado"));
        var simulado = simuladoRepo.porId(n.getSimulado()).orElseThrow(() -> new IllegalStateException("simulado não encontrado"));
        disciplinaRepo.porId(n.getDisciplina()).orElseThrow(() -> new IllegalStateException("disciplina não encontrada"));
        var turma    = turmaRepo.porId(simulado.getTurma()).orElseThrow(() -> new IllegalStateException("turma não encontrada"));

        // RN-31/32/33: aluno inativo não recebe nota
        if (!aluno.isAtivo())
            throw new IllegalStateException("RN-31/RN-32/RN-33: aluno inativado não pode receber nota.");

        // RN-94: turma inativa não recebe novas notas
        if (!turma.isAtivo())
            throw new IllegalStateException("RN-94: turma inativada não permite novas notas/simulados.");

        // RN-32: simulado deve estar EM_EDICAO
        if (simulado.getStatus() != Simulado.Status.EM_EDICAO)
            throw new IllegalStateException("RN-32: Lançamento só com simulado EM_EDICAO.");

        // RN-33: evitar duplicidade (aluno+simulado+disciplina)
        if (repo.porChave(n.getAluno(), n.getSimulado(), n.getDisciplina()).isPresent())
            throw new IllegalStateException("RN-33: Nota duplicada para mesma disciplina/simulado/aluno.");

        var id = repo.salvar(n); // ORM atribui ID
        rankingServico.recalcular(n.getSimulado());
        return id;
    }

    /**
     * RN-37/38/39: retificar criando NOVA versão e registrando justificativa.
     * Retorna o ID da nova nota.
     */
    public NotaId retificarComJustificativa(NotaId idOriginal, double novoValor, String justificativa, ProfessorId professor) {
        var original = repo.porId(idOriginal).orElseThrow();

        // RN-39: simulado precisa estar EM_EDICAO
        var simulado = simuladoRepo.porId(original.getSimulado()).orElseThrow();
        if (simulado.getStatus() != Simulado.Status.EM_EDICAO)
            throw new IllegalStateException("RN-39: Retificação só permitida em simulado EM_EDICAO.");

        // RN-37: justificativa mínima
        String txt = justificativa == null ? "" : justificativa.trim();
        if (txt.length() < 20)
            throw new IllegalArgumentException("RN-37: Justificativa deve conter ao menos 20 caracteres.");

        // RN-38: cria nova versão (novo ID). Original preservada.
        var nova = new Nota(
                original.getAluno(),
                original.getSimulado(),
                original.getDisciplina(),
                novoValor,
                LocalDateTime.now()
        );
        var novaId = repo.salvar(nova);

        // histórico de justificativa (opcional)
        if (justificativaRepo != null) {
            Objects.requireNonNull(professor, "professor é obrigatório na retificação");
            var j = new Justificativa(
                    idOriginal,
                    original.getValor(),
                    novoValor,
                    txt,
                    LocalDateTime.now(),
                    professor
            );
            justificativaRepo.salvar(j);
        }

        rankingServico.recalcular(original.getSimulado());
        return novaId;
    }
}
