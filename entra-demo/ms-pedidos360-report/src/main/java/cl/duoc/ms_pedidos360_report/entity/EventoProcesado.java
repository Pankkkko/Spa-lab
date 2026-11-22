package cl.duoc.ms_pedidos360_report.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "eventos_procesados")
public class EventoProcesado {

    @Id
    @Column(length = 100, nullable = false)
    private String eventId;

    @Column(length = 100, nullable = false)
    private String eventType;

    private Long pedidoId;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(nullable = false)
    private Instant processedAt;

    @Column(columnDefinition = "LONGTEXT")
    private String payload;

    public EventoProcesado() {
    }

    public EventoProcesado(
            String eventId,
            String eventType,
            Long pedidoId,
            Instant occurredAt,
            Instant processedAt,
            String payload) {

        this.eventId = eventId;
        this.eventType = eventType;
        this.pedidoId = pedidoId;
        this.occurredAt = occurredAt;
        this.processedAt = processedAt;
        this.payload = payload;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public String getPayload() {
        return payload;
    }
}