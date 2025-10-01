package dev.com.qnota.dominio.avaliacao.simulado;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.com.qnota.dominio.academico.turma.TurmaId;
import dev.com.qnota.dominio.avaliacao.disciplina.DisciplinaId;

public class Simulado {
    public enum Status { EM_EDICAO, FINALIZADO }

    private final SimuladoId id;
    private LocalDate dataAplicacao;
    private Status status;
    private TurmaId turma;
    private final List<DisciplinaPeso> disciplinas;

    public Simulado(SimuladoId id, LocalDate dataAplicacao, Status status, TurmaId turma, List<DisciplinaPeso> disciplinas) {
        this.id = id;
        this.dataAplicacao = dataAplicacao;
        this.status = status;
        this.turma = turma;
        this.disciplinas = new ArrayList<>(disciplinas == null ? List.of() : disciplinas);
    }

    public SimuladoId getId() { return id; }
    public LocalDate getDataAplicacao() { return dataAplicacao; }
    public Status getStatus() { return status; }
    public TurmaId getTurma() { return turma; }
    public List<DisciplinaPeso> getDisciplinas() { return Collections.unmodifiableList(disciplinas); }

    public void finalizar() { this.status = Status.FINALIZADO; }

    public record DisciplinaPeso(DisciplinaId disciplina, double peso) {}
}
