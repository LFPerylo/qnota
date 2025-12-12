/* Título da análise: QNota - Padrão Observer na finalização de Simulado */
package dev.com.qnota.dominio.principal.simulado;

/**
 * Observer de eventos relacionados a Simulado.
 * No contexto atual, é notificado quando um simulado é finalizado.
 */
public interface SimuladoObserver {

    /**
     * Chamado após a finalização bem-sucedida de um simulado.
     */
    void aoFinalizarSimulado(SimuladoId id);
}
