
package cl.duoc.mspedidos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PedidoResponse(
    Long id,

    @NotNull(message = "El ID del cliente es obligatorio")
    @Positive(message = "El ID del cliente debe ser mayor que cero")
    Long clienteId,

    String clienteNombre,
    String estado,
    LocalDate fecha,
    BigDecimal total,

    @NotEmpty(message = "El pedido debe contener al menos un producto")
    List<@Valid PedidoDetalleResponse> detalles
) {}
