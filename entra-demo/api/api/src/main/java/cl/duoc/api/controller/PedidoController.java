package cl.duoc.api.controller;

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

import cl.duoc.api.dto.PedidoResponse;
import cl.duoc.api.repository.PedidoRepository;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoRepository pedidoRepository;

    public PedidoController(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listar() {
        return ResponseEntity.ok(pedidoRepository.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoRepository.buscarPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PedidoResponse>> buscarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(pedidoRepository.buscarPorCliente(clienteId));
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> crear(@RequestBody PedidoResponse request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoRepository.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PedidoResponse> actualizar(
        @PathVariable Long id,
        @RequestBody PedidoResponse request
    ) {
        return ResponseEntity.ok(pedidoRepository.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pedidoRepository.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}