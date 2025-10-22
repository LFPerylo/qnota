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

    // ID gerado na infraestrutura (repositório)
    private SimuladoId id;

    private LocalDate dataAplicacao;
    private Status status;
    private TurmaId turma;
    private final List<DisciplinaPeso> disciplinas;

    /** Constrói um simulado EM_EDICAO (mais comum). */
    public Simulado(LocalDate dataAplicacao, TurmaId turma, List<DisciplinaPeso> disciplinas) {
        this(dataAplicacao, Status.EM_EDICAO, turma, disciplinas);
    }

    /** Constrói permitindo informar o status inicial (sem ID). */
    public Simulado(LocalDate dataAplicacao, Status status, TurmaId turma, List<DisciplinaPeso> disciplinas) {
        this.dataAplicacao = Objects.requireNonNull(dataAplicacao, "dataAplicacao não pode ser nula");
        this.status        = Objects.requireNonNull(status,        "status não pode ser nulo");
        this.turma         = Objects.requireNonNull(turma,         "turma não pode ser nula");

        var lista = new ArrayList<>(Objects.requireNonNull(disciplinas, "lista de disciplinas não pode ser nula"));
        validarDisciplinas(lista); // RN-12, RN-13, RN-14B + null-safety
        this.disciplinas = new ArrayList<>(lista); // cópia defensiva
    }

    /** Infra chama para fixar o ID gerado. Não permite reatribuição divergente. */
    public void atribuirIdSeAusente(SimuladoId novoId) {
        Objects.requireNonNull(novoId, "'id' não pode ser nulo");
        if (this.id != null && !this.id.equals(novoId)) {
            throw new IllegalStateException("ID já atribuído para este simulado");
        }
        this.id = novoId;
    }

    // ===== getters =====
    public SimuladoId getId()                     { return id; }
    public LocalDate getDataAplicacao()           { return dataAplicacao; }
    public Status getStatus()                     { return status; }
    public TurmaId getTurma()                     { return turma; }
    public List<DisciplinaPeso> getDisciplinas()  { return Collections.unmodifiableList(disciplinas); }

    // ===== operações do agregado (regras locais) =====

    /** Altera disciplinas do simulado. */
    public void alterarDisciplinas(List<DisciplinaPeso> novas) {
        var lista = new ArrayList<>(Objects.requireNonNull(novas, "lista de disciplinas não pode ser nula"));
        validarDisciplinas(lista);
        this.disciplinas.clear();
        this.disciplinas.addAll(lista);
    }

    /** Troca de data — regra transversal (se houver) fica no serviço. */
    public void alterarData(LocalDate novaData) {
        this.dataAplicacao = Objects.requireNonNull(novaData, "dataAplicacao não pode ser nula");
    }

    /** Finaliza o simulado. (RN-16 é checada no serviço antes de chamar) */
    public void finalizar() {
        this.status = Status.FINALIZADO;
    }

    // ===== validações internas =====
    private static void validarDisciplinas(List<DisciplinaPeso> lista) {
        // null-safety
        for (DisciplinaPeso dp : lista) {
            if (dp == null) throw new IllegalArgumentException("DisciplinaPeso não pode ser nulo");
            if (dp.disciplina() == null) throw new IllegalArgumentException("DisciplinaId não pode ser nulo");
        }
    }

    // Value Object local
    public record DisciplinaPeso(DisciplinaId disciplina, double peso) {}
}
