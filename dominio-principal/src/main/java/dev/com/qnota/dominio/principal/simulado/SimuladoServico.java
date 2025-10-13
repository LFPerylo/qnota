package dev.com.qnota.dominio.principal.simulado;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import dev.com.qnota.dominio.principal.ranking.RankingServico;
import dev.com.qnota.dominio.principal.turma.TurmaId;

public class SimuladoServico {
    private final SimuladoRepositorio repo;
    private final RankingServico rankingServico;

    public SimuladoServico(SimuladoRepositorio repo, RankingServico rankingServico) {
        this.repo = Objects.requireNonNull(repo);
        this.rankingServico = Objects.requireNonNull(rankingServico);
    }

    /** Factory de conveniência para criar EM_EDICAO sem expor ID. */
    public void criar(LocalDate dataAplicacao, TurmaId turma, List<Simulado.DisciplinaPeso> disciplinas) {
        var s = new Simulado(dataAplicacao, turma, disciplinas);
        criar(s);
    }

    /** Criação: RN-52 (máx. 2 em edição por turma). */
    public void criar(Simulado s) {
        if (repo.contarEmEdicaoPorTurma(s.getTurma()) >= 2)
            throw new IllegalStateException("RN-52: Máximo de 2 simulados em edição por turma.");
        repo.salvar(s); // repo atribui o ID se estiver nulo
    }

    /** Edição de disciplinas (RN-12/13/14B/14C na entidade) + recalcula ranking. */
    public void editarDisciplinas(SimuladoId id, List<Simulado.DisciplinaPeso> novas) {
        var s = repo.porId(id).orElseThrow();
        s.alterarDisciplinas(novas);
        repo.salvar(s);
        rankingServico.recalcular(id); // RN-98/99
    }

    /** Finalização: RN-16 e congelamento do ranking (RN-102). */
    public void finalizar(SimuladoId id) {
        if (!repo.todasNotasLancadas(id))
            throw new IllegalStateException("RN-16: Todas as notas devem estar lançadas.");
        var s = repo.porId(id).orElseThrow();
        s.finalizar();
        repo.salvar(s);
        rankingServico.congelar(id); // RN-102
    }
}
