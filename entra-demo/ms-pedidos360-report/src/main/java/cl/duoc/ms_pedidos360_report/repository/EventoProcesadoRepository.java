package cl.duoc.ms_pedidos360_report.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.ms_pedidos360_report.entity.EventoProcesado;

public interface EventoProcesadoRepository
        extends JpaRepository<EventoProcesado, String> {

    boolean existsByEventId(String eventId);
}