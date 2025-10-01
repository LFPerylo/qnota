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

/** Repositório em memória que implementa TODAS as interfaces do subdomínio principal. */
public class Repositorio implements
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
    private int seqNota = 1;

    private final Map<Integer, List<Justificativa>> justificativasPorNota = new ConcurrentHashMap<>();

    // auxiliares
    private final Map<Integer, Map<Integer, Double>> pesosPorSimulado = new HashMap<>();
    private final Set<Integer> simuladoComTodasNotasLancadas = new HashSet<>();

    // ranking (snapshot opcional para testes)
    private final Map<Integer, List<ItemRanking>> rankingPorSimulado = new HashMap<>();
    private final Set<Integer> rankingCongelado = new HashSet<>();

    // ---------- utils ----------
    private static String keyNomeArea(String n, Object area) {
        return (n + "#" + String.valueOf(area)).toLowerCase();
    }

    // ====== AlunoRepositorio ======
    @Override public void salvar(Aluno a) { alunos.put(a.getId().value(), a); }
    @Override public Optional<Aluno> porId(AlunoId id) { return Optional.ofNullable(alunos.get(id.value())); }
    @Override public void remover(AlunoId id) { alunos.remove(id.value()); }

    @Override
    public boolean existeOutroComMesmoNomeENascimentoNaTurma(String nome, LocalDate data, TurmaId turmaId) {
        return alunos.values().stream().anyMatch(a ->
                a.getNome().equalsIgnoreCase(nome)
             && a.getDataNascimento().equals(data)
             && a.getTurma().equals(turmaId));
    }
    @Override public int contarResponsaveis(AlunoId id) {
        return porId(id).map(a -> a.getResponsaveis().size()).orElse(0);
    }
    @Override public boolean existeVinculo(AlunoId id) { return alunos.containsKey(id.value()); }
    @Override public List<Aluno> porTurma(TurmaId turmaId) {
        return alunos.values().stream().filter(a -> a.getTurma().equals(turmaId)).toList();
    }
    @Override public boolean temNotasPendentesEmSimuladosEmEdicao(AlunoId alunoId) {
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

    // ====== ResponsavelRepositorio ======
    private final Set<String> cpfsResponsavel = new HashSet<>();
    private final Set<Integer> responsavelVinculado = new HashSet<>();

    @Override public void salvar(Responsavel r) { responsaveis.put(r.getId().value(), r); cpfsResponsavel.add(r.getCpf()); }
    @Override public Optional<Responsavel> porId(ResponsavelId id) { return Optional.ofNullable(responsaveis.get(id.value())); }
    @Override public boolean cpfExiste(String cpf) { return cpfsResponsavel.contains(cpf); }
    @Override public boolean estaVinculadoAAlgumAluno(ResponsavelId id) { return responsavelVinculado.contains(id.value()); }
    // util
    public void marcarResponsavelVinculado(ResponsavelId id, boolean on){ if (on) responsavelVinculado.add(id.value()); else responsavelVinculado.remove(id.value()); }

    // ====== ProfessorRepositorio ======
	@Override
	public void salvar(Professor p) {
		professores.put(p.getId().value(), p);
	}

	@Override
	public Optional<Professor> porId(ProfessorId id) {
		return Optional.ofNullable(professores.get(id.value()));
	}

	@Override
	public int contarTurmasAtivas(ProfessorId id) {
		return (int) turmas.values().stream()
				.filter(t -> t.isAtivo() && t.getProfessor().equals(id))
				.count();
	}

	@Override
	public List<String> nomesDeAreasDoProfessor(ProfessorId id) {
		var p = professores.get(id.value());
		return (p == null) ? List.of() : p.getEspecialidades(); // Professor.especialidades é LISTA
	}

	@Override
	public boolean possuiSimuladoFinalizado(ProfessorId id) {
		// Alguma turma do professor tem simulado FINALIZADO?
		var turmasDoProfessor = turmas.values().stream()
				.filter(t -> t.getProfessor().equals(id))
				.map(t -> t.getId())
				.toList();

		return simulados.values().stream()
				.anyMatch(s -> turmasDoProfessor.contains(s.getTurma())
							&& s.getStatus() == Simulado.Status.FINALIZADO);
	}

	@Override
	public void substituirProfessor(ProfessorId antigo, ProfessorId substituto) {
		// Atualiza todas as turmas que referenciam o professor "antigo"
		turmas.replaceAll((k, t) ->
				t.getProfessor().equals(antigo)
						? new Turma(t.getId(), t.getNome(), t.getAnoLetivo(), t.isAtivo(), substituto)
						: t
		);
	}


    // ====== TurmaRepositorio ======
    @Override public void salvar(Turma t) { turmas.put(t.getId().value(), t); }
    @Override public Optional<Turma> porId(TurmaId id) { return Optional.ofNullable(turmas.get(id.value())); }
    @Override public boolean existeNomeNoAno(String nome, int anoLetivo) {
        return turmas.values().stream().anyMatch(t -> t.getNome().equalsIgnoreCase(nome) && t.getAnoLetivo() == anoLetivo);
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

    // ====== DisciplinaRepositorio ======
    private final Set<String> nomeAreaUsados = new HashSet<>();

    @Override public void salvar(Disciplina d) {
        disciplinas.put(d.getId().value(), d);
        nomeAreaUsados.add(keyNomeArea(d.getNome(), d.getArea()));
    }
    @Override public Optional<Disciplina> porId(DisciplinaId id) { return Optional.ofNullable(disciplinas.get(id.value())); }
    @Override public boolean existeNomeNaArea(String nome, String areaNome) { return nomeAreaUsados.contains(keyNomeArea(nome, areaNome)); }
    @Override public boolean foiUsadaEmAlgumSimulado(DisciplinaId id) {
        return simulados.values().stream().anyMatch(s -> s.getDisciplinas().stream().anyMatch(dp -> dp.disciplina().equals(id)));
    }
    @Override public boolean foiUsadaEmSimuladoFinalizado(DisciplinaId id) {
        return simulados.values().stream()
                .filter(s -> s.getStatus() == Simulado.Status.FINALIZADO)
                .anyMatch(s -> s.getDisciplinas().stream().anyMatch(dp -> dp.disciplina().equals(id)));
    }

    // ====== SimuladoRepositorio ======
    @Override public void salvar(Simulado s) {
        simulados.put(s.getId().value(), s);
        var map = s.getDisciplinas().stream()
                .collect(Collectors.toMap(dp -> dp.disciplina().value(), Simulado.DisciplinaPeso::peso));
        pesosPorSimulado.put(s.getId().value(), map);
    }
    @Override public Optional<Simulado> porId(SimuladoId id) { return Optional.ofNullable(simulados.get(id.value())); }
    @Override public int contarEmEdicaoPorTurma(TurmaId turmaId) {
        return (int) simulados.values().stream()
                .filter(s -> s.getTurma().equals(turmaId))
                .filter(s -> s.getStatus() == Simulado.Status.EM_EDICAO).count();
    }
    // RENOMEADO: evita conflito com AlunoRepositorio
    @Override public List<Simulado> listarPorTurma(TurmaId turmaId) {
        return simulados.values().stream().filter(s -> s.getTurma().equals(turmaId)).toList();
    }
    @Override public Map<Integer, Double> pesosDoSimulado(SimuladoId id) {
        return pesosPorSimulado.getOrDefault(id.value(), Map.of());
    }
    @Override public boolean todasNotasLancadas(SimuladoId id) { return simuladoComTodasNotasLancadas.contains(id.value()); }
    // util p/ testes
    public void setTodasNotasLancadas(SimuladoId id, boolean ok) {
        if (ok) simuladoComTodasNotasLancadas.add(id.value()); else simuladoComTodasNotasLancadas.remove(id.value());
    }

    // ====== NotaRepositorio ======
    @Override public void salvar(Nota n) {
        int id = (n.getId() == null) ? (seqNota++) : n.getId().value();
        notas.put(id, new Nota(new NotaId(id), n.getAluno(), n.getSimulado(), n.getDisciplina(), n.getValor(), n.getDataLancamento()));
    }
    @Override public Optional<Nota> porId(NotaId id) { return Optional.ofNullable(notas.get(id.value())); }
    @Override public Optional<Nota> porChave(AlunoId aluno, SimuladoId simulado, DisciplinaId disciplina) {
        return notas.values().stream().filter(n ->
                n.getAluno().equals(aluno) && n.getSimulado().equals(simulado) && n.getDisciplina().equals(disciplina)).findFirst();
    }
    @Override public List<Nota> porSimulado(SimuladoId simulado) {
        return notas.values().stream().filter(n -> n.getSimulado().equals(simulado)).toList();
    }
    @Override public boolean simuladoEstaEmEdicao(SimuladoId simulado) {
        return porId(simulado).map(Simulado::getStatus).map(st -> st == Simulado.Status.EM_EDICAO).orElse(false);
    }

    // ====== JustificativaRepositorio ======
    @Override public void salvar(Justificativa j) {
        justificativasPorNota.computeIfAbsent(j.getNota().value(), k -> new ArrayList<>()).add(j);
    }
    @Override public List<Justificativa> porNota(NotaId idNota) {
        return justificativasPorNota.getOrDefault(idNota.value(), List.of());
    }

    // ====== RankingRepositorio (snapshot em memória para testes) ======
    @Override public void limpar(SimuladoId simulado) { rankingPorSimulado.remove(simulado.value()); }
    @Override public void salvarPosicoes(SimuladoId simulado, List<ItemRanking> itens) { rankingPorSimulado.put(simulado.value(), List.copyOf(itens)); }
    @Override public void congelar(SimuladoId simulado) { rankingCongelado.add(simulado.value()); }
    @Override public boolean estaCongelado(SimuladoId simulado) { return rankingCongelado.contains(simulado.value()); }
    @Override public List<ItemRanking> carregar(SimuladoId simulado) { return rankingPorSimulado.getOrDefault(simulado.value(), List.of()); }
}
