package cl.duoc.ms_pedidos360_report.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.ms_pedidos360_report.entity.ProductoVenta;

public interface ProductoVentaRepository
        extends JpaRepository<ProductoVenta, Long> {

    Optional<ProductoVenta> findByProductoKeyAndFechaVenta(
            String productoKey,
            LocalDate fechaVenta
    );

    List<ProductoVenta> findAllByFechaVenta(LocalDate fechaVenta);

    List<ProductoVenta> findTop10ByOrderByUnidadesVendidasDescMontoTotalDesc();

    List<ProductoVenta> findTop10ByFechaVentaBetweenOrderByUnidadesVendidasDescMontoTotalDesc(
            LocalDate desde,
            LocalDate hasta
    );
}