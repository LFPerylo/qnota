package dev.com.qnota.dominio.principal.aluno;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.turma.TurmaId;

public class Aluno {

    private AlunoId id; // atribuído pelo ORM
    private String nome;
    private LocalDate dataNascimento;
    private boolean ativo;
    private TurmaId turma;

    /** Vínculos: VO (id + flag principal) */
    private final List<AlunoResponsavel> vinculos;

    /** Notas do agregado (endogestas ao Aluno) */
    private final Map<String, NotaDoAluno> notas;

    public Aluno(String nome,
                 LocalDate dataNascimento,
                 boolean ativo,
                 TurmaId turma,
                 List<ResponsavelId> responsaveis,
                 ResponsavelId principal) {
        this.id = null;
        this.nome = requireNonBlank(nome, "'nome' não pode ser vazio");
        this.dataNascimento = Objects.requireNonNull(dataNascimento, "'dataNascimento' não pode ser nula");
        this.ativo = ativo;
        this.turma = Objects.requireNonNull(turma, "'turma' não pode ser nula");
        this.vinculos = new ArrayList<>(montarVinculos(responsaveis, principal));
        this.notas = new HashMap<>();
    }

    public Aluno(AlunoId id,
                 String nome,
                 LocalDate dataNascimento,
                 boolean ativo,
                 TurmaId turma,
                 List<ResponsavelId> responsaveis,
                 ResponsavelId principal) {
        this(nome, dataNascimento, ativo, turma, responsaveis, principal);
        this.id = Objects.requireNonNull(id, "'id' não pode ser nulo");
    }

    /** ORM atribui o ID se ausente. */
    public void atribuirIdSeAusente(AlunoId novoId) {
        Objects.requireNonNull(novoId, "'id' não pode ser nulo");
        if (this.id != null && !this.id.equals(novoId)) throw new IllegalStateException("ID já atribuído e diferente");
        this.id = novoId;
    }

    // ========= getters =========
    public AlunoId getId() { return id; }
    public String getNome() { return nome; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public boolean isAtivo() { return ativo; }
    public TurmaId getTurma() { return turma; }

    /** Compatibilidade (ids simples) */
    public List<ResponsavelId> getResponsaveis() {
        return vinculos.stream().map(AlunoResponsavel::responsavel).toList();
    }

    /** VO bruto (somente leitura) */
    public List<AlunoResponsavel> getVinculos() {
        return Collections.unmodifiableList(vinculos);
    }

    public ResponsavelId getResponsavelPrincipal() {
        return vinculos.stream().filter(AlunoResponsavel::principal)
                .map(AlunoResponsavel::responsavel).findFirst().orElse(null);
    }

    /** Leitura das notas do agregado (somente leitura). */
    public Collection<NotaDoAluno> getNotas() {
        return Collections.unmodifiableCollection(notas.values());
    }

    // ========= operações locais do agregado =========
    public void inativar() { this.ativo = false; }
    public void ativar()   { this.ativo = true;  }

    public void mudarTurma(TurmaId novaTurma) {
        this.turma = Objects.requireNonNull(novaTurma, "'novaTurma' não pode ser nula");
    }

    public void renomear(String novoNome) {
        this.nome = requireNonBlank(novoNome, "'nome' não pode ser vazio");
    }

    /** Substituição completa de vínculos (validações ficam no serviço). */
    public void substituirResponsaveis(List<ResponsavelId> novaLista, ResponsavelId novoPrincipal) {
        var novos = montarVinculos(novaLista, novoPrincipal);
        vinculos.clear();
        vinculos.addAll(novos);
    }

    /** Adiciona vínculo (serviço faz as RN). */
    public void adicionarResponsavel(ResponsavelId idResp, boolean principal) {
        Objects.requireNonNull(idResp, "'responsavelId' não pode ser nulo");
        vinculos.add(new AlunoResponsavel(idResp, false));

        // Se pediram principal OU ainda não existe principal, promove o próprio idResp
        if (principal || getResponsavelPrincipal() == null) {
            promover(idResp);
        }
    }

    /** Remove vínculo (serviço garante RN). */
    public void removerResponsavel(ResponsavelId idResp) {
        boolean removido = vinculos.removeIf(v -> v.responsavel().equals(idResp));
        if (!removido) return;
        if (getResponsavelPrincipal() == null && !vinculos.isEmpty()) {
            promover(vinculos.get(0).responsavel());
        }
    }

    public void definirPrincipal(ResponsavelId idResp) {
        promover(idResp);
    }

    // ========= NOTAS — métodos internos ao agregado (usados por NotaServico) =========

    /** Sem validações — NotaServico valida antes. (package-private) */
    void adicionarNotaInterna(SimuladoId simuladoId, DisciplinaId disciplinaId, double valor) {
        var chave = key(simuladoId, disciplinaId);
        var nota = new NotaDoAluno(simuladoId, disciplinaId, valor, LocalDateTime.now(), Collections.emptyList());
        notas.put(chave, nota);
    }

    /** (package-private) */
    Optional<NotaDoAluno> obterNotaInterna(SimuladoId simuladoId, DisciplinaId disciplinaId) {
        return Optional.ofNullable(notas.get(key(simuladoId, disciplinaId)));
    }

    /** (package-private) */
    void adicionarJustificativaInterna(SimuladoId simuladoId, DisciplinaId disciplinaId, Justificativa justificativa) {
        var chave = key(simuladoId, disciplinaId);
        var atual = notas.get(chave);
        notas.put(chave, atual.adicionarJustificativa(justificativa));
    }

    /** (package-private) */
    void retificarNotaInterna(SimuladoId simuladoId, DisciplinaId disciplinaId, double novoValor, Justificativa justificativa) {
        var chave = key(simuladoId, disciplinaId);
        var atual = notas.get(chave);
        notas.put(chave, atual.alterarValor(novoValor).adicionarJustificativa(justificativa));
    }

    /** (package-private) */
    boolean possuiNotaInterna(SimuladoId simuladoId, DisciplinaId disciplinaId) {
        return notas.containsKey(key(simuladoId, disciplinaId));
    }

    // ========= helpers =========
    private static String requireNonBlank(String s, String msg) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException(msg);
        return s.trim();
    }

    private static List<AlunoResponsavel> montarVinculos(List<ResponsavelId> ids, ResponsavelId principal) {
        if (ids == null) ids = List.of();
        var lista = new ArrayList<AlunoResponsavel>(ids.size());
        for (var id : ids) {
            Objects.requireNonNull(id, "Responsável não pode ser nulo");
            lista.add(new AlunoResponsavel(id, id.equals(principal)));
        }
        return lista;
    }

    private void promover(ResponsavelId novoPrincipal) {
        for (int i = 0; i < vinculos.size(); i++) {
            var v = vinculos.get(i);
            vinculos.set(i, new AlunoResponsavel(v.responsavel(), v.responsavel().equals(novoPrincipal)));
        }
    }

    private String key(SimuladoId s, DisciplinaId d) {
        return s.value() + "_" + d.value();
    }
}
