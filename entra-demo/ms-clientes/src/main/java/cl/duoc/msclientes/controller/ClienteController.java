package cl.duoc.msclientes.controller;

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

import cl.duoc.msclientes.dto.ClienteResponse;
import cl.duoc.msclientes.service.ClienteService;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // 1. Obtener cliente por ID
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtenerClientePorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    // 2. Listar todos los clientes
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listarClientes() {
        return ResponseEntity.ok(clienteService.listarClientes());
    }

    // 3. Agregar cliente
    @PostMapping
    public ResponseEntity<ClienteResponse> agregarCliente(@RequestBody ClienteResponse request) {
        ClienteResponse creado = clienteService.agregarCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // 4. Editar cliente
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> editarCliente(
        @PathVariable Long id,
        @RequestBody ClienteResponse request
    ) {
        return ResponseEntity.ok(clienteService.editarCliente(id, request));
    }

    // 5. Eliminar cliente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }
}