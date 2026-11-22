
package cl.duoc.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductoTopResponse(
    String productoKey,
    String productoNombre,
    long unidadesVendidas,
    BigDecimal montoTotal,
    LocalDate fechaVenta
) {}
