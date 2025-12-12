/* Título da análise: QNota - Serviço de auditoria para operações de Simulado */
package dev.com.qnota.dominio.principal.simulado;

public interface SimuladoAuditoria {

    void registrarSalvar(Simulado s);

    void registrarLeitura(SimuladoId id);

    void registrarRemocao(SimuladoId id);
}
