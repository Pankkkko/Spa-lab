package cl.duoc.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductoRequest(
    String sku,
    String nombre,
    String descripcion,
    String categoria,
    BigDecimal precio,
    List<String> imagenes,
    Integer stock
) {
}