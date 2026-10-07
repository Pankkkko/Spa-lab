package cl.duoc.ms_pedidos360_catalog.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductoRequestDTO implements Serializable {

    @NotBlank(message = "El SKU es obligatorio")
    private String sku;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La descripcion es obligatoria")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(
        value = "0.0",
        inclusive = false,
        message = "El precio debe ser mayor a 0"
    )
    private BigDecimal precio;

    @NotBlank(message = "La categoria es obligatoria")
    private String categoria;

    @NotEmpty(message = "Debe existir al menos una imagen")
    private List<String> imagenes;

    @NotNull(message = "El stock es obligatorio")
    @Min(
        value = 0,
        message = "El stock no puede ser negativo"
    )
    private Integer stock;
}