/* Título da análise: QNota - Implementação simples de auditoria de Simulado */
package dev.com.qnota.dominio.principal.simulado;

import java.time.LocalDateTime;

public class SimuladoAuditoriaConsole implements SimuladoAuditoria {

    @Override
    public void registrarSalvar(Simulado s) {
        System.out.printf("[%s] Auditoria: salvar simulado (id=%s, turma=%s, status=%s)%n",
                LocalDateTime.now(),
                s.getId() != null ? s.getId().value() : "novo",
                s.getTurma().value(),
                s.getStatus());
    }

    @Override
    public void registrarLeitura(SimuladoId id) {
        System.out.printf("[%s] Auditoria: leitura de simulado (id=%d)%n",
                LocalDateTime.now(),
                id.value());
    }

    @Override
    public void registrarRemocao(SimuladoId id) {
        System.out.printf("[%s] Auditoria: remoção de simulado (id=%d)%n",
                LocalDateTime.now(),
                id.value());
    }
}
