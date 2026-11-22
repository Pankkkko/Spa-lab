
package cl.duoc.ms_pedidos360_audit.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "registros_auditoria",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_auditoria_event_id",
            columnNames = "event_id"
        )
    },
    indexes = {
        @Index(
            name = "idx_auditoria_usuario",
            columnList = "usuario_id"
        ),
        @Index(
            name = "idx_auditoria_fecha",
            columnList = "fecha_evento"
        ),
        @Index(
            name = "idx_auditoria_tipo_evento",
            columnList = "tipo_evento"
        ),
        @Index(
            name = "idx_auditoria_entidad",
            columnList = "entidad, entidad_id"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @Column(name = "usuario_id", length = 100)
    private String usuarioId;

    @Column(name = "usuario_nombre", length = 150)
    private String usuarioNombre;

    @Column(name = "tipo_evento", nullable = false, length = 100)
    private String tipoEvento;

    @Column(name = "entidad", nullable = false, length = 80)
    private String entidad;

    @Column(name = "entidad_id", nullable = false, length = 100)
    private String entidadId;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "fecha_evento", nullable = false)
    private Instant fechaEvento;

    @Column(name = "origen", length = 150)
    private String origen;

    @Column(name = "trace_id", length = 100)
    private String traceId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "payload", columnDefinition = "LONGTEXT")
    private String payload;

    @Column(name = "fecha_recepcion", nullable = false)
    private Instant fechaRecepcion;
}
