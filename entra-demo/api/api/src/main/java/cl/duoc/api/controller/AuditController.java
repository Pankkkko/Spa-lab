
package cl.duoc.api.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.api.dto.AuditRegistroDetalleResponse;
import cl.duoc.api.dto.AuditRegistroResponse;
import cl.duoc.api.dto.PageResponse;
import cl.duoc.api.repository.AuditRepository;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditRepository auditRepository;

    public AuditController(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    /**
     * Obtiene registros de auditoría aplicando filtros opcionales.
     */
    @GetMapping
    public ResponseEntity<PageResponse<AuditRegistroResponse>> buscarRegistros(
            @RequestParam(required = false) String usuarioId,
            @RequestParam(required = false) String tipoEvento,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<AuditRegistroResponse> resultado =
                auditRepository.buscarRegistros(
                        usuarioId,
                        tipoEvento,
                        desde,
                        hasta,
                        page,
                        size
                );

        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtiene el historial de auditoría de un pedido específico.
     */
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<PageResponse<AuditRegistroResponse>> buscarPorPedido(
            @PathVariable String pedidoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<AuditRegistroResponse> resultado =
                auditRepository.buscarPorPedido(pedidoId, page, size);

        return ResponseEntity.ok(resultado);
    }

    /**
     * Obtiene el detalle completo de un registro de auditoría.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuditRegistroDetalleResponse> buscarPorId(
            @PathVariable Long id) {

        AuditRegistroDetalleResponse resultado =
                auditRepository.buscarPorId(id);

        return ResponseEntity.ok(resultado);
    }
}
