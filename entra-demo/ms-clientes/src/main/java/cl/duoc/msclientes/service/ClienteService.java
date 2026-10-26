package cl.duoc.msclientes.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import cl.duoc.msclientes.dto.ClientePerfilRequest;
import cl.duoc.msclientes.dto.ClienteResponse;
import cl.duoc.msclientes.entity.Cliente;
import cl.duoc.msclientes.repository.ClienteRepository;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    // GET por ID
    public ClienteResponse buscarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + id));
        return toResponse(cliente);
    }

    // GET todos
    public List<ClienteResponse> listarClientes() {
        return clienteRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // POST
    public ClienteResponse agregarCliente(ClienteResponse request) {
        Cliente cliente = new Cliente(
                request.nombre(),
                request.email(),
                request.telefono()
        );
        Cliente guardado = clienteRepository.save(cliente);
        return toResponse(guardado);
    }

    // PUT
    public ClienteResponse editarCliente(Long id, ClienteResponse request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + id));
        cliente.setNombre(request.nombre());
        cliente.setEmail(request.email());
        cliente.setTelefono(request.telefono());
        Cliente actualizado = clienteRepository.save(cliente);
        return toResponse(actualizado);
    }

    // DELETE
    public void eliminarCliente(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado: " + id);
        }
        clienteRepository.deleteById(id);
    }

    public ClienteResponse buscarPorEntraObjectId(String entraObjectId) {
        Cliente cliente = clienteRepository
                .findByEntraObjectId(entraObjectId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El usuario todavía no tiene un perfil de cliente"
                ));

        return toResponse(cliente);
    }

    public ClienteResponse crearPerfil(ClientePerfilRequest request) {
        if (request.entraObjectId() == null
                || request.entraObjectId().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La identidad de Microsoft es obligatoria"
            );
        }

        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre es obligatorio"
            );
        }

        if (request.email() == null || request.email().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El correo es obligatorio"
            );
        }

        if (clienteRepository
                .findByEntraObjectId(request.entraObjectId())
                .isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta cuenta ya tiene un perfil de cliente"
            );
        }

        Cliente cliente = new Cliente(
                request.nombre().trim(),
                request.email().trim(),
                request.telefono()
        );

        cliente.setEntraObjectId(request.entraObjectId());

        return toResponse(clienteRepository.save(cliente));
    }

    // Mapper entidad -> DTO
    private ClienteResponse toResponse(Cliente c) {
        return new ClienteResponse(c.getId(), c.getNombre(), c.getEmail(), c.getTelefono());
    }
}
