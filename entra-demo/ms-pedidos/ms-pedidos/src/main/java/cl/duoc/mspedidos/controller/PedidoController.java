package cl.duoc.mspedidos.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.mspedidos.dto.PedidoCompraInternaRequest;
import cl.duoc.mspedidos.dto.PedidoResponse;
import cl.duoc.mspedidos.service.PedidoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final String internalApiKey;

    public PedidoController(
            PedidoService pedidoService,
            @Value("${internal.api-key}") String internalApiKey
    ) {
        this.pedidoService = pedidoService;
        this.internalApiKey = internalApiKey;
    }

    // 1. Obtener pedido por ID
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.buscarPorId(id));
    }

    // 2. Listar todos los pedidos
    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarPedidos() {
        return ResponseEntity.ok(pedidoService.listarPedidos());
    }

    // 3. Listar pedidos por cliente
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PedidoResponse>> buscarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(pedidoService.buscarPorCliente(clienteId));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PedidoResponse>> buscarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(pedidoService.buscarPorEstado(estado));
    }

    // 4. Agregar pedido
    @PostMapping
    public ResponseEntity<PedidoResponse> agregarPedido(
            @Valid @RequestBody PedidoResponse request) {

        PedidoResponse creado = pedidoService.agregarPedido(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // 5. Editar pedido
    @PutMapping("/{id}")
    public ResponseEntity<PedidoResponse> editarPedido(
            @PathVariable Long id,
            @Valid @RequestBody PedidoResponse request) {

        PedidoResponse actualizado = pedidoService.editarPedido(id, request);

        return ResponseEntity.ok(actualizado);
    }

    // 6. Eliminar pedido
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPedido(@PathVariable Long id) {
        pedidoService.eliminarPedido(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PedidoResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String nuevoEstado = body.get("status");
        if (nuevoEstado == null) {
            nuevoEstado = body.get("estado");
        }
        return ResponseEntity.ok(pedidoService.cambiarEstado(id, nuevoEstado));
    }

    @PostMapping("/checkout-interno")
    public ResponseEntity<PedidoResponse> crearCompraInterna(
            @RequestHeader(
                    value = "X-Internal-Api-Key",
                    required = false
            ) String apiKey,
            @Valid @RequestBody PedidoCompraInternaRequest request
    ) {
        if (apiKey == null || !MessageDigest.isEqual(
                internalApiKey.getBytes(StandardCharsets.UTF_8),
                apiKey.getBytes(StandardCharsets.UTF_8)
        )) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PedidoResponse creado = pedidoService.crearCompraInterna(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
