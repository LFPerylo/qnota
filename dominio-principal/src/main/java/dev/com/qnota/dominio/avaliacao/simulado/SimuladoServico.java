package dev.com.qnota.dominio.avaliacao.simulado;

import java.util.List;

import dev.com.qnota.dominio.avaliacao.ranking.RankingServico;

public class SimuladoServico {
    private final SimuladoRepositorio repo;
    private final RankingServico rankingServico;

    public SimuladoServico(SimuladoRepositorio repo, RankingServico rankingServico) {
        this.repo = repo;
        this.rankingServico = rankingServico;
    }

    public void criar(Simulado s) {
        if (s.getDisciplinas().size() < 2)
            throw new IllegalArgumentException("RN-12: Pelo menos duas disciplinas.");
        double soma = s.getDisciplinas().stream().mapToDouble(Simulado.DisciplinaPeso::peso).sum();
        if (Math.abs(soma - 10.0) > 1e-6)
            throw new IllegalArgumentException("RN-13: Pesos devem somar 10.");
        if (repo.contarEmEdicaoPorTurma(s.getTurma()) >= 2)
            throw new IllegalStateException("RN-52: Máximo de 2 simulados em edição por turma.");
        repo.salvar(s);
    }

    public void editarDisciplinas(SimuladoId id, List<Simulado.DisciplinaPeso> novas) {
        var s = repo.porId(id).orElseThrow();
        if (s.getStatus() == Simulado.Status.FINALIZADO)
            throw new IllegalStateException("RN-14C: Não é permitido editar simulado finalizado.");
        if (novas.stream().map(Simulado.DisciplinaPeso::disciplina).distinct().count() != novas.size())
            throw new IllegalArgumentException("RN-14B: Disciplina não pode se repetir.");
        double soma = novas.stream().mapToDouble(Simulado.DisciplinaPeso::peso).sum();
        if (Math.abs(soma - 10.0) > 1e-6)
            throw new IllegalArgumentException("RN-13: Pesos devem somar 10.");

        repo.salvar(new Simulado(s.getId(), s.getDataAplicacao(), s.getStatus(), s.getTurma(), novas));
        rankingServico.recalcular(id); // ranking dinâmico após mudanças de pesos
    }

    public void finalizar(SimuladoId id) {
        if (!repo.todasNotasLancadas(id))
            throw new IllegalStateException("RN-16: Todas as notas devem estar lançadas.");
        var s = repo.porId(id).orElseThrow();
        s.finalizar();
        repo.salvar(s);
        rankingServico.congelar(id); // RN-102
    }
}
