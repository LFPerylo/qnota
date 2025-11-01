package dev.com.qnota.dominio.principal.aluno;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaRepositorio;
import dev.com.qnota.dominio.principal.professor.ProfessorId;
import dev.com.qnota.dominio.principal.simulado.Simulado;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.simulado.SimuladoRepositorio;
import dev.com.qnota.dominio.principal.turma.TurmaRepositorio;

/** Serviço de aplicação para operações de Nota dentro do agregado Aluno. */
public class NotaServico {

    private final AlunoRepositorio alunoRepo;
    private final SimuladoRepositorio simuladoRepo;
    private final TurmaRepositorio turmaRepo;
    private final DisciplinaRepositorio disciplinaRepo;

    public NotaServico(AlunoRepositorio alunoRepo,
                       SimuladoRepositorio simuladoRepo,
                       TurmaRepositorio turmaRepo,
                       DisciplinaRepositorio disciplinaRepo) {
        this.alunoRepo = Objects.requireNonNull(alunoRepo);
        this.simuladoRepo = Objects.requireNonNull(simuladoRepo);
        this.turmaRepo = Objects.requireNonNull(turmaRepo);
        this.disciplinaRepo = Objects.requireNonNull(disciplinaRepo);
    }

    public void lancar(AlunoId aluno, SimuladoId simulado, DisciplinaId disciplina, double valor) {
        lancarNota(aluno, simulado, disciplina, valor);
    }

    public void lancarNota(AlunoId alunoId, SimuladoId simuladoId, DisciplinaId disciplinaId, double valor) {
        if (valor < 0.0 || valor > 10.0)
            throw new IllegalArgumentException("RN-31: Valor da nota deve estar entre 0 e 10.");

        var aluno = alunoRepo.porId(alunoId);
        var simulado = simuladoRepo.porId(simuladoId);
        disciplinaRepo.porId(disciplinaId);
        var turma = turmaRepo.porId(simulado.getTurma());

        if (!aluno.isAtivo())
            throw new IllegalStateException("RN-31/32/33: aluno inativado não pode receber nota.");
        if (!turma.isAtivo())
            throw new IllegalStateException("RN-94: turma inativada não permite novas notas/simulados.");
        if (simulado.getStatus() != Simulado.Status.EM_EDICAO)
            throw new IllegalStateException("RN-32: Lançamento só com simulado EM_EDICAO.");
        if (aluno.possuiNotaInterna(simuladoId, disciplinaId))
            throw new IllegalStateException("RN-33: Nota duplicada para mesma disciplina/simulado/aluno.");

        aluno.adicionarNotaInterna(simuladoId, disciplinaId, valor);
        alunoRepo.salvar(aluno);
    }

    public void retificarNota(AlunoId alunoId, SimuladoId simuladoId, DisciplinaId disciplinaId,
                              double novoValor, String justificativa, ProfessorId professorId) {

        if (novoValor < 0.0 || novoValor > 10.0)
            throw new IllegalArgumentException("RN-31: Valor da nota deve estar entre 0 e 10.");

        var aluno = alunoRepo.porId(alunoId);
        var simulado = simuladoRepo.porId(simuladoId);

        if (simulado.getStatus() != Simulado.Status.EM_EDICAO)
            throw new IllegalStateException("RN-39: Retificação só em simulado EM_EDICAO.");

        String txt = justificativa == null ? "" : justificativa.trim();
        if (txt.length() < 20)
            throw new IllegalArgumentException("RN-37: Justificativa deve conter ao menos 20 caracteres.");

        var notaExistente = aluno.obterNotaInterna(simuladoId, disciplinaId)
                .orElseThrow(() -> new IllegalStateException("Nota não encontrada para retificação"));

        var novaJust = new Justificativa(
                notaExistente.getValor(), novoValor, txt, LocalDateTime.now(), professorId);

        aluno.retificarNotaInterna(simuladoId, disciplinaId, novoValor, novaJust);
        alunoRepo.salvar(aluno);
    }

    public void adicionarJustificativa(AlunoId alunoId, SimuladoId simuladoId, DisciplinaId disciplinaId,
                                       String justificativa, ProfessorId professorId) {

        var aluno = alunoRepo.porId(alunoId);
        var simulado = simuladoRepo.porId(simuladoId);

        if (simulado.getStatus() != Simulado.Status.EM_EDICAO)
            throw new IllegalStateException("RN-39: Adição de justificativa só em simulado EM_EDICAO.");

        String txt = justificativa == null ? "" : justificativa.trim();
        if (txt.length() < 20)
            throw new IllegalArgumentException("RN-37: Justificativa deve conter ao menos 20 caracteres.");

        var nota = aluno.obterNotaInterna(simuladoId, disciplinaId)
                .orElseThrow(() -> new IllegalStateException("Nota não encontrada para adicionar justificativa"));

        var novaJust = new Justificativa(nota.getValor(), nota.getValor(), txt, LocalDateTime.now(), professorId);
        aluno.adicionarJustificativaInterna(simuladoId, disciplinaId, novaJust);
        alunoRepo.salvar(aluno);
    }

    /** Utilitário usado pelo RankingService. */
    public double calcularMediaPonderada(Aluno aluno, Map<Integer, Double> pesos) {
        double soma = 0, somaPesos = 0;
        for (var n : aluno.getNotas()) {
            double p = pesos.getOrDefault(n.getDisciplinaId().value(), 0.0);
            soma += n.getValor() * p;
            somaPesos += p;
        }
        double media = somaPesos == 0 ? 0 : (soma / somaPesos) * 10.0; // pesos somam 10
        return BigDecimal.valueOf(media).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
