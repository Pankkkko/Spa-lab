package cl.duoc.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.api.dto.CheckoutRequest;
import cl.duoc.api.dto.PedidoResponse;
import cl.duoc.api.service.CheckoutService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pedidos")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<PedidoResponse> checkout(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody CheckoutRequest request
    ) {
        String entraObjectId = jwt.getClaimAsString("oid");

        PedidoResponse pedido = checkoutService.realizarCheckout(
            entraObjectId,
            request
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(pedido);
    }
}