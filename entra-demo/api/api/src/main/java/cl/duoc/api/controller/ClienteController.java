package cl.duoc.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.api.dto.ClientePerfilInternoRequest;
import cl.duoc.api.dto.ClientePerfilRequest;
import cl.duoc.api.dto.ClienteResponse;
import cl.duoc.api.repository.ClienteRepository;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteRepository clienteRepository;

    public ClienteController(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar() {
        return ResponseEntity.ok(clienteRepository.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteRepository.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@RequestBody ClienteResponse request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteRepository.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizar(
            @PathVariable Long id,
            @RequestBody ClienteResponse request
    ) {
        return ResponseEntity.ok(clienteRepository.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteRepository.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Obtener el perfil del usuario autenticado
    @GetMapping("/me")
    public ResponseEntity<ClienteResponse> obtenerMiPerfil(
            @AuthenticationPrincipal Jwt jwt) {

        String entraObjectId = jwt.getClaimAsString("oid");

        if (entraObjectId == null || entraObjectId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "El token no contiene la identidad de Microsoft"
            );
        }

        return ResponseEntity.ok(
                clienteRepository.buscarPorIdentidad(entraObjectId)
        );
    }

// Registrar el perfil del usuario autenticado
    @PostMapping("/perfil")
    public ResponseEntity<ClienteResponse> crearMiPerfil(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ClientePerfilRequest request) {

        String entraObjectId = jwt.getClaimAsString("oid");

        if (entraObjectId == null || entraObjectId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "El token no contiene la identidad de Microsoft"
            );
        }

        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre es obligatorio"
            );
        }

        String email = jwt.getClaimAsString("email");

        if (email == null || email.isBlank()) {
            email = jwt.getClaimAsString("preferred_username");
        }

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El token no contiene un correo utilizable para el perfil"
            );
        }

        ClientePerfilInternoRequest perfil
                = new ClientePerfilInternoRequest(
                        entraObjectId,
                        request.nombre().trim(),
                        email.trim(),
                        request.telefono()
                );

        ClienteResponse creado = clienteRepository.crearPerfil(perfil);

        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }
}
