/* Titulo: Padrao Decorator - Implementacao de auditoria com armazenamento em memoria */
package dev.com.qnota.dominio.principal.simulado;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Implementacao de SimuladoAuditoria que armazena os eventos em memoria.
 * 
 * Esta classe demonstra o padrao Decorator em acao:
 * - O SimuladoRepositorioDecorator intercepta operacoes do repositorio
 * - Cada operacao gera um evento que e registrado aqui
 * - Os eventos podem ser consultados via API para exibicao no frontend
 * 
 * Nota: Em producao, os eventos seriam persistidos em banco de dados.
 * Esta implementacao em memoria e suficiente para demonstracao do padrao.
 */
public class SimuladoAuditoriaArmazenada implements SimuladoAuditoria {

    /** Record imutavel representando um evento de auditoria */
    public record EventoAuditoria(
        LocalDateTime dataHora,
        TipoEvento tipo,
        Integer simuladoId,
        Integer turmaId,
        String status,
        String descricao
    ) {}

    /** Tipos de eventos auditados */
    public enum TipoEvento {
        SALVAR("Simulado salvo"),
        LEITURA("Simulado consultado"),
        REMOCAO("Simulado removido");

        private final String descricao;

        TipoEvento(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    // Lista thread-safe para armazenar eventos (em producao seria persistido no banco)
    private final List<EventoAuditoria> eventos = new CopyOnWriteArrayList<>();

    // Limite maximo de eventos em memoria (evita consumo excessivo)
    private static final int MAX_EVENTOS = 1000;

    @Override
    public void registrarSalvar(Simulado s) {
        var evento = new EventoAuditoria(
            LocalDateTime.now(),
            TipoEvento.SALVAR,
            s.getId() != null ? s.getId().value() : null,
            s.getTurma().value(),
            s.getStatus().name(),
            String.format("Simulado %s para turma %d (status: %s)",
                s.getId() != null ? "atualizado" : "criado",
                s.getTurma().value(),
                s.getStatus().name())
        );
        adicionarEvento(evento);
        
        // Tambem imprime no console (comportamento original)
        System.out.printf("[%s] Auditoria: %s%n", evento.dataHora(), evento.descricao());
    }

    @Override
    public void registrarLeitura(SimuladoId id) {
        var evento = new EventoAuditoria(
            LocalDateTime.now(),
            TipoEvento.LEITURA,
            id.value(),
            null,
            null,
            String.format("Simulado %d consultado", id.value())
        );
        adicionarEvento(evento);
        
        System.out.printf("[%s] Auditoria: %s%n", evento.dataHora(), evento.descricao());
    }

    @Override
    public void registrarRemocao(SimuladoId id) {
        var evento = new EventoAuditoria(
            LocalDateTime.now(),
            TipoEvento.REMOCAO,
            id.value(),
            null,
            null,
            String.format("Simulado %d removido", id.value())
        );
        adicionarEvento(evento);
        
        System.out.printf("[%s] Auditoria: %s%n", evento.dataHora(), evento.descricao());
    }

    private void adicionarEvento(EventoAuditoria evento) {
        eventos.add(evento);
        
        // Remove eventos antigos se ultrapassar o limite
        while (eventos.size() > MAX_EVENTOS) {
            eventos.remove(0);
        }
    }

    /**
     * Retorna todos os eventos de auditoria (mais recentes primeiro).
     * @return Lista imutavel de eventos
     */
    public List<EventoAuditoria> listarEventos() {
        var lista = new ArrayList<>(eventos);
        Collections.reverse(lista); // Mais recentes primeiro
        return Collections.unmodifiableList(lista);
    }

    /**
     * Retorna os ultimos N eventos de auditoria.
     * @param limite Quantidade maxima de eventos
     * @return Lista imutavel de eventos
     */
    public List<EventoAuditoria> listarEventos(int limite) {
        var todos = listarEventos();
        return todos.subList(0, Math.min(limite, todos.size()));
    }

    /**
     * Limpa todos os eventos (util para testes).
     */
    public void limpar() {
        eventos.clear();
    }

    /**
     * Retorna a quantidade de eventos armazenados.
     */
    public int contarEventos() {
        return eventos.size();
    }
}

