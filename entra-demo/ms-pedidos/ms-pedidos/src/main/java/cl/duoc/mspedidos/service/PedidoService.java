package cl.duoc.mspedidos.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import cl.duoc.mspedidos.dto.PedidoDetalleResponse;
import cl.duoc.mspedidos.dto.PedidoResponse;
import cl.duoc.mspedidos.entity.Pedido;
import cl.duoc.mspedidos.entity.PedidoDetalle;
import cl.duoc.mspedidos.repository.PedidoRepository;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    // GET por cliente
    public List<PedidoResponse> buscarPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // GET por ID
    public PedidoResponse buscarPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + id));
        return toResponse(pedido);
    }

    // GET todos
    public List<PedidoResponse> listarPedidos() {
        return pedidoRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // POST
    public PedidoResponse agregarPedido(PedidoResponse request) {
        Pedido pedido = new Pedido(request.clienteId(), request.estado(), request.fecha());
        if (request.detalles() != null) {
            for (PedidoDetalleResponse d : request.detalles()) {
                PedidoDetalle detalle = new PedidoDetalle(d.producto(), d.cantidad(), d.precioUnitario());
                pedido.agregarDetalle(detalle);
            }
        }
        Pedido guardado = pedidoRepository.save(pedido);
        return toResponse(guardado);
    }

    // PUT
    public PedidoResponse editarPedido(Long id, PedidoResponse request) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + id));

        pedido.setClienteId(request.clienteId());
        pedido.setEstado(request.estado());
        pedido.setFecha(request.fecha());

        // Reemplazar detalles (orphanRemoval=true borra los viejos)
        pedido.getDetalles().clear();
        if (request.detalles() != null) {
            for (PedidoDetalleResponse d : request.detalles()) {
                PedidoDetalle detalle = new PedidoDetalle(d.producto(), d.cantidad(), d.precioUnitario());
                pedido.agregarDetalle(detalle);
            }
        }

        Pedido actualizado = pedidoRepository.save(pedido);
        return toResponse(actualizado);
    }

    // DELETE
    public void eliminarPedido(Long id) {
        if (!pedidoRepository.existsById(id)) {
            throw new RuntimeException("Pedido no encontrado: " + id);
        }
        pedidoRepository.deleteById(id);
    }

    // Mapper entidad -> DTO
    private PedidoResponse toResponse(Pedido p) {
        List<PedidoDetalleResponse> detalles = p.getDetalles().stream()
            .map(d -> new PedidoDetalleResponse(
                d.getProducto(),
                d.getCantidad(),
                d.getPrecioUnitario(),
                d.getSubtotal()
            ))
            .collect(Collectors.toList());

        Double total = detalles.stream()
            .mapToDouble(PedidoDetalleResponse::subtotal)
            .sum();

        return new PedidoResponse(
            p.getId(),
            p.getClienteId(),
            null, // clienteNombre se enriquece en el BFF
            p.getEstado(),
            p.getFecha(),
            total,
            detalles
        );
    }
}