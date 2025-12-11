package dev.com.qnota.apresentacao.principal.auditoria;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.com.qnota.dominio.principal.simulado.SimuladoAuditoriaArmazenada;
import dev.com.qnota.dominio.principal.simulado.SimuladoAuditoriaArmazenada.EventoAuditoria;
import dev.com.qnota.dominio.principal.simulado.SimuladoAuditoriaArmazenada.TipoEvento;

/**
 * Controlador REST para expor os eventos de auditoria.
 * 
 * Este controlador demonstra como o padrao Decorator beneficia a aplicacao:
 * - O SimuladoRepositorioDecorator intercepta operacoes do repositorio
 * - Os eventos sao armazenados em SimuladoAuditoriaArmazenada
 * - Este controlador expoe esses eventos para o frontend
 * 
 * Fluxo: Frontend -> AuditoriaControlador -> SimuladoAuditoriaArmazenada
 */
@RestController
@RequestMapping("backend/auditoria")
class AuditoriaControlador {

    private final SimuladoAuditoriaArmazenada auditoria;

    AuditoriaControlador(SimuladoAuditoriaArmazenada auditoria) {
        this.auditoria = auditoria;
    }

    // DTO para transferencia de dados
    record EventoAuditoriaDto(
        String dataHora,
        String tipo,
        String tipoDescricao,
        Integer simuladoId,
        Integer turmaId,
        String status,
        String descricao
    ) {
        static EventoAuditoriaDto from(EventoAuditoria evento) {
            return new EventoAuditoriaDto(
                evento.dataHora().toString(),
                evento.tipo().name(),
                evento.tipo().getDescricao(),
                evento.simuladoId(),
                evento.turmaId(),
                evento.status(),
                evento.descricao()
            );
        }
    }

    // Resumo estatistico
    record AuditoriaResumoDto(
        int totalEventos,
        int salvamentos,
        int leituras,
        int remocoes,
        String ultimoEvento
    ) {}

    /**
     * Lista todos os eventos de auditoria.
     * @param limite Quantidade maxima de eventos (opcional, padrao 100)
     * @return Lista de eventos ordenada por data (mais recente primeiro)
     */
    @RequestMapping(method = GET, path = "eventos")
    List<EventoAuditoriaDto> listarEventos(
            @RequestParam(value = "limite", defaultValue = "100") int limite) {
        return auditoria.listarEventos(limite).stream()
                .map(EventoAuditoriaDto::from)
                .toList();
    }

    /**
     * Retorna um resumo estatistico dos eventos de auditoria.
     */
    @RequestMapping(method = GET, path = "resumo")
    AuditoriaResumoDto resumo() {
        var eventos = auditoria.listarEventos();
        
        int salvamentos = (int) eventos.stream()
                .filter(e -> e.tipo() == TipoEvento.SALVAR)
                .count();
        int leituras = (int) eventos.stream()
                .filter(e -> e.tipo() == TipoEvento.LEITURA)
                .count();
        int remocoes = (int) eventos.stream()
                .filter(e -> e.tipo() == TipoEvento.REMOCAO)
                .count();
        
        String ultimoEvento = eventos.isEmpty() ? null : eventos.get(0).dataHora().toString();
        
        return new AuditoriaResumoDto(
            eventos.size(),
            salvamentos,
            leituras,
            remocoes,
            ultimoEvento
        );
    }
}

