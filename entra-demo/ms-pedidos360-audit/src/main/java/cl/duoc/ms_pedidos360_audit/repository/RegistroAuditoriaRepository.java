
package cl.duoc.ms_pedidos360_audit.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.duoc.ms_pedidos360_audit.entity.RegistroAuditoria;

public interface RegistroAuditoriaRepository
        extends JpaRepository<RegistroAuditoria, Long> {

    boolean existsByEventId(String eventId);

    Optional<RegistroAuditoria> findByEventId(String eventId);

    Page<RegistroAuditoria> findAllByOrderByFechaEventoDesc(
            Pageable pageable
    );

    Page<RegistroAuditoria> findByUsuarioIdOrderByFechaEventoDesc(
            String usuarioId,
            Pageable pageable
    );

    Page<RegistroAuditoria> findByTipoEventoOrderByFechaEventoDesc(
            String tipoEvento,
            Pageable pageable
    );

    Page<RegistroAuditoria> findByFechaEventoBetweenOrderByFechaEventoDesc(
            Instant desde,
            Instant hasta,
            Pageable pageable
    );

    Page<RegistroAuditoria> findByEntidadAndEntidadIdOrderByFechaEventoDesc(
            String entidad,
            String entidadId,
            Pageable pageable
    );

    @Query("""
        SELECT r
        FROM RegistroAuditoria r
        WHERE (:usuarioId IS NULL OR r.usuarioId = :usuarioId)
          AND (:tipoEvento IS NULL OR r.tipoEvento = :tipoEvento)
          AND (:desde IS NULL OR r.fechaEvento >= :desde)
          AND (:hasta IS NULL OR r.fechaEvento <= :hasta)
        ORDER BY r.fechaEvento DESC
        """)
    Page<RegistroAuditoria> buscarConFiltros(
            @Param("usuarioId") String usuarioId,
            @Param("tipoEvento") String tipoEvento,
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta,
            Pageable pageable
    );
}
