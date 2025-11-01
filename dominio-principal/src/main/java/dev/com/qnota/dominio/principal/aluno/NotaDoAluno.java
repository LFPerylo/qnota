package dev.com.qnota.dominio.principal.aluno;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;

/** Entidade local do agregado Aluno (PK composta: AlunoId implícito, SimuladoId, DisciplinaId) */
public class NotaDoAluno {

    private final SimuladoId simuladoId;
    private final DisciplinaId disciplinaId;
    private final LocalDateTime dataLancamento;
    private final List<Justificativa> justificativas;
    private final double valor;

    public NotaDoAluno(SimuladoId simuladoId,
                       DisciplinaId disciplinaId,
                       double valor,
                       LocalDateTime dataLancamento,
                       List<Justificativa> justificativas) {
        this.simuladoId = Objects.requireNonNull(simuladoId);
        this.disciplinaId = Objects.requireNonNull(disciplinaId);
        this.dataLancamento = Objects.requireNonNull(dataLancamento);
        this.valor = valor;
        this.justificativas = justificativas != null
                ? Collections.unmodifiableList(justificativas)
                : Collections.emptyList();
    }

    public SimuladoId getSimuladoId() { return simuladoId; }
    public DisciplinaId getDisciplinaId() { return disciplinaId; }
    public double getValor() { return valor; }
    public LocalDateTime getDataLancamento() { return dataLancamento; }
    public List<Justificativa> getJustificativas() { return justificativas; }

    public NotaDoAluno adicionarJustificativa(Justificativa nova) {
        var novas = new java.util.ArrayList<>(this.justificativas);
        novas.add(Objects.requireNonNull(nova));
        return new NotaDoAluno(simuladoId, disciplinaId, valor, dataLancamento, novas);
    }

    public NotaDoAluno alterarValor(double novoValor) {
        return new NotaDoAluno(simuladoId, disciplinaId, novoValor, dataLancamento, justificativas);
    }

    public boolean temJustificativas() { return !justificativas.isEmpty(); }
    public int quantidadeJustificativas() { return justificativas.size(); }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotaDoAluno that)) return false;
        return simuladoId.equals(that.simuladoId) && disciplinaId.equals(that.disciplinaId);
    }
    @Override public int hashCode() { return Objects.hash(simuladoId, disciplinaId); }
}
