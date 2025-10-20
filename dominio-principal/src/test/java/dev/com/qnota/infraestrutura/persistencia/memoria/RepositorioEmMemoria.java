package dev.com.qnota.infraestrutura.persistencia.memoria;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import dev.com.qnota.dominio.principal.aluno.*;
import dev.com.qnota.dominio.principal.responsavel.*;
import dev.com.qnota.dominio.principal.professor.*;
import dev.com.qnota.dominio.principal.turma.*;
import dev.com.qnota.dominio.principal.disciplina.*;
import dev.com.qnota.dominio.principal.simulado.*;
import dev.com.qnota.dominio.principal.nota.*;
import dev.com.qnota.dominio.principal.justificativa.*;
import dev.com.qnota.dominio.principal.ranking.*;

/** Repositório em memória para testes. Implementa TODAS as interfaces do subdomínio principal. */
public class RepositorioEmMemoria implements
        AlunoRepositorio,
        ResponsavelRepositorio,
        ProfessorRepositorio,
        TurmaRepositorio,
        DisciplinaRepositorio,
        SimuladoRepositorio,
        NotaRepositorio,
        JustificativaRepositorio,
        RankingRepositorio {

    // ---------- storage ----------
    private final Map<Integer, Aluno> alunos = new ConcurrentHashMap<>();
    private final Map<Integer, Responsavel> responsaveis = new ConcurrentHashMap<>();
    private final Map<Integer, Professor> professores = new ConcurrentHashMap<>();
    private final Map<Integer, Turma> turmas = new ConcurrentHashMap<>();
    private final Map<Integer, Disciplina> disciplinas = new ConcurrentHashMap<>();
    private final Map<Integer, Simulado> simulados = new ConcurrentHashMap<>();
    private final Map<Integer, Nota> notas = new ConcurrentHashMap<>();
    private final Map<Integer, List<Justificativa>> justificativasPorNota = new ConcurrentHashMap<>();

    // auxiliares
    private final Map<Integer, Map<Integer, Double>> pesosPorSimulado = new HashMap<>();
    private final Set<Integer> simuladoComTodasNotasLancadas = new HashSet<>();

    // ranking (snapshot opcional para testes)
    private final Map<Integer, List<ItemRanking>> rankingPorSimulado = new HashMap<>();
    private final Set<Integer> rankingCongelado = new HashSet<>();

    // ---------- seqs de ID (infra) ----------
    private int seqAluno = 1;
    private int seqResponsavel = 1;
    private int seqProfessor = 1;
    private int seqTurma = 1;
    private int seqSimulado = 1;
    private int seqDisciplina = 1;
    private int seqNota = 1;
    private int seqJustificativa = 1;

    // =========================================================
    // =============== AlunoRepositorio ========================
    // =========================================================
    @Override
    public AlunoId salvar(Aluno a) {
        if (a.getId() == null) {
            a.atribuirIdSeAusente(new AlunoId(seqAluno++));
        }
        alunos.put(a.getId().value(), a);
        return a.getId();
    }

    @Override public Optional<Aluno> porId(AlunoId id) { return Optional.ofNullable(alunos.get(id.value())); }
    @Override public void remover(AlunoId id) { alunos.remove(id.value()); }

    @Override
    public boolean existeOutroComMesmoNomeENascimentoNaTurma(String nome, LocalDate data, TurmaId turmaId) {
        return alunos.values().stream().anyMatch(a ->
                a.getNome().equalsIgnoreCase(nome)
                        && a.getDataNascimento().equals(data)
                        && a.getTurma().equals(turmaId));
    }

    @Override public int contarResponsaveis(AlunoId id) { return porId(id).map(a -> a.getResponsaveis().size()).orElse(0); }
    @Override public boolean existeVinculo(AlunoId id) { return alunos.containsKey(id.value()); }

    @Override
    public List<Aluno> porTurma(TurmaId turmaId) {
        return alunos.values().stream().filter(a -> a.getTurma().equals(turmaId)).toList();
    }

    @Override
    public boolean temNotasPendentesEmSimuladosEmEdicao(AlunoId alunoId) {
        var a = alunos.get(alunoId.value());
        if (a == null) return false;
        return simulados.values().stream()
                .filter(s -> s.getTurma().equals(a.getTurma()))
                .anyMatch(s -> s.getStatus() == Simulado.Status.EM_EDICAO
                        && !simuladoComTodasNotasLancadas.contains(s.getId().value()));
    }

    @Override public boolean temNotas(AlunoId alunoId) {
        return notas.values().stream().anyMatch(n -> n.getAluno().equals(alunoId));
    }

    @Override
    public boolean possuiSimuladoFinalizado(AlunoId alunoId) {
        var a = alunos.get(alunoId.value());
        if (a == null) return false;
        var turmaDoAluno = a.getTurma();
        return simulados.values().stream()
                .anyMatch(s -> s.getTurma().equals(turmaDoAluno)
                        && s.getStatus() == Simulado.Status.FINALIZADO);
    }

    @Override
    public void alterarTurma(AlunoId alunoId, TurmaId novaTurmaId) {
        var a = alunos.get(alunoId.value());
        if (a != null) {
            a.mudarTurma(novaTurmaId);
            alunos.put(a.getId().value(), a);
        }
    }

    // =========================================================
    // =============== ResponsavelRepositorio ==================
    // =========================================================

    // guarda CPFs NORMALIZADOS (apenas dígitos)
    private final Set<String> cpfsResponsavel = new HashSet<>();
    private static String normCpf(String s) { return s == null ? null : s.replaceAll("\\D", ""); }

    @Override
    public ResponsavelId salvar(Responsavel r) {
        if (r.getId() == null) {
            r.atribuirIdSeAusente(new ResponsavelId(seqResponsavel++));
        }
        responsaveis.put(r.getId().value(), r);
        cpfsResponsavel.add(normCpf(r.getCpf())); // normalizado
        return r.getId();
    }

    @Override public Optional<Responsavel> porId(ResponsavelId id) { return Optional.ofNullable(responsaveis.get(id.value())); }
    @Override public boolean cpfExiste(String cpf) { return cpfsResponsavel.contains(normCpf(cpf)); }

    @Override
    public void atualizarContato(ResponsavelId id, String novoNome, String novoEmail) {
        var r = responsaveis.get(id.value());
        if (r == null) return;
        r.renomear(novoNome);
        r.alterarEmail(novoEmail);
        responsaveis.put(id.value(), r);
    }

    @Override
    public void excluir(ResponsavelId id) {
        var r = responsaveis.remove(id.value());
        if (r != null) cpfsResponsavel.remove(normCpf(r.getCpf()));
    }

    @Override
    public boolean estaVinculadoAAlgumAluno(ResponsavelId id) {
        return alunos.values().stream().anyMatch(a ->
                a.getResponsaveis().stream().anyMatch(rid -> rid.equals(id))
        );
    }

    // =========================================================
    // =============== ProfessorRepositorio ====================
    // =========================================================
    @Override
    public ProfessorId salvar(Professor p) {
        if (p.getId() == null) {
            p.atribuirIdSeAusente(new ProfessorId(seqProfessor++));
        }
        professores.put(p.getId().value(), p);
        return p.getId();
    }

    @Override public Optional<Professor> porId(ProfessorId id) { return Optional.ofNullable(professores.get(id.value())); }

    @Override
    public int contarTurmasAtivas(ProfessorId id) {
        return (int) turmas.values().stream()
                .filter(t -> t.isAtivo() && t.getProfessor().equals(id))
                .count();
    }

    @Override
    public List<String> nomesDeAreasDoProfessor(ProfessorId id) {
        var p = professores.get(id.value());
        return (p == null) ? List.of() : p.getEspecialidades();
    }

    @Override
    public boolean possuiSimuladoFinalizado(ProfessorId id) {
        var turmasDoProfessor = turmas.values().stream()
                .filter(t -> t.getProfessor().equals(id))
                .map(Turma::getId)
                .toList();
        return simulados.values().stream()
                .anyMatch(s -> turmasDoProfessor.contains(s.getTurma())
                        && s.getStatus() == Simulado.Status.FINALIZADO);
    }

    @Override
    public void substituirProfessor(ProfessorId antigo, ProfessorId substituto) {
        turmas.replaceAll((k, t) -> {
            if (t.getProfessor().equals(antigo)) {
                t.mudarProfessor(substituto);
            }
            return t;
        });
        professores.remove(antigo.value());
    }

    // =========================================================
    // =============== TurmaRepositorio ========================
    // =========================================================
    @Override
    public void salvar(Turma t) {
        if (t.getId() == null) {
            t.atribuirIdSeAusente(new TurmaId(seqTurma++));
        }
        turmas.put(t.getId().value(), t);
    }

    @Override public Optional<Turma> porId(TurmaId id) { return Optional.ofNullable(turmas.get(id.value())); }
    @Override public void remover(TurmaId id) { turmas.remove(id.value()); }

    @Override
    public boolean existeNomeNoAno(String nome, int anoLetivo) {
        return turmas.values().stream().anyMatch(t ->
                t.getNome().equalsIgnoreCase(nome) && t.getAnoLetivo() == anoLetivo);
    }

    @Override public boolean possuiAlunosAtivos(TurmaId id) {
        return alunos.values().stream().anyMatch(a -> a.getTurma().equals(id) && a.isAtivo());
    }
    @Override public boolean possuiSimulados(TurmaId id) {
        return simulados.values().stream().anyMatch(s -> s.getTurma().equals(id));
    }
    @Override public boolean possuiSimuladosEmEdicao(TurmaId id) {
        return simulados.values().stream().anyMatch(s -> s.getTurma().equals(id) && s.getStatus() == Simulado.Status.EM_EDICAO);
    }
    @Override public boolean possuiSimuladosFinalizados(TurmaId id) {
        return simulados.values().stream().anyMatch(s -> s.getTurma().equals(id) && s.getStatus() == Simulado.Status.FINALIZADO);
    }
    @Override public int anoLetivoDe(TurmaId id) {
        var t = turmas.get(id.value());
        if (t == null) throw new NoSuchElementException("Turma não encontrada: " + id.value());
        return t.getAnoLetivo();
    }

    // =========================================================
    // =============== DisciplinaRepositorio ===================
    // =========================================================

    // índice de unicidade por (nome + área) para RN-121
    private final Set<String> nomeAreaIndex = new HashSet<>();
    private static String keyNomeArea(String nome, String areaNome) {
        String n = (nome == null ? "" : nome.trim().toLowerCase());
        String a = (areaNome == null ? "" : areaNome.trim().toLowerCase());
        return n + "#" + a;
    }
    private void rebuildDisciplinaIndex() {
        nomeAreaIndex.clear();
        for (var d : disciplinas.values()) {
            nomeAreaIndex.add(keyNomeArea(d.getNome(), d.getArea().nome()));
        }
    }

    @Override
    public DisciplinaId salvar(Disciplina d) {
        if (d.getId() == null) {
            d.atribuirIdSeAusente(new DisciplinaId(seqDisciplina++));
        }
        disciplinas.put(d.getId().value(), d);
        rebuildDisciplinaIndex();
        return d.getId();
    }

    @Override public Optional<Disciplina> porId(DisciplinaId id) { return Optional.ofNullable(disciplinas.get(id.value())); }
    @Override public void remover(DisciplinaId id) { disciplinas.remove(id.value()); rebuildDisciplinaIndex(); }

    @Override public boolean existeNomeNaArea(String nome, String areaNome) {
        return nomeAreaIndex.contains(keyNomeArea(nome, areaNome));
    }

    @Override
    public boolean foiUsadaEmAlgumSimulado(DisciplinaId id) {
        return simulados.values().stream()
            .anyMatch(s -> s.getDisciplinas().stream().anyMatch(dp -> dp.disciplina().equals(id)));
    }

    @Override
    public boolean foiUsadaEmSimuladoFinalizado(DisciplinaId id) {
        return simulados.values().stream()
            .filter(s -> s.getStatus() == Simulado.Status.FINALIZADO)
            .anyMatch(s -> s.getDisciplinas().stream().anyMatch(dp -> dp.disciplina().equals(id)));
    }

    // =========================================================
    // =============== SimuladoRepositorio =====================
    // =========================================================
    @Override
    public void salvar(Simulado s) {
        if (s.getId() == null) {
            s.atribuirIdSeAusente(new SimuladoId(seqSimulado++));
        }
        simulados.put(s.getId().value(), s);
        var map = s.getDisciplinas().stream()
                .collect(Collectors.toMap(dp -> dp.disciplina().value(), Simulado.DisciplinaPeso::peso));
        pesosPorSimulado.put(s.getId().value(), map);
    }

    @Override public Optional<Simulado> porId(SimuladoId id) { return Optional.ofNullable(simulados.get(id.value())); }

    @Override
    public int contarEmEdicaoPorTurma(TurmaId turmaId) {
        return (int) simulados.values().stream()
                .filter(s -> s.getTurma().equals(turmaId))
                .filter(s -> s.getStatus() == Simulado.Status.EM_EDICAO).count();
    }

    @Override public List<Simulado> listarPorTurma(TurmaId turmaId) {
        return simulados.values().stream().filter(s -> s.getTurma().equals(turmaId)).toList();
    }

    @Override public Map<Integer, Double> pesosDoSimulado(SimuladoId id) { return pesosPorSimulado.getOrDefault(id.value(), Map.of()); }
    @Override public boolean todasNotasLancadas(SimuladoId id) { return simuladoComTodasNotasLancadas.contains(id.value()); }

    @Override
    public boolean existeNotaParaSimulado(SimuladoId id) {
        return notas.values().stream().anyMatch(n -> n.getSimulado().equals(id));
    }

    @Override
    public void remover(SimuladoId id) {
        simulados.remove(id.value());
        pesosPorSimulado.remove(id.value());
        simuladoComTodasNotasLancadas.remove(id.value());
    }

    // util p/ testes
    public void setTodasNotasLancadas(SimuladoId id, boolean ok) {
        if (ok) simuladoComTodasNotasLancadas.add(id.value()); else simuladoComTodasNotasLancadas.remove(id.value());
    }

    // =========================================================
    // =============== NotaRepositorio =========================
    // =========================================================
    @Override
    public NotaId salvar(Nota n) {
        if (n.getId() == null) {
            n.atribuirIdSeAusente(new NotaId(seqNota++));
        }
        notas.put(n.getId().value(), n);
        return n.getId();
    }

    @Override public Optional<Nota> porId(NotaId id) { return Optional.ofNullable(notas.get(id.value())); }

    @Override
    public Optional<Nota> porChave(AlunoId aluno, SimuladoId simulado, DisciplinaId disciplina) {
        return notas.values().stream()
            .filter(n -> n.getAluno().equals(aluno)
                    && n.getSimulado().equals(simulado)
                    && n.getDisciplina().equals(disciplina))
            .findFirst();
    }

    @Override
    public List<Nota> porSimulado(SimuladoId simulado) {
        return notas.values().stream().filter(n -> n.getSimulado().equals(simulado)).toList();
    }

    @Override
    public boolean simuladoEstaEmEdicao(SimuladoId simulado) {
        var s = simulados.get(simulado.value());
        return s != null && s.getStatus() == Simulado.Status.EM_EDICAO;
    }

    // =========================================================
    // =============== JustificativaRepositorio ================
    // =========================================================
    @Override
    public JustificativaId salvar(Justificativa j) {
        if (j.getId() == null) {
            j.atribuirIdSeAusente(new JustificativaId(seqJustificativa++));
        }
        justificativasPorNota
                .computeIfAbsent(j.getNota().value(), k -> new ArrayList<>())
                .add(j);
        return j.getId();
    }

    @Override public List<Justificativa> porNota(NotaId idNota) {
        return justificativasPorNota.getOrDefault(idNota.value(), List.of());
    }

    // =========================================================
    // =============== RankingRepositorio ======================
    // =========================================================
    @Override public void limpar(SimuladoId simulado) { rankingPorSimulado.remove(simulado.value()); }
    @Override public void salvarPosicoes(SimuladoId simulado, List<ItemRanking> itens) {
        rankingPorSimulado.put(simulado.value(), List.copyOf(itens));
    }
    @Override public void congelar(SimuladoId simulado) { rankingCongelado.add(simulado.value()); }
    @Override public boolean estaCongelado(SimuladoId simulado) { return rankingCongelado.contains(simulado.value()); }
    @Override public List<ItemRanking> carregar(SimuladoId simulado) {
        return rankingPorSimulado.getOrDefault(simulado.value(), List.of());
    }
}
