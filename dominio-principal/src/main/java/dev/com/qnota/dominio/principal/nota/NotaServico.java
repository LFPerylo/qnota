package dev.com.qnota.dominio.principal.nota;

import java.time.LocalDateTime;
import java.util.Objects;

import dev.com.qnota.dominio.principal.aluno.AlunoId;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.ranking.RankingServico;

public class NotaServico {

    private final NotaRepositorio repo;
    private final RankingServico rankingServico;

    public NotaServico(NotaRepositorio repo, RankingServico rankingServico) {
        this.repo = Objects.requireNonNull(repo);
        this.rankingServico = Objects.requireNonNull(rankingServico);
    }

    /** Conveniência: cria e lança uma nova nota já com timestamp. */
    public void lancar(AlunoId aluno, SimuladoId simulado, DisciplinaId disciplina, double valor) {
        var agora = LocalDateTime.now();
        var n = new Nota(null, aluno, simulado, disciplina, valor, agora);
        lancar(n); // delega para a regra central abaixo
    }

    /**
     * Regra central de lançamento:
     * - RN-32: só quando o simulado está EM_EDICAO (coordena com repo)
     * - RN-31: faixa [0..10] já validada pela entidade Nota
     * - RN-33: evita duplicidade (aluno+simulado+disciplina)
     * - RN-98/RN-99: recalcula ranking após salvar
     */
    public void lancar(Nota n) {
        if (!repo.simuladoEstaEmEdicao(n.getSimulado()))
            throw new IllegalStateException("RN-32: Lançamento só com simulado EM_EDICAO.");
        if (repo.porChave(n.getAluno(), n.getSimulado(), n.getDisciplina()).isPresent())
            throw new IllegalStateException("RN-33: Nota duplicada para mesma disciplina/simulado/aluno.");

        repo.salvar(n);
        rankingServico.recalcular(n.getSimulado()); // RN-98 e RN-99
    }

    /**
     * Retificação:
     * - RN-39: só é permitido em simulado EM_EDICAO
     * - RN-31: faixa [0..10] na entidade
     * - RN-98/RN-99: recalcula ranking
     */
    public void retificar(NotaId id, double novoValor) {
        var n = repo.porId(id).orElseThrow();
        if (!repo.simuladoEstaEmEdicao(n.getSimulado()))
            throw new IllegalStateException("RN-39: Retificação só permitida em simulado EM_EDICAO.");

        n.alterar(novoValor); // valida RN-31
        repo.salvar(n);
        rankingServico.recalcular(n.getSimulado()); // RN-98 e RN-99
    }
}
