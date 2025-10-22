package dev.com.qnota.dominio.principal.aluno;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.aluno.Justificativa;
import dev.com.qnota.dominio.principal.responsavel.ResponsavelId;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;
import dev.com.qnota.dominio.principal.turma.TurmaId;

public class Aluno {

    private AlunoId id; // atribuído pelo repositório/ORM
    private String nome;
    private LocalDate dataNascimento;
    private boolean ativo;
    private TurmaId turma;

    // Agora: lista simples de responsáveis + um principal
    private final List<ResponsavelId> responsaveis;
    private ResponsavelId responsavelPrincipal;

    // Coleção de notas do aluno organizadas por simulado e disciplina
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

        var lista = copyIds(responsaveis);
        this.responsaveis = lista;
        this.responsavelPrincipal = principal;
        this.notas = new HashMap<>();
    }

    /** Construtor compatível quando o ID já é conhecido. */
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
        if (this.id != null && !this.id.equals(novoId))
            throw new IllegalStateException("ID já atribuído e diferente");
        this.id = novoId;
    }

    // ========= getters =========
    public AlunoId getId() { return id; }
    public String getNome() { return nome; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public boolean isAtivo() { return ativo; }
    public TurmaId getTurma() { return turma; }
    public List<ResponsavelId> getResponsaveis() { return Collections.unmodifiableList(responsaveis); }
    public ResponsavelId getResponsavelPrincipal() { return responsavelPrincipal; }
    public Collection<NotaDoAluno> getNotas() { return Collections.unmodifiableCollection(notas.values()); }

    // ========= operações =========
    public void inativar() { this.ativo = false; }
    public void ativar()   { this.ativo = true;  }

    public void mudarTurma(TurmaId novaTurma) {
        this.turma = Objects.requireNonNull(novaTurma, "'novaTurma' não pode ser nula");
    }

    /** Substituição completa da lista e do principal. */
    public void substituirResponsaveis(List<ResponsavelId> novaLista, ResponsavelId novoPrincipal) {
        var tmp = copyIds(novaLista);
        responsaveis.clear();
        responsaveis.addAll(tmp);
        responsavelPrincipal = novoPrincipal;
    }

    /** Vincular responsável. Operação simples sem validações de negócio. */
    public void adicionarResponsavel(ResponsavelId idResp, boolean principal) {
        Objects.requireNonNull(idResp, "'responsavelId' não pode ser nulo");

        var nova = new ArrayList<>(responsaveis);
        nova.add(idResp);

        var novoPrincipal = this.responsavelPrincipal;
        if (principal) {
            novoPrincipal = idResp;
        }
        if (novoPrincipal == null) novoPrincipal = idResp; // garante 1 principal

        responsaveis.clear();
        responsaveis.addAll(nova);
        responsavelPrincipal = novoPrincipal;
    }

    /** Desvincula; se remover o principal, promove o primeiro da lista. */
    public void removerResponsavel(ResponsavelId idResp) {
        var nova = new ArrayList<>(responsaveis);
        boolean removido = nova.remove(idResp);
        if (!removido) return;

        var novoPrincipal = this.responsavelPrincipal;
        if (idResp.equals(this.responsavelPrincipal)) {
            novoPrincipal = nova.get(0); // autopromoção
        }

        responsaveis.clear();
        responsaveis.addAll(nova);
        responsavelPrincipal = novoPrincipal;
    }

    public void definirPrincipal(ResponsavelId idResp) {
        if (!responsaveis.contains(idResp))
            throw new IllegalStateException("Vínculo de responsável inexistente");
        this.responsavelPrincipal = idResp;
    }

    // ========= operações de notas =========
    
    /**
     * Adiciona uma nota do aluno em um simulado/disciplina específica.
     * Operação simples sem validações de negócio.
     */
    public void adicionarNota(SimuladoId simuladoId, DisciplinaId disciplinaId, double valor) {
        String chave = gerarChaveNota(simuladoId, disciplinaId);
        var notaDoAluno = new NotaDoAluno(simuladoId, disciplinaId, valor, LocalDateTime.now(), Collections.emptyList());
        notas.put(chave, notaDoAluno);
    }

    /**
     * Retorna a nota do aluno em um simulado/disciplina específica.
     */
    public Optional<NotaDoAluno> obterNota(SimuladoId simuladoId, DisciplinaId disciplinaId) {
        String chave = gerarChaveNota(simuladoId, disciplinaId);
        return Optional.ofNullable(notas.get(chave));
    }

    /**
     * Adiciona uma justificativa à nota existente.
     */
    public void adicionarJustificativa(SimuladoId simuladoId, DisciplinaId disciplinaId, Justificativa justificativa) {
        String chave = gerarChaveNota(simuladoId, disciplinaId);
        NotaDoAluno notaExistente = notas.get(chave);
        
        if (notaExistente == null) {
            throw new IllegalStateException("Nota não encontrada para o simulado/disciplina especificados");
        }
        
        NotaDoAluno notaAtualizada = notaExistente.adicionarJustificativa(justificativa);
        notas.put(chave, notaAtualizada);
    }

    /**
     * Retifica uma nota existente criando uma nova versão com justificativa.
     */
    public void retificarNota(SimuladoId simuladoId, DisciplinaId disciplinaId, double novoValor, Justificativa justificativa) {
        String chave = gerarChaveNota(simuladoId, disciplinaId);
        NotaDoAluno notaExistente = notas.get(chave);
        
        if (notaExistente == null) {
            throw new IllegalStateException("Nota não encontrada para o simulado/disciplina especificados");
        }
        
        // Cria nova versão da nota com o novo valor e justificativa
        NotaDoAluno notaRetificada = notaExistente.alterarValor(novoValor).adicionarJustificativa(justificativa);
        notas.put(chave, notaRetificada);
    }

    /**
     * Verifica se o aluno possui nota em um simulado/disciplina específica.
     */
    public boolean possuiNota(SimuladoId simuladoId, DisciplinaId disciplinaId) {
        String chave = gerarChaveNota(simuladoId, disciplinaId);
        return notas.containsKey(chave);
    }

    /**
     * Retorna todas as notas do aluno em um simulado específico.
     */
    public List<NotaDoAluno> obterNotasDoSimulado(SimuladoId simuladoId) {
        return notas.values().stream()
                .filter(nota -> nota.getSimuladoId().equals(simuladoId))
                .collect(Collectors.toList());
    }

    /**
     * Gera uma chave única para identificar uma nota (simulado + disciplina).
     */
    private String gerarChaveNota(SimuladoId simuladoId, DisciplinaId disciplinaId) {
        return simuladoId.value() + "_" + disciplinaId.value();
    }


    // ========= helpers =========
    private static String requireNonBlank(String s, String msg) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException(msg);
        return s.trim();
    }
    private static List<ResponsavelId> copyIds(List<ResponsavelId> origem) {
        if (origem == null) return new ArrayList<>();
        var tmp = new ArrayList<ResponsavelId>(origem.size());
        for (var id : origem) {
            if (id == null) throw new IllegalArgumentException("Responsável não pode ser nulo");
            tmp.add(id);
        }
        return tmp;
    }
}
