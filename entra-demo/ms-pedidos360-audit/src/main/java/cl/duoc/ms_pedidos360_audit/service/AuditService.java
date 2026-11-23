package cl.duoc.ms_pedidos360_audit.service;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.ms_pedidos360_audit.dto.AuditRegistroDetalleResponse;
import cl.duoc.ms_pedidos360_audit.dto.AuditRegistroResponse;
import cl.duoc.ms_pedidos360_audit.entity.RegistroAuditoria;
import cl.duoc.ms_pedidos360_audit.repository.RegistroAuditoriaRepository;

@Service
public class AuditService {

    private final RegistroAuditoriaRepository auditoriaRepository;

    public AuditService(RegistroAuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Transactional(readOnly = true)
    public Page<AuditRegistroResponse> buscarRegistros(
            String usuarioId,
            String tipoEvento,
            Instant desde,
            Instant hasta,
            int page,
            int size
    ) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException(
                "La fecha inicial no puede ser posterior a la fecha final"
            );
        }

        if (page < 0) {
            throw new IllegalArgumentException("La página no puede ser negativa");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                "El tamaño de página debe estar entre 1 y 100"
            );
        }

        String usuario = normalizarFiltro(usuarioId);
        String tipo = normalizarFiltro(tipoEvento);

        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "fechaEvento")
        );

        return auditoriaRepository.buscarConFiltros(
                usuario,
                tipo,
                desde,
                hasta,
                pageable
            )
            .map(this::convertirAResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuditRegistroResponse> buscarPorPedido(
            String pedidoId,
            int page,
            int size
    ) {
        if (pedidoId == null || pedidoId.isBlank()) {
            throw new IllegalArgumentException("El ID del pedido es obligatorio");
        }

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Parámetros de paginación inválidos");
        }

        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "fechaEvento")
        );

        return auditoriaRepository
            .findByEntidadAndEntidadIdOrderByFechaEventoDesc(
                "PEDIDO",
                pedidoId,
                pageable
            )
            .map(this::convertirAResponse);
    }

    private String normalizarFiltro(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private AuditRegistroResponse convertirAResponse(
            RegistroAuditoria registro
    ) {
        return new AuditRegistroResponse(
            registro.getId(),
            registro.getEventId(),
            registro.getUsuarioId(),
            registro.getUsuarioNombre(),
            registro.getClienteId(),
            registro.getTipoEvento(),
            registro.getEntidad(),
            registro.getEntidadId(),
            registro.getDescripcion(),
            registro.getFechaEvento(),
            registro.getOrigen(),
            registro.getTraceId(),
            registro.getCorrelationId()
        );
    }

    @Transactional(readOnly = true)
    public AuditRegistroDetalleResponse buscarPorId(Long id) {

        RegistroAuditoria registro = auditoriaRepository.findById(id)
            .orElseThrow(() -> new java.util.NoSuchElementException(
                "No se encontró un registro de auditoría con ID: " + id
            ));

        return new AuditRegistroDetalleResponse(
            registro.getId(),
            registro.getEventId(),
            registro.getUsuarioId(),
            registro.getUsuarioNombre(),
            registro.getClienteId(),
            registro.getTipoEvento(),
            registro.getEntidad(),
            registro.getEntidadId(),
            registro.getDescripcion(),
            registro.getFechaEvento(),
            registro.getOrigen(),
            registro.getTraceId(),
            registro.getCorrelationId(),
            registro.getPayload(),
            registro.getFechaRecepcion()
        );
    }
}