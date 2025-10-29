package dev.com.qnota.dominio.principal.aluno;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.aluno.Justificativa; // ajuste o pacote se necessário
import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.turma.TurmaId;

public class Aluno {

    private AlunoId id; // ORM
    private String nome;
    private LocalDate dataNascimento;
    private boolean ativo;
    private TurmaId turma;

    /** Agora o vínculo é um VO (id + flag de principal) */
    private final List<AlunoResponsavel> vinculos;

    // Notas (mantido)
    private final Map<String, NotaDoAluno> notas;

    /** Construtor recomendado (ID gerado na persistência). */
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

        var lista = montarVinculos(responsaveis, principal);
        this.vinculos = new ArrayList<>(lista);
        this.notas = new HashMap<>();
    }

    /** Construtor com id (compatibilidade). */
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

    /** ORM/Repo fixa o ID se ainda não houver. */
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

    /** Mantido por compatibilidade (ids simples) */
    public List<ResponsavelId> getResponsaveis() {
        return vinculos.stream().map(AlunoResponsavel::responsavel).toList();
    }

    /** Novo: acesso direto aos vínculos (VO) */
    public List<AlunoResponsavel> getVinculos() {
        return Collections.unmodifiableList(vinculos);
    }

    public ResponsavelId getResponsavelPrincipal() {
        return vinculos.stream().filter(AlunoResponsavel::principal)
                .map(AlunoResponsavel::responsavel).findFirst().orElse(null);
    }

    public Collection<NotaDoAluno> getNotas() { return Collections.unmodifiableCollection(notas.values()); }

    // ========= operações =========
    public void inativar() { this.ativo = false; }
    public void ativar()   { this.ativo = true;  }

    public void mudarTurma(TurmaId novaTurma) {
        this.turma = Objects.requireNonNull(novaTurma, "'novaTurma' não pode ser nula");
    }

    /** Substituição completa dos vínculos sem validações de negócio. */
    public void substituirResponsaveis(List<ResponsavelId> novaLista, ResponsavelId novoPrincipal) {
        var novos = montarVinculos(novaLista, novoPrincipal);
        vinculos.clear();
        vinculos.addAll(novos);
    }

    /** Adiciona vínculo sem validações de negócio (validações ficam no serviço). */
    public void adicionarResponsavel(ResponsavelId idResp, boolean principal) {
        Objects.requireNonNull(idResp, "'responsavelId' não pode ser nulo");

        var novo = new AlunoResponsavel(idResp, principal);
        vinculos.add(novo);

        // Se ainda não há principal, promova o primeiro vínculo
        if (getResponsavelPrincipal() == null) {
            promover(vinculos.get(0).responsavel());
        }
    }

    /** Desvincula responsável sem validações de negócio (validações ficam no serviço). */
    public void removerResponsavel(ResponsavelId idResp) {
        boolean removido = vinculos.removeIf(v -> v.responsavel().equals(idResp));
        if (!removido) return;

        if (getResponsavelPrincipal() == null && !vinculos.isEmpty()) {
            promover(vinculos.get(0).responsavel());
        }
    }

    /** Define um dos já vinculados como principal sem validações de negócio. */
    public void definirPrincipal(ResponsavelId idResp) {
        promover(idResp);
    }

    // ========= operações de notas (métodos internos - apenas para NotaServico) =========
    
    private String gerarChaveNota(SimuladoId simuladoId, DisciplinaId disciplinaId) {
        return simuladoId.value() + "_" + disciplinaId.value();
    }
    
    /** Método interno para NotaServico - adiciona nota sem validações */
    void adicionarNotaInterna(SimuladoId simuladoId, DisciplinaId disciplinaId, double valor) {
        String chave = gerarChaveNota(simuladoId, disciplinaId);
        var notaDoAluno = new NotaDoAluno(simuladoId, disciplinaId, valor, LocalDateTime.now(), Collections.emptyList());
        notas.put(chave, notaDoAluno);
    }

    /** Método interno para NotaServico - obtém nota sem validações */
    Optional<NotaDoAluno> obterNotaInterna(SimuladoId simuladoId, DisciplinaId disciplinaId) {
        String chave = gerarChaveNota(simuladoId, disciplinaId);
        return Optional.ofNullable(notas.get(chave));
    }

    /** Método interno para NotaServico - adiciona justificativa sem validações */
    void adicionarJustificativaInterna(SimuladoId simuladoId, DisciplinaId disciplinaId, Justificativa justificativa) {
        String chave = gerarChaveNota(simuladoId, disciplinaId);
        var notaExistente = notas.get(chave);
        notas.put(chave, notaExistente.adicionarJustificativa(justificativa));
    }

    /** Método interno para NotaServico - retifica nota sem validações */
    void retificarNotaInterna(SimuladoId simuladoId, DisciplinaId disciplinaId, double novoValor, Justificativa justificativa) {
        String chave = gerarChaveNota(simuladoId, disciplinaId);
        var notaExistente = notas.get(chave);
        notas.put(chave, notaExistente.alterarValor(novoValor).adicionarJustificativa(justificativa));
    }

    /** Método interno para NotaServico - verifica se possui nota */
    boolean possuiNotaInterna(SimuladoId simuladoId, DisciplinaId disciplinaId) {
        return notas.containsKey(gerarChaveNota(simuladoId, disciplinaId));
    }

    /** Método interno para NotaServico - obtém notas do simulado */
    List<NotaDoAluno> obterNotasDoSimuladoInterna(SimuladoId simuladoId) {
        return notas.values().stream().filter(n -> n.getSimuladoId().equals(simuladoId)).toList();
    }

    /** Método público para testes - adiciona nota sem validações */
    public void adicionarNotaParaTeste(SimuladoId simuladoId, DisciplinaId disciplinaId, double valor) {
        adicionarNotaInterna(simuladoId, disciplinaId, valor);
    }

    /** Método público para testes - obtém nota sem validações */
    public Optional<NotaDoAluno> obterNotaParaTeste(SimuladoId simuladoId, DisciplinaId disciplinaId) {
        return obterNotaInterna(simuladoId, disciplinaId);
    }

    // ========= helpers =========
    private static String requireNonBlank(String s, String msg) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException(msg);
        return s.trim();
    }

    private static List<AlunoResponsavel> montarVinculos(List<ResponsavelId> ids, ResponsavelId principal) {
        if (ids == null) ids = List.of();
        // Validações de negócio removidas - ficam no AlunoServico
        var lista = new ArrayList<AlunoResponsavel>(ids.size());
        for (var id : ids) {
            lista.add(new AlunoResponsavel(id, id.equals(principal)));
        }
        return lista;
    }

    private boolean contem(ResponsavelId idResp) {
        return vinculos.stream().anyMatch(v -> v.responsavel().equals(idResp));
    }

    private void promover(ResponsavelId novoPrincipal) {
        for (int i = 0; i < vinculos.size(); i++) {
            var v = vinculos.get(i);
            vinculos.set(i, new AlunoResponsavel(v.responsavel(), v.responsavel().equals(novoPrincipal)));
        }
    }
}