package dev.com.qnota.dominio.principal.nota;

import java.time.LocalDateTime;
import java.util.Objects;

import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;

public class Nota {

    private final NotaId id;                 // pode ser nulo no lançamento (gerado no repo)
    private final AlunoId aluno;             // NOT NULL
    private final SimuladoId simulado;       // NOT NULL
    private final DisciplinaId disciplina;   // NOT NULL
    private double valor;                    // 0..10
    private final LocalDateTime dataLancamento; // NOT NULL (carimbo de quem lançou)

    public Nota(NotaId id,
                AlunoId aluno,
                SimuladoId simulado,
                DisciplinaId disciplina,
                double valor,
                LocalDateTime dataLancamento) {

        // id pode ser nulo; demais não.
        this.id         = id;
        this.aluno      = Objects.requireNonNull(aluno,     "'aluno' não pode ser nulo");
        this.simulado   = Objects.requireNonNull(simulado,  "'simulado' não pode ser nulo");
        this.disciplina = Objects.requireNonNull(disciplina,"'disciplina' não pode ser nula");
        this.dataLancamento = Objects.requireNonNull(dataLancamento, "'dataLancamento' não pode ser nulo");

        validarFaixa(valor);
        this.valor = valor;
    }

    // getters
    public NotaId getId()               { return id; }
    public AlunoId getAluno()           { return aluno; }
    public SimuladoId getSimulado()     { return simulado; }
    public DisciplinaId getDisciplina() { return disciplina; }
    public double getValor()            { return valor; }
    public LocalDateTime getDataLancamento() { return dataLancamento; }

    /** Retificação local do valor (regra de faixa é do agregado). */
    public void alterar(double novoValor) {
        validarFaixa(novoValor);
        this.valor = novoValor;
    }

    private static void validarFaixa(double v) {
        if (v < 0.0 || v > 10.0)
            throw new IllegalArgumentException("RN-31: Valor da nota deve estar entre 0 e 10.");
    }
}
