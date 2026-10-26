package cl.duoc.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record CheckoutRequest(
    @NotEmpty List<@Valid CheckoutItemRequest> items
) {}