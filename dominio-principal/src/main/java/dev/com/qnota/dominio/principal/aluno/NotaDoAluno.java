package dev.com.qnota.dominio.principal.aluno;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dev.com.qnota.dominio.principal.disciplina.DisciplinaId;
import dev.com.qnota.dominio.principal.aluno.Justificativa;
import dev.com.qnota.dominio.principal.simulado.SimuladoId;

/**
 * Entidade que representa a nota de um aluno em uma disciplina específica de um simulado,
 * incluindo todas as justificativas relacionadas a essa nota.
 */
public class NotaDoAluno {

    // ID gerado na infraestrutura; atribuído pelo repositório após persistir
    private NotaId id;
    
    private final SimuladoId simuladoId;
    private final DisciplinaId disciplinaId;
    private double valor;
    private final LocalDateTime dataLancamento;
    private final List<Justificativa> justificativas;

    /** Constrói sem ID; o repositório chamará atribuirIdSeAusente(...) após salvar. */
    public NotaDoAluno(SimuladoId simuladoId,
                      DisciplinaId disciplinaId,
                      double valor,
                      LocalDateTime dataLancamento,
                      List<Justificativa> justificativas) {
        
        this.simuladoId = Objects.requireNonNull(simuladoId, "'simuladoId' não pode ser nulo");
        this.disciplinaId = Objects.requireNonNull(disciplinaId, "'disciplinaId' não pode ser nulo");
        this.dataLancamento = Objects.requireNonNull(dataLancamento, "'dataLancamento' não pode ser nulo");
        
        this.valor = valor;
        
        this.justificativas = justificativas != null ? 
            Collections.unmodifiableList(justificativas) : 
            Collections.emptyList();
    }

    /** Infra chama para fixar o ID gerado. Não permite reatribuição divergente. */
    public void atribuirIdSeAusente(NotaId novoId) {
        Objects.requireNonNull(novoId, "'id' não pode ser nulo");
        if (this.id != null && !this.id.equals(novoId)) {
            throw new IllegalStateException("ID já atribuído para esta nota");
        }
        this.id = novoId;
    }

    // Getters
    public NotaId getId() { return id; }
    public SimuladoId getSimuladoId() { return simuladoId; }
    public DisciplinaId getDisciplinaId() { return disciplinaId; }
    public double getValor() { return valor; }
    public LocalDateTime getDataLancamento() { return dataLancamento; }
    public List<Justificativa> getJustificativas() { return justificativas; }

    /**
     * Cria uma nova instância de NotaDoAluno com uma justificativa adicional.
     * Como Justificativa é um value object imutável, retorna uma nova instância.
     */
    public NotaDoAluno adicionarJustificativa(Justificativa novaJustificativa) {
        Objects.requireNonNull(novaJustificativa, "'justificativa' não pode ser nula");
        
        var novasJustificativas = new java.util.ArrayList<>(this.justificativas);
        novasJustificativas.add(novaJustificativa);
        
        var novaNota = new NotaDoAluno(this.simuladoId, this.disciplinaId, this.valor, 
                                      this.dataLancamento, novasJustificativas);
        
        // Preserva o ID se existir
        if (this.id != null) {
            novaNota.atribuirIdSeAusente(this.id);
        }
        
        return novaNota;
    }

    /**
     * Cria uma nova instância de NotaDoAluno com o valor atualizado.
     * Retorna uma nova instância para manter a imutabilidade.
     */
    public NotaDoAluno alterarValor(double novoValor) {
        var novaNota = new NotaDoAluno(this.simuladoId, this.disciplinaId, novoValor, 
                                      this.dataLancamento, this.justificativas);
        
        // Preserva o ID se existir
        if (this.id != null) {
            novaNota.atribuirIdSeAusente(this.id);
        }
        
        return novaNota;
    }

    /**
     * Verifica se esta nota possui justificativas.
     */
    public boolean temJustificativas() {
        return !justificativas.isEmpty();
    }

    /**
     * Retorna o número de justificativas.
     */
    public int quantidadeJustificativas() {
        return justificativas.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotaDoAluno that = (NotaDoAluno) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("NotaDoAluno{id=%s, simuladoId=%s, disciplinaId=%s, valor=%.2f, dataLancamento=%s, justificativas=%d}",
                id, simuladoId, disciplinaId, valor, dataLancamento, justificativas.size());
    }
}
