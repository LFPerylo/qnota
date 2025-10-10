package dev.com.qnota.dominio.principal.simulado;

import java.util.List;

import dev.com.qnota.dominio.principal.ranking.RankingServico;

public class SimuladoServico {
    private final SimuladoRepositorio repo;
    private final RankingServico rankingServico;

    public SimuladoServico(SimuladoRepositorio repo, RankingServico rankingServico) {
        this.repo = repo;
        this.rankingServico = rankingServico;
    }

    /** Criação: valida regra transversal de limite por turma (RN-52). 
     *  As invariantes locais (RN-12/13/14B) já são garantidas pela entidade. */
    public void criar(Simulado s) {
        if (repo.contarEmEdicaoPorTurma(s.getTurma()) >= 2)
            throw new IllegalStateException("RN-52: Máximo de 2 simulados em edição por turma.");
        repo.salvar(s);
    }

    /** Edição de disciplinas: entidade cuida de RN-14B/14C/12/13; serviço recalcula o ranking. */
    public void editarDisciplinas(SimuladoId id, List<Simulado.DisciplinaPeso> novas) {
        var s = repo.porId(id).orElseThrow();
        s.alterarDisciplinas(novas);
        repo.salvar(s);
        rankingServico.recalcular(id); // ranking dinâmico após mudanças de pesos
    }

    /** Finalização: checa RN-16 (todas as notas lançadas) e congela ranking. */
    public void finalizar(SimuladoId id) {
        if (!repo.todasNotasLancadas(id))
            throw new IllegalStateException("RN-16: Todas as notas devem estar lançadas.");
        var s = repo.porId(id).orElseThrow();
        s.finalizar();
        repo.salvar(s);
        rankingServico.congelar(id); // RN-102
    }
}
