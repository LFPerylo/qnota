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
        validarInvariantesVinculos(lista);
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

    /** Substituição completa dos vínculos. */
    public void substituirResponsaveis(List<ResponsavelId> novaLista, ResponsavelId novoPrincipal) {
        var novos = montarVinculos(novaLista, novoPrincipal);
        validarInvariantesVinculos(novos);
        vinculos.clear();
        vinculos.addAll(novos);
    }

    /** RN-19/58: adiciona vínculo. Se principal=true e já existir principal, lança exceção. */
    public void adicionarResponsavel(ResponsavelId idResp, boolean principal) {
        Objects.requireNonNull(idResp, "'responsavelId' não pode ser nulo");

        if (contem(idResp)) throw new IllegalStateException("já existe vínculo entre o responsável e o aluno");
        if (principal && getResponsavelPrincipal() != null)
            throw new IllegalStateException("deve haver exatamente um responsável principal");

        if (vinculos.size() >= 3) throw new IllegalStateException("o número máximo de responsáveis por aluno é 3");

        var novo = new AlunoResponsavel(idResp, principal);
        vinculos.add(novo);

        // Se ainda não há principal, promova o primeiro vínculo
        if (getResponsavelPrincipal() == null) {
            promover(vinculos.get(0).responsavel());
        }

        validarInvariantesVinculos(vinculos);
    }

    /** Desvincula; se remover o principal, promove o primeiro restante. */
    public void removerResponsavel(ResponsavelId idResp) {
        boolean removido = vinculos.removeIf(v -> v.responsavel().equals(idResp));
        if (!removido) return;

        if (vinculos.isEmpty()) throw new IllegalStateException("o aluno deve ter pelo menos um responsável");

        if (getResponsavelPrincipal() == null) {
            promover(vinculos.get(0).responsavel());
        }
        validarInvariantesVinculos(vinculos);
    }

    /** Define um dos já vinculados como principal. */
    public void definirPrincipal(ResponsavelId idResp) {
        if (!contem(idResp)) throw new IllegalStateException("Vínculo de responsável inexistente");
        promover(idResp);
        validarInvariantesVinculos(vinculos);
    }

    // ========= operações de notas (mantidas) =========
    public void adicionarNota(SimuladoId simuladoId, DisciplinaId disciplinaId, double valor) {
        String chave = gerarChaveNota(simuladoId, disciplinaId);
        var notaDoAluno = new NotaDoAluno(simuladoId, disciplinaId, valor, LocalDateTime.now(), Collections.emptyList());
        notas.put(chave, notaDoAluno);
    }

    public Optional<NotaDoAluno> obterNota(SimuladoId simuladoId, DisciplinaId disciplinaId) {
        String chave = gerarChaveNota(simuladoId, disciplinaId);
        return Optional.ofNullable(notas.get(chave));
    }

    public void adicionarJustificativa(SimuladoId simuladoId, DisciplinaId disciplinaId, Justificativa justificativa) {
        String chave = gerarChaveNota(simuladoId, disciplinaId);
        var notaExistente = notas.get(chave);
        if (notaExistente == null) throw new IllegalStateException("Nota não encontrada");
        notas.put(chave, notaExistente.adicionarJustificativa(justificativa));
    }

    public void retificarNota(SimuladoId simuladoId, DisciplinaId disciplinaId, double novoValor, Justificativa justificativa) {
        String chave = gerarChaveNota(simuladoId, disciplinaId);
        var notaExistente = notas.get(chave);
        if (notaExistente == null) throw new IllegalStateException("Nota não encontrada");
        notas.put(chave, notaExistente.alterarValor(novoValor).adicionarJustificativa(justificativa));
    }

    public boolean possuiNota(SimuladoId simuladoId, DisciplinaId disciplinaId) {
        return notas.containsKey(gerarChaveNota(simuladoId, disciplinaId));
    }

    public List<NotaDoAluno> obterNotasDoSimulado(SimuladoId simuladoId) {
        return notas.values().stream().filter(n -> n.getSimuladoId().equals(simuladoId)).toList();
    }

    private String gerarChaveNota(SimuladoId simuladoId, DisciplinaId disciplinaId) {
        return simuladoId.value() + "_" + disciplinaId.value();
    }

    // ========= invariantes (somente para vínculos) =========
    private static void validarInvariantesVinculos(List<AlunoResponsavel> lista) {
        if (lista == null || lista.isEmpty())
            throw new IllegalArgumentException("Aluno deve ter ao menos um responsável");
        if (lista.size() > 3)
            throw new IllegalArgumentException("o número máximo de responsáveis por aluno é 3");

        // sem duplicados
        var ids = lista.stream().map(AlunoResponsavel::responsavel).toList();
        if (new LinkedHashSet<>(ids).size() != ids.size())
            throw new IllegalArgumentException("Vínculo de responsável duplicado");

        long principais = lista.stream().filter(AlunoResponsavel::principal).count();
        if (principais != 1)
            throw new IllegalArgumentException("deve haver exatamente um responsável principal");
    }

    // ========= helpers =========
    private static String requireNonBlank(String s, String msg) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException(msg);
        return s.trim();
    }

    private static List<AlunoResponsavel> montarVinculos(List<ResponsavelId> ids, ResponsavelId principal) {
        if (ids == null) ids = List.of();
        if (principal == null) throw new IllegalArgumentException("é obrigatório definir um responsável principal");
        var lista = new ArrayList<AlunoResponsavel>(ids.size());
        for (var id : ids) {
            if (id == null) throw new IllegalArgumentException("Responsável não pode ser nulo");
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