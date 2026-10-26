
package cl.duoc.ms_pedidos360_catalog.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReservaStockRequest {

    @NotEmpty(message = "La reserva debe contener productos")
    private List<@Valid ReservaStockDetalleRequest> detalles;
}
