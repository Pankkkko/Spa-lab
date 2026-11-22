package cl.duoc.ms_pedidos360_report.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.ms_pedidos360_report.entity.VentaPorHora;

public interface VentaPorHoraRepository
        extends JpaRepository<VentaPorHora, Long> {

    Optional<VentaPorHora> findByHoraInicio(
            LocalDateTime horaInicio
    );
    List<VentaPorHora> findAllByOrderByHoraInicioAsc();

    List<VentaPorHora> findAllByHoraInicioBetweenOrderByHoraInicioAsc(
            LocalDateTime desde,
            LocalDateTime hasta
    );
}