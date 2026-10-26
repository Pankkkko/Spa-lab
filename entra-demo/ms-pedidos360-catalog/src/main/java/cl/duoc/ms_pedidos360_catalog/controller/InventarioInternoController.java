
package cl.duoc.ms_pedidos360_catalog.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import cl.duoc.ms_pedidos360_catalog.dto.ReservaStockRequest;
import cl.duoc.ms_pedidos360_catalog.service.ProductoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/internal/inventario")
public class InventarioInternoController {

    private final ProductoService productoService;
    private final String apiKey;

    public InventarioInternoController(
            ProductoService productoService,
            @Value("${internal.api-key}") String apiKey
    ) {
        this.productoService = productoService;
        this.apiKey = apiKey;
    }

    @PostMapping("/reservar")
    public ResponseEntity<Void> reservar(
            @RequestHeader(
                    value = "X-Internal-Api-Key",
                    required = false
            ) String suppliedKey,
            @Valid @RequestBody ReservaStockRequest request
    ) {
        validarClave(suppliedKey);
        productoService.reservarStock(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/liberar")
    public ResponseEntity<Void> liberar(
            @RequestHeader(
                    value = "X-Internal-Api-Key",
                    required = false
            ) String suppliedKey,
            @Valid @RequestBody ReservaStockRequest request
    ) {
        validarClave(suppliedKey);
        productoService.liberarStock(request);
        return ResponseEntity.noContent().build();
    }

    private void validarClave(String suppliedKey) {
        if (suppliedKey == null
                || !MessageDigest.isEqual(
                        apiKey.getBytes(StandardCharsets.UTF_8),
                        suppliedKey.getBytes(StandardCharsets.UTF_8)
                )) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales internas inválidas"
            );
        }
    }
}
