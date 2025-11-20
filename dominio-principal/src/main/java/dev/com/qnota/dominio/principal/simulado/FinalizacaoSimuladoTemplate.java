/* Título da análise: QNota - Template Method + Observer na finalização de Simulado */
package dev.com.qnota.dominio.principal.simulado;

import java.util.ArrayList;
import java.util.List;

/**
 * Define o esqueleto do algoritmo de finalização de um simulado.
 * Agora também atua como "Subject" do padrão Observer, notificando
 * observadores após a finalização bem-sucedida.
 */
public abstract class FinalizacaoSimuladoTemplate {

    private final List<SimuladoObserver> observers = new ArrayList<>();

    /**
     * Registra um observer para ser notificado após a finalização.
     */
    public void registrarObserver(SimuladoObserver observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    /**
     * Método Template: representa o fluxo completo de finalização.
     * Passos:
     * 1) Carregar o simulado
     * 2) Validar se já está finalizado
     * 3) Validar pré-condições de finalização (RN-16 e outras)
     * 4) Executar gancho antes da finalização (opcional)
     * 5) Alterar estado para FINALIZADO
     * 6) Salvar o simulado
     * 7) Notificar observers
     * 8) Executar gancho após finalização (opcional)
     */
    public final void finalizar(SimuladoId id) {
        Simulado simulado = carregarSimulado(id);
        validarJaFinalizado(simulado);
        validarPreCondicoes(id, simulado);
        antesDeFinalizar(simulado, id);
        aplicarFinalizacao(simulado);
        salvar(simulado);
        notificarObservers(id);
        aposFinalizar(simulado, id);
    }

    /** Carrega o simulado a partir do repositório ou fonte correspondente. */
    protected abstract Simulado carregarSimulado(SimuladoId id);

    /**
     * Valida se o simulado já está finalizado.
     * Implementação padrão usa o status da entidade.
     */
    protected void validarJaFinalizado(Simulado s) {
        if (s.getStatus() == Simulado.Status.FINALIZADO) {
            throw new IllegalStateException("Simulado já está finalizado.");
        }
    }

    /**
     * Valida pré-condições de finalização específicas.
     * No QNota atual, aqui entra a RN-16 (todas as notas lançadas).
     */
    protected abstract void validarPreCondicoes(SimuladoId id, Simulado s);

    /**
     * Gancho opcional antes de mudar o estado do simulado.
     * Implementação padrão não faz nada.
     */
    protected void antesDeFinalizar(Simulado s, SimuladoId id) {
        // gancho opcional
    }

    /**
     * Passo responsável por alterar o estado do agregado para FINALIZADO.
     * Implementação padrão delega para o método de domínio do Simulado.
     */
    protected void aplicarFinalizacao(Simulado s) {
        s.finalizar();
    }

    /** Persiste o simulado após a finalização. */
    protected abstract void salvar(Simulado s);

    /**
     * Notifica todos os observers registrados de que o simulado foi finalizado.
     */
    protected void notificarObservers(SimuladoId id) {
        for (SimuladoObserver observer : observers) {
            observer.aoFinalizarSimulado(id);
        }
    }

    /**
     * Gancho opcional após a finalização.
     */
    protected void aposFinalizar(Simulado s, SimuladoId id) {
        // gancho opcional
    }
}
