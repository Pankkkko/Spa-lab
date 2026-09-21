package cl.duoc.msclientes.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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

    // Mapper entidad -> DTO
    private ClienteResponse toResponse(Cliente c) {
        return new ClienteResponse(c.getId(), c.getNombre(), c.getEmail(), c.getTelefono());
    }
}