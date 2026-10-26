package cl.duoc.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CheckoutItemRequest(
    @NotBlank String sku,
    @NotNull @Positive Integer cantidad
) {}