package dev.com.qnota.dominio.principal.simulado;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import dev.com.qnota.dominio.principal.aluno.AlunoRepositorio;
import dev.com.qnota.dominio.principal.disciplina.Disciplina;
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
    private final AlunoRepositorio alunoRepo;
    private final FinalizacaoSimuladoTemplate finalizacaoTemplate;

    /**
     * Construtor de conveniência: cria o Template padrão de finalização.
     */
    public SimuladoServico(SimuladoRepositorio repo, RankingServico rankingServico,
                           TurmaRepositorio turmaRepo, ProfessorRepositorio professorRepo,
                           DisciplinaRepositorio disciplinaRepo, AlunoRepositorio alunoRepo) {
        this(repo, rankingServico, turmaRepo, professorRepo, disciplinaRepo, alunoRepo,
             criarTemplateFinalizacao(repo, rankingServico));
    }

    /**
     * Construtor principal: permite injetar uma implementação customizada
     * de FinalizacaoSimuladoTemplate, se necessário.
     */
    public SimuladoServico(SimuladoRepositorio repo, RankingServico rankingServico,
                           TurmaRepositorio turmaRepo, ProfessorRepositorio professorRepo,
                           DisciplinaRepositorio disciplinaRepo, AlunoRepositorio alunoRepo,
                           FinalizacaoSimuladoTemplate finalizacaoTemplate) {
        this.repo = Objects.requireNonNull(repo);
        this.rankingServico = Objects.requireNonNull(rankingServico);
        this.turmaRepo = Objects.requireNonNull(turmaRepo);
        this.professorRepo = Objects.requireNonNull(professorRepo);
        this.disciplinaRepo = Objects.requireNonNull(disciplinaRepo);
        this.alunoRepo = Objects.requireNonNull(alunoRepo);
        this.finalizacaoTemplate = Objects.requireNonNull(finalizacaoTemplate);
    }

    private static FinalizacaoSimuladoTemplate criarTemplateFinalizacao(SimuladoRepositorio repo,
                                                                        RankingServico rankingServico) {
        var template = new FinalizacaoSimuladoPadrao(repo);
        template.registrarObserver(rankingServico);
        return template;
    }

    /** Factory de conveniência para criar EM_EDICAO sem expor ID. */
    public SimuladoId criar(LocalDate dataAplicacao, TurmaId turma, List<Simulado.DisciplinaPeso> disciplinas) {
        var s = new Simulado(dataAplicacao, turma, disciplinas);
        return criar(s);
    }

    /** Criação: RN-52 (máx. 2 em edição por turma) + RN-96 + RN-12 + RN-53. */
    public SimuladoId criar(Simulado s) {
        // RN-12: pelo menos duas disciplinas
        if (s.getDisciplinas().size() < 2) {
            throw new IllegalArgumentException("RN-12: Pelo menos duas disciplinas.");
        }
        
        // RN-96: não pode criar simulado em turma inativa
        var turma = turmaRepo.porId(s.getTurma());
        if (!turma.isAtivo())
            throw new IllegalStateException("RN-96: Não é possível criar simulado em turma inativa.");

        // RN-53: compatibilidade de especialidades do professor com as áreas das disciplinas
        var areasProfessor = professorRepo.nomesDeAreasDoProfessor(turma.getProfessor());
        var areasDisciplinas = s.getDisciplinas().stream()
                .map(dp -> disciplinaRepo.porId(dp.disciplina()))
                .map(Disciplina::getArea)
                .map(Disciplina.AreaConhecimento::nome)
                .distinct()
                .toList();

        boolean temCompatibilidade = areasDisciplinas.stream().anyMatch(areasProfessor::contains);
        if (!temCompatibilidade)
            throw new IllegalStateException(
                "RN-53: Professor não possui especialidade compatível com as disciplinas do simulado.");

        if (repo.contarEmEdicaoPorTurma(s.getTurma()) >= 3)
            throw new IllegalStateException("RN-52: Máximo de 3 simulados em edição por turma.");

        return repo.salvar(s); // ORM atribui o ID se estiver nulo
    }

    /** Edição de disciplinas: RN-12/13/14B/14C (entidade) + RN-34 + RN-53 + recalcula ranking. */
    public void editarDisciplinas(SimuladoId id, List<Simulado.DisciplinaPeso> novas) {
        var s = repo.porId(id);

        // RN-14C: verificar se está finalizado primeiro
        if (s.getStatus() == Simulado.Status.FINALIZADO)
            throw new IllegalStateException("RN-14C: Não é permitido editar simulado finalizado.");

        // RN-34: validar existência das disciplinas informadas
        var disciplinasCarregadas = novas.stream()
                .map(dp -> disciplinaRepo.porId(dp.disciplina()))
                .toList();

        // RN-53: compatibilidade com o professor da turma do simulado
        var turma = turmaRepo.porId(s.getTurma());
        var areasProfessor = professorRepo.nomesDeAreasDoProfessor(turma.getProfessor());
        var areasDisciplinas = disciplinasCarregadas.stream()
                .map(Disciplina::getArea)
                .map(Disciplina.AreaConhecimento::nome)
                .distinct()
                .toList();

        boolean temCompatibilidade = areasDisciplinas.stream().anyMatch(areasProfessor::contains);
        if (!temCompatibilidade)
            throw new IllegalStateException(
                "RN-53: Professor não possui especialidade compatível com as disciplinas do simulado.");

        // RN-12: pelo menos duas disciplinas
        if (novas.size() < 2) {
            throw new IllegalArgumentException("RN-12: Pelo menos duas disciplinas.");
        }
        
        // RN-14B: disciplinas distintas
        long distintos = novas.stream().map(Simulado.DisciplinaPeso::disciplina).distinct().count();
        if (distintos != novas.size()) {
            throw new IllegalArgumentException("RN-14B: Disciplina não pode se repetir.");
        }
        
        // RN-13: pesos somam 10
        double soma = novas.stream().mapToDouble(Simulado.DisciplinaPeso::peso).sum();
        if (Math.abs(soma - 10.0) > 1e-6) {
            throw new IllegalArgumentException("RN-13: Pesos devem somar 10.");
        }
        
        // Agora alterar as disciplinas
        s.alterarDisciplinas(novas);

        repo.salvar(s);
        rankingServico.recalcular(id); // RN-98/99
    }

    /** Finalização: RN-16 e congelamento do ranking (RN-102). */
    /** Finalização: delega para o Template Method de finalização de simulados. */
    public void finalizar(SimuladoId id) {
        finalizacaoTemplate.finalizar(id);
    }

    /** RN-15: excluir simulado só se não houver nota. */
    public void excluir(SimuladoId id) {
        if (alunoRepo.existeNotaParaSimulado(id))
            throw new IllegalStateException("RN-15: já existem notas lançadas.");
        repo.remover(id);
    }
}
