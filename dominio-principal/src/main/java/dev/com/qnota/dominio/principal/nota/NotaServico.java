package dev.com.qnota.dominio.principal.nota;

import dev.com.qnota.dominio.principal.ranking.RankingServico;

public class NotaServico {
    private final NotaRepositorio repo;
    private final RankingServico rankingServico;

    public NotaServico(NotaRepositorio repo, RankingServico rankingServico) {
        this.repo = repo;
        this.rankingServico = rankingServico;
    }

    public void lancar(Nota n) {
        if (!repo.simuladoEstaEmEdicao(n.getSimulado()))
            throw new IllegalStateException("RN-32: Lançamento só com simulado EM_EDICAO.");
        if (n.getValor() < 0 || n.getValor() > 10)
            throw new IllegalArgumentException("RN-31: Valor da nota deve estar entre 0 e 10.");
        if (repo.porChave(n.getAluno(), n.getSimulado(), n.getDisciplina()).isPresent())
            throw new IllegalStateException("RN-33: Nota duplicada para mesma disciplina/simulado/aluno.");

        repo.salvar(n);
        rankingServico.recalcular(n.getSimulado()); // RN-98 e RN-99
    }

    public void retificar(NotaId id, double novoValor) {
        var n = repo.porId(id).orElseThrow();
        if (!repo.simuladoEstaEmEdicao(n.getSimulado()))
            throw new IllegalStateException("RN-39: Retificação só permitida em simulado EM_EDICAO.");
        if (novoValor < 0 || novoValor > 10)
            throw new IllegalArgumentException("RN-31: Valor da nota deve estar entre 0 e 10.");

        n.alterar(novoValor);
        repo.salvar(n);
        rankingServico.recalcular(n.getSimulado()); // RN-98 e RN-99
    }
}
