
package cl.duoc.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CompraDetalleRequest(
    @NotBlank(message = "El SKU es obligatorio")
    String sku,

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    Integer cantidad
) {}
