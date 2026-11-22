package cl.duoc.ms_pedidos360_report.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.ms_pedidos360_report.entity.EstadoPedido;
import cl.duoc.ms_pedidos360_report.entity.PedidoResumen;

public interface PedidoResumenRepository
        extends JpaRepository<PedidoResumen, Long> {

    long countByEstado(EstadoPedido estado);

    List<PedidoResumen> findByFechaEntregaIsNotNull();
}