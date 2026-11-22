
package cl.duoc.ms_pedidos360_audit.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.ms_pedidos360_audit.dto.AuditRegistroDetalleResponse;
import cl.duoc.ms_pedidos360_audit.dto.AuditRegistroResponse;
import cl.duoc.ms_pedidos360_audit.service.AuditService;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public Page<AuditRegistroResponse> buscarRegistros(
            @RequestParam(required = false) String usuarioId,
            @RequestParam(required = false) String tipoEvento,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            Instant fechaDesde = desde == null
                ? null
                : desde.atStartOfDay().toInstant(ZoneOffset.UTC);

            Instant fechaHasta = hasta == null
                ? null
                : hasta.plusDays(1)
                    .atStartOfDay()
                    .toInstant(ZoneOffset.UTC)
                    .minusNanos(1);

            return auditService.buscarRegistros(
                usuarioId,
                tipoEvento,
                fechaDesde,
                fechaHasta,
                page,
                size
            );

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        }
    }

    @GetMapping("/pedido/{pedidoId}")
    public Page<AuditRegistroResponse> buscarPorPedido(
            @PathVariable String pedidoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            return auditService.buscarPorPedido(pedidoId, page, size);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        }
    }

    @GetMapping("/{id}")
public AuditRegistroDetalleResponse buscarPorId(@PathVariable Long id) {

    try {
        return auditService.buscarPorId(id);

    } catch (java.util.NoSuchElementException e) {
        throw new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            e.getMessage()
        );
    }
}
}
