package dev.com.qnota.dominio.principal.simulado;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.turma.TurmaId;

public class Simulado {
    public enum Status { EM_EDICAO, FINALIZADO }

    private final SimuladoId id;
    private LocalDate dataAplicacao;
    private Status status;
    private TurmaId turma;
    private final List<DisciplinaPeso> disciplinas;

    public Simulado(SimuladoId id,
                    LocalDate dataAplicacao,
                    Status status,
                    TurmaId turma,
                    List<DisciplinaPeso> disciplinas) {

        this.id            = Objects.requireNonNull(id, "id não pode ser nulo");
        this.dataAplicacao = Objects.requireNonNull(dataAplicacao, "dataAplicacao não pode ser nula");
        this.status        = Objects.requireNonNull(status, "status não pode ser nulo");
        this.turma         = Objects.requireNonNull(turma, "turma não pode ser nula");

        var lista = new ArrayList<>(Objects.requireNonNull(disciplinas, "lista de disciplinas não pode ser nula"));
        validarDisciplinas(lista); // RN-12 (>=2), RN-13 (soma==10), RN-14B (sem repetição), null-safety
        this.disciplinas = lista;
    }

    // ===== getters =====
    public SimuladoId getId()                { return id; }
    public LocalDate getDataAplicacao()      { return dataAplicacao; }
    public Status getStatus()                { return status; }
    public TurmaId getTurma()                { return turma; }
    public List<DisciplinaPeso> getDisciplinas() { return Collections.unmodifiableList(disciplinas); }

    // ===== operações do agregado (regras locais) =====

    /** RN-14C é garantida aqui: não edita disciplinas se FINALIZADO. */
    public void alterarDisciplinas(List<DisciplinaPeso> novas) {
        if (this.status == Status.FINALIZADO)
            throw new IllegalStateException("RN-14C: Não é permitido editar simulado finalizado.");
        var lista = new ArrayList<>(Objects.requireNonNull(novas, "lista de disciplinas não pode ser nula"));
        validarDisciplinas(lista); // RN-12, RN-13, RN-14B
        this.disciplinas.clear();
        this.disciplinas.addAll(lista);
    }

    /** Troca de data é uma mudança local plausível. Regra transversal (se existisse) ficaria no serviço. */
    public void alterarData(LocalDate novaData) {
        this.dataAplicacao = Objects.requireNonNull(novaData, "dataAplicacao não pode ser nula");
    }

    /** Finalização do estado (RN-16 é tratada no serviço antes de chamar este método). */
    public void finalizar() {
        this.status = Status.FINALIZADO;
    }

    // ===== validações internas =====

    private static void validarDisciplinas(List<DisciplinaPeso> lista) {
        // null-safety de cada item e id
        for (DisciplinaPeso dp : lista) {
            if (dp == null) throw new IllegalArgumentException("DisciplinaPeso não pode ser nulo");
            if (dp.disciplina() == null) throw new IllegalArgumentException("DisciplinaId não pode ser nulo");
        }

        // RN-12: pelo menos duas disciplinas
        if (lista.size() < 2)
            throw new IllegalArgumentException("RN-12: Pelo menos duas disciplinas.");

        // RN-14B: disciplina não pode se repetir
        long distintos = lista.stream().map(DisciplinaPeso::disciplina).distinct().count();
        if (distintos != lista.size())
            throw new IllegalArgumentException("RN-14B: Disciplina não pode se repetir.");

        // RN-13: soma dos pesos == 10
        double soma = lista.stream().mapToDouble(DisciplinaPeso::peso).sum();
        if (Math.abs(soma - 10.0) > 1e-6)
            throw new IllegalArgumentException("RN-13: Pesos devem somar 10.");
    }

    // value object local
    public record DisciplinaPeso(DisciplinaId disciplina, double peso) {}
}
