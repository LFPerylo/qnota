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

    // =========================================================
    // =============== AlunoRepositorio ========================
    // =========================================================
    @Override public void salvar(Aluno a) { alunos.put(a.getId().value(), a); }

    @Override public Optional<Aluno> porId(AlunoId id) {
        return Optional.ofNullable(alunos.get(id.value()));
    }

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

    /** RN-57.1: alguma turma do ALUNO possui simulado FINALIZADO? */
    @Override
    public boolean possuiSimuladoFinalizado(AlunoId alunoId) {
        var a = alunos.get(alunoId.value());
        if (a == null) return false;
        var turmaDoAluno = a.getTurma();
        return simulados.values().stream()
                .anyMatch(s -> s.getTurma().equals(turmaDoAluno)
                            && s.getStatus() == Simulado.Status.FINALIZADO);
    }

    /** Opcional (interface pede). O serviço normalmente salva o agregado atualizado. */
    @Override
    public void alterarTurma(AlunoId alunoId, TurmaId novaTurmaId) {
        var a = alunos.get(alunoId.value());
        if (a != null) {
            var atualizado = new Aluno(a.getId(), a.getNome(), a.getDataNascimento(),
                    a.isAtivo(), novaTurmaId, a.getResponsaveis());
            alunos.put(a.getId().value(), atualizado);
        }
    }

    // =========================================================
    // =============== ResponsavelRepositorio ==================
    // =========================================================
    private final Set<String> cpfsResponsavel = new HashSet<>();

    @Override public void salvar(Responsavel r) { responsaveis.put(r.getId().value(), r); cpfsResponsavel.add(r.getCpf()); }

    @Override public Optional<Responsavel> porId(ResponsavelId id) { return Optional.ofNullable(responsaveis.get(id.value())); }

    @Override public boolean cpfExiste(String cpf) { return cpfsResponsavel.contains(cpf); }

    /** Checa vínculos percorrendo os agregados de Aluno. */
    @Override public boolean estaVinculadoAAlgumAluno(ResponsavelId id) {
        return alunos.values().stream().anyMatch(a ->
            a.getResponsaveis().stream().anyMatch(ar -> ar.responsavel().equals(id)));
    }

    // ------- Helpers (não fazem parte da interface, mas ajudam nos testes) -------
    public int quantidadeResponsaveisDoAluno(AlunoId alunoId) {
        var a = alunos.get(alunoId.value());
        return (a == null) ? 0 : a.getResponsaveis().size();
    }

    public void atualizarContato(ResponsavelId id, String novoNome, String novoEmail) {
        var r = responsaveis.get(id.value());
        if (r == null) return;
        var atualizado = new Responsavel(r.getId(), novoNome, r.getCpf(), novoEmail, r.getStatus());
        responsaveis.put(id.value(), atualizado);
    }

    public void excluir(ResponsavelId id) {
        var r = responsaveis.remove(id.value());
        if (r != null) cpfsResponsavel.remove(r.getCpf());
    }

    @Override
    public boolean vinculadoAoAluno(ResponsavelId id, AlunoId alunoId) {
        var a = alunos.get(alunoId.value());
        if (a == null) return false;
        return a.getResponsaveis().stream().anyMatch(ar -> ar.responsavel().equals(id));
    }

    @Override
    public boolean estaInadimplente(ResponsavelId id) {
        var r = responsaveis.get(id.value());
        return r != null && r.getStatus() == Responsavel.Status.INADIMPLENTE;
    }

    @Override
    public void vincular(ResponsavelId respId, AlunoId alunoId) {
        var a = alunos.get(alunoId.value());
        if (a == null) return;

        var nova = new ArrayList<>(a.getResponsaveis());
        boolean jaVinculado = nova.stream().anyMatch(ar -> ar.responsavel().equals(respId));
        if (!jaVinculado) {
            // Para o fake usamos um vínculo “neutro”; as regras (máx 3, principal, etc.) são do domínio/serviço
            nova.add(new Aluno.AlunoResponsavel(respId, "vinculo", false));
            alunos.put(a.getId().value(), new Aluno(
                    a.getId(), a.getNome(), a.getDataNascimento(), a.isAtivo(), a.getTurma(), nova));
        }
    }

    @Override
    public void desvincular(ResponsavelId respId, AlunoId alunoId) {
        var a = alunos.get(alunoId.value());
        if (a == null) return;

        var nova = a.getResponsaveis().stream()
                .filter(ar -> !ar.responsavel().equals(respId))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        alunos.put(a.getId().value(), new Aluno(
                a.getId(), a.getNome(), a.getDataNascimento(), a.isAtivo(), a.getTurma(), nova));
    }

    @Override
    public void definirPrincipal(ResponsavelId respId, AlunoId alunoId) {
        var a = alunos.get(alunoId.value());
        if (a == null) return;

        var nova = new ArrayList<Aluno.AlunoResponsavel>(a.getResponsaveis().size());
        for (var ar : a.getResponsaveis()) {
            boolean principal = ar.responsavel().equals(respId);
            nova.add(new Aluno.AlunoResponsavel(ar.responsavel(), ar.grauParentesco(), principal));
        }
        alunos.put(a.getId().value(), new Aluno(
                a.getId(), a.getNome(), a.getDataNascimento(), a.isAtivo(), a.getTurma(), nova));
    }

    // =========================================================
    // =============== ProfessorRepositorio ====================
    // =========================================================
    @Override public void salvar(Professor p) { professores.put(p.getId().value(), p); }

    @Override public Optional<Professor> porId(ProfessorId id) { return Optional.ofNullable(professores.get(id.value())); }

    @Override public int contarTurmasAtivas(ProfessorId id) {
        return (int) turmas.values().stream().filter(t -> t.isAtivo() && t.getProfessor().equals(id)).count();
    }

    @Override public List<String> nomesDeAreasDoProfessor(ProfessorId id) {
        var p = professores.get(id.value());
        return (p == null) ? List.of() : p.getEspecialidades();
    }

    @Override public boolean possuiSimuladoFinalizado(ProfessorId id) {
        var turmasDoProfessor = turmas.values().stream()
                .filter(t -> t.getProfessor().equals(id))
                .map(Turma::getId)
                .toList();
        return simulados.values().stream()
                .anyMatch(s -> turmasDoProfessor.contains(s.getTurma())
                            && s.getStatus() == Simulado.Status.FINALIZADO);
    }

    @Override public void substituirProfessor(ProfessorId antigo, ProfessorId substituto) {
        turmas.replaceAll((k, t) ->
            t.getProfessor().equals(antigo)
                ? new Turma(t.getId(), t.getNome(), t.getAnoLetivo(), t.isAtivo(), substituto)
                : t
        );
    }

    // =========================================================
    // =============== TurmaRepositorio ========================
    // =========================================================
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

    // Helper opcional para testes (não é da interface):
    public int anoLetivoDe(TurmaId id) {
        var t = turmas.get(id.value());
        if (t == null) throw new NoSuchElementException("Turma não encontrada: " + id.value());
        return t.getAnoLetivo();
    }

    // ====== DisciplinaRepositorio ======
    
    private int seqDisciplina = 1;

    // índice de unicidade por (nome + área) para RN-121
    private final java.util.Set<String> nomeAreaIndex = new java.util.HashSet<>();

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
    public dev.com.qnota.dominio.principal.disciplina.DisciplinaId proximoId() {
        return new dev.com.qnota.dominio.principal.disciplina.DisciplinaId(seqDisciplina++);
    }

    @Override
    public void salvar(dev.com.qnota.dominio.principal.disciplina.Disciplina d) {
        disciplinas.put(d.getId().value(), d);
        // Reconstroi o índice para manter consistência em edições
        rebuildDisciplinaIndex();
    }

    @Override
    public java.util.Optional<dev.com.qnota.dominio.principal.disciplina.Disciplina> porId(
            dev.com.qnota.dominio.principal.disciplina.DisciplinaId id) {
        return java.util.Optional.ofNullable(disciplinas.get(id.value()));
    }

    @Override
    public void remover(dev.com.qnota.dominio.principal.disciplina.DisciplinaId id) {
        disciplinas.remove(id.value());
        rebuildDisciplinaIndex();
    }

    @Override
    public boolean existeNomeNaArea(String nome, String areaNome) {
        // usado pelo serviço para RN-121
        return nomeAreaIndex.contains(keyNomeArea(nome, areaNome));
    }

    @Override
    public boolean foiUsadaEmAlgumSimulado(dev.com.qnota.dominio.principal.disciplina.DisciplinaId id) {
        // true se qualquer simulado referenciar a disciplina (RN-44)
        return simulados.values().stream()
                .anyMatch(s -> s.getDisciplinas().stream()
                        .anyMatch(dp -> dp.disciplina().equals(id)));
    }

    @Override
    public boolean foiUsadaEmSimuladoFinalizado(dev.com.qnota.dominio.principal.disciplina.DisciplinaId id) {
        // true se algum simulado FINALIZADO referenciar a disciplina (RN-62)
        return simulados.values().stream()
                .filter(s -> s.getStatus() == dev.com.qnota.dominio.principal.simulado.Simulado.Status.FINALIZADO)
                .anyMatch(s -> s.getDisciplinas().stream()
                        .anyMatch(dp -> dp.disciplina().equals(id)));
    }


    // =========================================================
    // =============== SimuladoRepositorio =====================
    // =========================================================
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

    // =========================================================
    // =============== NotaRepositorio =========================
    // =========================================================
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
        var s = simulados.get(simulado.value());
        return s != null && s.getStatus() == Simulado.Status.EM_EDICAO;
    }

    // =========================================================
    // =============== JustificativaRepositorio ================
    // =========================================================
    @Override public void salvar(Justificativa j) {
        justificativasPorNota.computeIfAbsent(j.getNota().value(), k -> new ArrayList<>()).add(j);
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

    @Override public List<ItemRanking> carregar(SimuladoId simulado) { return rankingPorSimulado.getOrDefault(simulado.value(), List.of()); }
}
