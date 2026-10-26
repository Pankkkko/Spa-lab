
package cl.duoc.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record CompraRequest(
    @NotEmpty(message = "El carrito no puede estar vacío")
    List<@Valid CompraDetalleRequest> detalles
) {}
