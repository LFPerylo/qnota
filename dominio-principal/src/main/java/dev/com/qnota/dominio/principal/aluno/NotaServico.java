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
        // RN-31: valor da nota deve estar entre 0 e 10
        if (valor < 0.0 || valor > 10.0) {
            throw new IllegalArgumentException("RN-31: Valor da nota deve estar entre 0 e 10.");
        }
        
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
        if (aluno.possuiNotaInterna(simuladoId, disciplinaId))
            throw new IllegalStateException("RN-33: Nota duplicada para mesma disciplina/simulado/aluno.");

        // Adiciona a nota ao agregado Aluno
        aluno.adicionarNotaInterna(simuladoId, disciplinaId, valor);
        alunoRepo.salvar(aluno);

        // Nota: Ranking será recalculado externamente pelo RankingServico
    }

    /**
     * RN-37/38/39: retificar nota diretamente no agregado Aluno.
     */
    public void retificarNota(AlunoId alunoId, SimuladoId simuladoId, DisciplinaId disciplinaId, 
                             double novoValor, String justificativa, ProfessorId professorId) {
        // RN-31: valor da nota deve estar entre 0 e 10
        if (novoValor < 0.0 || novoValor > 10.0) {
            throw new IllegalArgumentException("RN-31: Valor da nota deve estar entre 0 e 10.");
        }
        
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
        var notaExistente = aluno.obterNotaInterna(simuladoId, disciplinaId);
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

        aluno.retificarNotaInterna(simuladoId, disciplinaId, novoValor, novaJustificativa);
        alunoRepo.salvar(aluno);

        // Nota: Ranking será recalculado externamente pelo RankingServico
    }

    /**
     * RN-38: Adicionar justificativa a uma nota existente.
     */
    public void adicionarJustificativa(AlunoId alunoId, SimuladoId simuladoId, DisciplinaId disciplinaId, 
                                     String justificativa, ProfessorId professorId) {
        var aluno = alunoRepo.porId(alunoId);
        var simulado = simuladoRepo.porId(simuladoId);

        // RN-39: simulado precisa estar EM_EDICAO
        if (simulado.getStatus() != Simulado.Status.EM_EDICAO)
            throw new IllegalStateException("RN-39: Adição de justificativa só permitida em simulado EM_EDICAO.");

        // RN-37: justificativa mínima
        String txt = justificativa == null ? "" : justificativa.trim();
        if (txt.length() < 20)
            throw new IllegalArgumentException("RN-37: Justificativa deve conter ao menos 20 caracteres.");

        // Verifica se a nota existe
        var notaExistente = aluno.obterNotaInterna(simuladoId, disciplinaId);
        if (notaExistente.isEmpty())
            throw new IllegalStateException("Nota não encontrada para adicionar justificativa");

        // RN-38: cria nova justificativa
        var novaJustificativa = new Justificativa(
                notaExistente.get().getValor(),
                notaExistente.get().getValor(), // mesma nota
                txt,
                LocalDateTime.now(),
                professorId
        );

        aluno.adicionarJustificativaInterna(simuladoId, disciplinaId, novaJustificativa);
        alunoRepo.salvar(aluno);

        // Nota: Ranking será recalculado externamente pelo RankingServico
    }

    /**
     * Calcula a média ponderada de um aluno para um simulado específico.
     * Usado pelo RankingServico para calcular rankings.
     */
    public double calcularMediaPonderada(Aluno aluno, Map<Integer, Double> pesos) {
        double soma = 0, somaPesos = 0;

        for (var notaDoAluno : aluno.getNotas()) {
            double p = pesos.getOrDefault(notaDoAluno.getDisciplinaId().value(), 0.0);
            soma += notaDoAluno.getValor() * p;
            somaPesos += p;
        }

        double media = somaPesos == 0 ? 0 : (soma / somaPesos) * 10.0; // pesos somam 10
        return BigDecimal.valueOf(media).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
