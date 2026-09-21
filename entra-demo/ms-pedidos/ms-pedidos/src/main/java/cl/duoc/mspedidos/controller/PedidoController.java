package cl.duoc.mspedidos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.mspedidos.dto.PedidoResponse;
import cl.duoc.mspedidos.service.PedidoService;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
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

    // 4. Agregar pedido
    @PostMapping
    public ResponseEntity<PedidoResponse> agregarPedido(@RequestBody PedidoResponse request) {
        PedidoResponse creado = pedidoService.agregarPedido(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // 5. Editar pedido
    @PutMapping("/{id}")
    public ResponseEntity<PedidoResponse> editarPedido(
        @PathVariable Long id,
        @RequestBody PedidoResponse request
    ) {
        return ResponseEntity.ok(pedidoService.editarPedido(id, request));
    }

    // 6. Eliminar pedido
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPedido(@PathVariable Long id) {
        pedidoService.eliminarPedido(id);
        return ResponseEntity.noContent().build();
    }
}