package cl.duoc.ms_pedidos360_catalog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.ms_pedidos360_catalog.models.Producto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCase(String sku);

    List<Producto> findByCategoriaIgnoreCase(String categoria);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    Page<Producto> findByCategoriaIgnoreCase(String categoria, Pageable pageable);

    Page<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    Page<Producto> findByPrecioBetween(BigDecimal min, BigDecimal max, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE Producto p
       SET p.stock = p.stock - :cantidad
     WHERE p.sku = :sku
       AND p.stock >= :cantidad
    """)
    int reservarStock(
            @Param("sku") String sku,
            @Param("cantidad") Integer cantidad
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE Producto p
       SET p.stock = p.stock + :cantidad
     WHERE p.sku = :sku
    """)
    int liberarStock(
            @Param("sku") String sku,
            @Param("cantidad") Integer cantidad
    );

}
