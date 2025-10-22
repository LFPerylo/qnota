package dev.com.qnota.dominio.principal.aluno;

import java.time.LocalDateTime;
import java.util.Objects;

import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaRepositorio;
import dev.com.qnota.dominio.principal.professor.ProfessorId;
import dev.com.qnota.dominio.principal.ranking.RankingServico;
import dev.com.qnota.dominio.principal.simulado.Simulado;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.simulado.SimuladoRepositorio;
import dev.com.qnota.dominio.principal.turma.TurmaRepositorio;

public class NotaServico {

    private final RankingServico rankingServico;
    private final AlunoRepositorio alunoRepo;
    private final SimuladoRepositorio simuladoRepo;
    private final TurmaRepositorio turmaRepo;
    private final DisciplinaRepositorio disciplinaRepo;

    public NotaServico(RankingServico rankingServico,
                       AlunoRepositorio alunoRepo,
                       SimuladoRepositorio simuladoRepo,
                       TurmaRepositorio turmaRepo,
                       DisciplinaRepositorio disciplinaRepo) {
        this.rankingServico = Objects.requireNonNull(rankingServico);
        this.alunoRepo = Objects.requireNonNull(alunoRepo);
        this.simuladoRepo = Objects.requireNonNull(simuladoRepo);
        this.turmaRepo = Objects.requireNonNull(turmaRepo);
        this.disciplinaRepo = Objects.requireNonNull(disciplinaRepo);
    }

    /** Conveniência: cria a entidade e delega para o lançamento "principal". */
    public void lancar(AlunoId aluno, SimuladoId simulado, DisciplinaId disciplina, double valor) {
        lancarNota(aluno, simulado, disciplina, valor);
    }

    /**
     * Lançamento de nota diretamente no agregado Aluno.
     * - RN-34: valida existência de aluno/simulado/disciplina/turma
     * - RN-31/32/33: aluno inativo não recebe nota; simulado deve estar EM_EDICAO; evita duplicidade
     * - RN-94: turma inativa não permite novas notas
     * - RN-98/RN-99: recalcula ranking após salvar
     */
    public void lancarNota(AlunoId alunoId, SimuladoId simuladoId, DisciplinaId disciplinaId, double valor) {
        // RN-34: garantir que IDs existem
        var aluno    = alunoRepo.porId(alunoId);
        var simulado = simuladoRepo.porId(simuladoId);
        disciplinaRepo.porId(disciplinaId);
        var turma    = turmaRepo.porId(simulado.getTurma());

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
        if (aluno.possuiNota(simuladoId, disciplinaId))
            throw new IllegalStateException("RN-33: Nota duplicada para mesma disciplina/simulado/aluno.");

        // Adiciona a nota ao agregado Aluno
        aluno.adicionarNota(simuladoId, disciplinaId, valor);
        alunoRepo.salvar(aluno);

        rankingServico.recalcular(simuladoId);
    }

    /**
     * RN-37/38/39: retificar nota diretamente no agregado Aluno.
     */
    public void retificarNota(AlunoId alunoId, SimuladoId simuladoId, DisciplinaId disciplinaId, 
                             double novoValor, String justificativa, ProfessorId professorId) {
        var aluno = alunoRepo.porId(alunoId);
        var simulado = simuladoRepo.porId(simuladoId);

        // RN-39: simulado precisa estar EM_EDICAO
        if (simulado.getStatus() != Simulado.Status.EM_EDICAO)
            throw new IllegalStateException("RN-39: Retificação só permitida em simulado EM_EDICAO.");

        // RN-37: justificativa mínima
        String txt = justificativa == null ? "" : justificativa.trim();
        if (txt.length() < 20)
            throw new IllegalArgumentException("RN-37: Justificativa deve conter ao menos 20 caracteres.");

        // Verifica se a nota existe
        var notaExistente = aluno.obterNota(simuladoId, disciplinaId);
        if (notaExistente.isEmpty())
            throw new IllegalStateException("Nota não encontrada para retificação");

        // RN-38: cria nova versão da nota com justificativa
        var novaJustificativa = new Justificativa(
                notaExistente.get().getValor(),
                novoValor,
                txt,
                LocalDateTime.now(),
                professorId
        );

        aluno.retificarNota(simuladoId, disciplinaId, novoValor, novaJustificativa);
        alunoRepo.salvar(aluno);

        rankingServico.recalcular(simuladoId);
    }
}
