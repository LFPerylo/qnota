package dev.com.qnota.dominio.principal.nota;

import java.time.LocalDateTime;

import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;

public class Nota {
    private final NotaId id;
    private final AlunoId aluno;
    private final SimuladoId simulado;
    private final DisciplinaId disciplina;
    private double valor;
    private LocalDateTime dataLancamento;

    public Nota(NotaId id, AlunoId aluno, SimuladoId simulado, DisciplinaId disciplina, double valor, LocalDateTime dataLancamento) {
        this.id = id;
        this.aluno = aluno;
        this.simulado = simulado;
        this.disciplina = disciplina;
        this.valor = valor;
        this.dataLancamento = dataLancamento;
    }

    public NotaId getId() { return id; }
    public AlunoId getAluno() { return aluno; }
    public SimuladoId getSimulado() { return simulado; }
    public DisciplinaId getDisciplina() { return disciplina; }
    public double getValor() { return valor; }
    public LocalDateTime getDataLancamento() { return dataLancamento; }

    public void alterar(double novoValor) { this.valor = novoValor; }
}
