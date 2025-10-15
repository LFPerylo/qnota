package dev.com.qnota.dominio.principal.simulado;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import dev.com.qnota.dominio.principal.disciplina.DisciplinaRepositorio;
import dev.com.qnota.dominio.principal.professor.ProfessorRepositorio;
import dev.com.qnota.dominio.principal.ranking.RankingServico;
import dev.com.qnota.dominio.principal.turma.TurmaId;
import dev.com.qnota.dominio.principal.turma.TurmaRepositorio;

public class SimuladoServico {
    private final SimuladoRepositorio repo;
    private final RankingServico rankingServico;
    private final TurmaRepositorio turmaRepo;
    private final ProfessorRepositorio professorRepo;
    private final DisciplinaRepositorio disciplinaRepo;

    public SimuladoServico(SimuladoRepositorio repo, RankingServico rankingServico, 
                          TurmaRepositorio turmaRepo, ProfessorRepositorio professorRepo,
                          DisciplinaRepositorio disciplinaRepo) {
        this.repo = Objects.requireNonNull(repo);
        this.rankingServico = Objects.requireNonNull(rankingServico);
        this.turmaRepo = Objects.requireNonNull(turmaRepo);
        this.professorRepo = Objects.requireNonNull(professorRepo);
        this.disciplinaRepo = Objects.requireNonNull(disciplinaRepo);
    }

    /** Factory de conveniência para criar EM_EDICAO sem expor ID. */
    public void criar(LocalDate dataAplicacao, TurmaId turma, List<Simulado.DisciplinaPeso> disciplinas) {
        var s = new Simulado(dataAplicacao, turma, disciplinas);
        criar(s);
    }

    /** Criação: RN-52 (máx. 2 em edição por turma). */
    public void criar(Simulado s) {
        // RN-96: não pode criar simulado em turma inativa
        var turma = turmaRepo.porId(s.getTurma()).orElseThrow(() -> new IllegalStateException("turma não encontrada"));
        if (!turma.isAtivo())
            throw new IllegalStateException("RN-96: Não é possível criar simulado em turma inativa.");
        
        // RN-53: compatibilidade área do professor x disciplinas
        var areasProfessor = professorRepo.nomesDeAreasDoProfessor(turma.getProfessor());
        var areasDisciplinas = s.getDisciplinas().stream()
                .map(dp -> {
                    var disciplina = disciplinaRepo.porId(dp.disciplina()).orElseThrow(() -> 
                        new IllegalStateException("disciplina não encontrada"));
                    return disciplina.getArea().nome();
                })
                .distinct()
                .toList();
        
        boolean temCompatibilidade = areasDisciplinas.stream()
                .anyMatch(area -> areasProfessor.contains(area));
        
        if (!temCompatibilidade) {
            throw new IllegalStateException("RN-53: Professor não possui especialidade compatível com as disciplinas do simulado.");
        }
        
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

    /** RN-15: excluir simulado só se não houver nota. */
    public void excluir(SimuladoId id) {
        if (repo.existeNotaParaSimulado(id))
            throw new IllegalStateException("RN-15: já existem notas lançadas.");
        repo.remover(id);
    }
}
