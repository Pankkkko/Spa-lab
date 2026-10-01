package cl.duoc.mspedidos.service;

import cl.duoc.mspedidos.dto.PedidoDetalleResponse;
import cl.duoc.mspedidos.dto.PedidoEvento;
import cl.duoc.mspedidos.dto.PedidoResponse;
import cl.duoc.mspedidos.entity.Pedido;
import cl.duoc.mspedidos.entity.PedidoDetalle;
import cl.duoc.mspedidos.repository.PedidoRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final RabbitTemplate rabbitTemplate;

    public PedidoService(PedidoRepository pedidoRepository, RabbitTemplate rabbitTemplate) {
        this.pedidoRepository = pedidoRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public PedidoResponse agregarPedido(PedidoResponse request) {
        Pedido pedido = new Pedido(request.clienteId(), "CREADO", LocalDate.now());
        if (request.detalles() != null) {
            for (var d : request.detalles()) {
                pedido.agregarDetalle(new PedidoDetalle(d.producto(), d.cantidad(), d.precioUnitario()));
            }
        }

        Pedido guardado = pedidoRepository.save(pedido);

        PedidoEvento evento = PedidoEvento.of("OrderCreated", guardado.getId(), "CREADO");
        rabbitTemplate.convertAndSend("cmd.direct", "email.send", evento);
        rabbitTemplate.convertAndSend("cmd.direct", "kitchen.ticket", evento);
        rabbitTemplate.convertAndSend("cmd.direct", "invoice.gen", evento);

        return toResponse(guardado);
    }

    public PedidoResponse cambiarEstado(Long id, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + id));

        String estadoActual = pedido.getEstado();
        if ("DESPACHADO".equals(nuevoEstado) && !"ACEPTADO".equals(estadoActual)) {
            throw new RuntimeException("No se puede despachar sin aceptar");
        }

        pedido.setEstado(nuevoEstado);
        Pedido actualizado = pedidoRepository.save(pedido);

        PedidoEvento evento = PedidoEvento.of("Order" + nuevoEstado, actualizado.getId(), nuevoEstado);
        rabbitTemplate.convertAndSend("cmd.topic", "kitchen.ticket", evento);

        return toResponse(actualizado);
    }

    public PedidoResponse buscarPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + id));
        return toResponse(pedido);
    }

    public List<PedidoResponse> listarPedidos() {
        return pedidoRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<PedidoResponse> buscarPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public PedidoResponse editarPedido(Long id, PedidoResponse request) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + id));

        pedido.setClienteId(request.clienteId());
        pedido.setEstado(request.estado());
        pedido.setFecha(request.fecha() != null ? request.fecha() : LocalDate.now());

        pedido.getDetalles().clear();
        if (request.detalles() != null) {
            for (var d : request.detalles()) {
                PedidoDetalle detalle = new PedidoDetalle(d.producto(), d.cantidad(), d.precioUnitario());
                pedido.agregarDetalle(detalle);
            }
        }

        Pedido actualizado = pedidoRepository.save(pedido);
        return toResponse(actualizado);
    }

    public void eliminarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + id));
        pedidoRepository.delete(pedido);
    }

    private PedidoResponse toResponse(Pedido pedido) {
        return new PedidoResponse(
            pedido.getId(),
            pedido.getClienteId(),
            null,
            pedido.getEstado(),
            pedido.getFecha(),
            pedido.getDetalles().stream().mapToDouble(PedidoDetalle::getSubtotal).sum(),
            pedido.getDetalles().stream()
                .map(detalle -> new PedidoDetalleResponse(
                    detalle.getProducto(),
                    detalle.getCantidad(),
                    detalle.getPrecioUnitario(),
                    detalle.getSubtotal()))
                .collect(Collectors.toList())
        );
    }
}