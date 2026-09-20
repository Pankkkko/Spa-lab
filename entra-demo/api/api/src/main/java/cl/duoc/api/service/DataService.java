package cl.duoc.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import cl.duoc.api.dto.ClienteDetalleResponse;
import cl.duoc.api.dto.ClienteResponse;
import cl.duoc.api.dto.PedidoResponse;
import cl.duoc.api.repository.ClienteRepository;
import cl.duoc.api.repository.PedidoRepository;

@Service
public class DataService {

    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;

    public DataService(ClienteRepository clienteRepository, PedidoRepository pedidoRepository) {
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public ClienteDetalleResponse obtenerData(Long clienteId) {
        ClienteResponse cliente = clienteRepository.buscarPorId(clienteId);
        List<PedidoResponse> pedidos = pedidoRepository.buscarPorCliente(clienteId);

        List<PedidoResponse> pedidosEnriquecidos = pedidos.stream()
            .map(p -> new PedidoResponse(
                p.id(),
                p.clienteId(),
                cliente.nombre(),
                p.estado(),
                p.fecha(),
                p.total(),
                p.detalles()
            ))
            .collect(Collectors.toList());

        return new ClienteDetalleResponse(
            cliente.id(),
            cliente.nombre(),
            cliente.email(),
            cliente.telefono(),
            pedidosEnriquecidos
        );
    }
}