package cl.duoc.ms_pedidos360_report.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "productos_ventas",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"producto_key", "fecha_venta"}
        )
    }
)
public class ProductoVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "producto_key", nullable = false, length = 150)
    private String productoKey;

    @Column(length = 200)
    private String productoNombre;

    @Column(name = "fecha_venta", nullable = false)
    private LocalDate fechaVenta;

    @Column(nullable = false)
    private Long unidadesVendidas = 0L;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montoTotal = BigDecimal.ZERO;

    public ProductoVenta() {
    }

    public Long getId() {
        return id;
    }

    public String getProductoKey() {
        return productoKey;
    }

    public void setProductoKey(String productoKey) {
        this.productoKey = productoKey;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public LocalDate getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDate fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public Long getUnidadesVendidas() {
        return unidadesVendidas;
    }

    public void setUnidadesVendidas(Long unidadesVendidas) {
        this.unidadesVendidas = unidadesVendidas;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }
}