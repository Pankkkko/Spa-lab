package cl.duoc.mspedidos.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.mspedidos.config.RabbitMQConfig;
import cl.duoc.mspedidos.dto.ComandoEnvelope;
import cl.duoc.mspedidos.dto.PedidoCompraInternaRequest;
import cl.duoc.mspedidos.dto.PedidoDetalleCompraInternaRequest;
import cl.duoc.mspedidos.dto.PedidoDetalleResponse;
import cl.duoc.mspedidos.dto.PedidoResponse;
import cl.duoc.mspedidos.entity.Pedido;
import cl.duoc.mspedidos.entity.PedidoDetalle;
import cl.duoc.mspedidos.entity.PedidoEstado;
import cl.duoc.mspedidos.repository.PedidoRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public PedidoService(
            PedidoRepository pedidoRepository,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper
    ) {
        this.pedidoRepository = pedidoRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public PedidoResponse agregarPedido(PedidoResponse request) {
        Pedido pedido = new Pedido(
                request.clienteId(),
                "CREADO",
                LocalDate.now()
        );

        if (request.detalles() != null) {
            for (var detalle : request.detalles()) {
                pedido.agregarDetalle(
                        new PedidoDetalle(
                                detalle.producto(),
                                detalle.cantidad(),
                                detalle.precioUnitario()
                        )
                );
            }
        }

        Pedido guardado = pedidoRepository.save(pedido);

        String traceId = UUID.randomUUID().toString();
        String correlationId = UUID.randomUUID().toString();

        publicarComando(
                RabbitMQConfig.EXCHANGE_DIRECT,
                RabbitMQConfig.KEY_EMAIL,
                "EmailSolicitud",
                guardado,
                traceId,
                correlationId
        );

        publicarComando(
                RabbitMQConfig.EXCHANGE_DIRECT,
                RabbitMQConfig.KEY_PEDIDOS,
                "PedidoProcesar",
                guardado,
                traceId,
                correlationId
        );

        publicarComando(
                RabbitMQConfig.EXCHANGE_DIRECT,
                RabbitMQConfig.KEY_INVOICE,
                "FacturaGenerar",
                guardado,
                traceId,
                correlationId
        );

        return toResponse(guardado);
    }

    public PedidoResponse cambiarEstado(Long id, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(()
                        -> new RuntimeException("Pedido no encontrado: " + id));

        PedidoEstado.validarTransicion(
                pedido.getEstado(),
                nuevoEstado
        );

        String estadoAnterior = pedido.getEstado();

        pedido.setEstado(PedidoEstado.validar(nuevoEstado));
        Pedido actualizado = pedidoRepository.save(pedido);

        if (!estadoAnterior.equals(actualizado.getEstado())) {
            publicarCambioEstado(actualizado);
        }

        return toResponse(actualizado);
    }

    public PedidoResponse buscarPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(()
                        -> new RuntimeException("Pedido no encontrado: " + id));

        return toResponse(pedido);
    }

    public List<PedidoResponse> listarPedidos() {
        return pedidoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<PedidoResponse> buscarPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteId(clienteId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<PedidoResponse> buscarPorEstado(String estado) {
        String estadoValidado = PedidoEstado.validar(estado);

        List<String> estadosBuscados = new ArrayList<>();
        estadosBuscados.add(estadoValidado);

        if (PedidoEstado.CREADO.name().equals(estadoValidado)) {
            estadosBuscados.add("PENDIENTE");
        } else if (PedidoEstado.DESPACHADO.name().equals(estadoValidado)) {
            estadosBuscados.add("ENVIADO");
        }

        return pedidoRepository.findByEstadoIn(estadosBuscados)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public PedidoResponse editarPedido(Long id, PedidoResponse request) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(()
                        -> new RuntimeException("Pedido no encontrado: " + id));

        String estadoAnterior = pedido.getEstado();

        pedido.setClienteId(request.clienteId());

        PedidoEstado.validarTransicion(
                pedido.getEstado(),
                request.estado()
        );

        pedido.setEstado(PedidoEstado.validar(request.estado()));

        pedido.setFecha(
                request.fecha() != null
                ? request.fecha()
                : LocalDate.now()
        );

        pedido.getDetalles().clear();

        if (request.detalles() != null) {
            for (var detalle : request.detalles()) {
                pedido.agregarDetalle(
                        new PedidoDetalle(
                                detalle.producto(),
                                detalle.cantidad(),
                                detalle.precioUnitario()
                        )
                );
            }
        }

        Pedido actualizado = pedidoRepository.save(pedido);

        if (!estadoAnterior.equals(actualizado.getEstado())) {
            publicarCambioEstado(actualizado);
        }

        return toResponse(actualizado);
    }

    public void eliminarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(()
                        -> new RuntimeException("Pedido no encontrado: " + id));

        pedidoRepository.delete(pedido);
    }

    @Transactional
    public PedidoResponse crearCompraInterna(
            PedidoCompraInternaRequest request
    ) {
        if (request.detalles() == null || request.detalles().isEmpty()) {
            throw new IllegalArgumentException(
                    "El pedido debe contener al menos un producto"
            );
        }

        Pedido pedido = new Pedido(
                request.clienteId(),
                PedidoEstado.CREADO.name(),
                LocalDate.now()
        );

        for (PedidoDetalleCompraInternaRequest detalle : request.detalles()) {
            pedido.agregarDetalle(
                    new PedidoDetalle(
                            detalle.producto(),
                            detalle.cantidad(),
                            detalle.precioUnitario()
                    )
            );
        }

        Pedido guardado = pedidoRepository.saveAndFlush(pedido);

        String traceId = UUID.randomUUID().toString();
        String correlationId = UUID.randomUUID().toString();

        publicarComando(
                RabbitMQConfig.EXCHANGE_DIRECT,
                RabbitMQConfig.KEY_EMAIL,
                "EmailSolicitud",
                guardado,
                traceId,
                correlationId
        );

        publicarComando(
                RabbitMQConfig.EXCHANGE_DIRECT,
                RabbitMQConfig.KEY_PEDIDOS,
                "PedidoProcesar",
                guardado,
                traceId,
                correlationId
        );

        publicarComando(
                RabbitMQConfig.EXCHANGE_DIRECT,
                RabbitMQConfig.KEY_INVOICE,
                "FacturaGenerar",
                guardado,
                traceId,
                correlationId
        );

        return toResponse(guardado);
    }

    private void publicarCambioEstado(Pedido pedido) {
        String traceId = UUID.randomUUID().toString();
        String correlationId = UUID.randomUUID().toString();

        publicarComando(
                RabbitMQConfig.EXCHANGE_TOPIC,
                "pedidos.estado.actualizado",
                "PedidoEstadoActualizado",
                pedido,
                traceId,
                correlationId
        );
    }

    private void publicarComando(
            String exchange,
            String routingKey,
            String tipo,
            Pedido pedido,
            String traceId,
            String correlationId
    ) {
        JsonNode payload = objectMapper.valueToTree(crearPayload(pedido));

        ComandoEnvelope evento = new ComandoEnvelope(
                tipo,
                UUID.randomUUID().toString(),
                Instant.now(),
                traceId,
                correlationId,
                payload
        );

        rabbitTemplate.convertAndSend(
                exchange,
                routingKey,
                evento
        );
    }

    private Map<String, Object> crearPayload(Pedido pedido) {
        List<PedidoDetalleResponse> detalles = pedido.getDetalles()
                .stream()
                .map(detalle -> new PedidoDetalleResponse(
                        detalle.getProducto(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        detalle.getSubtotal()
                ))
                .toList();

        BigDecimal total = detalles.stream()
                .map(PedidoDetalleResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "pedidoId", pedido.getId(),
                "clienteId", pedido.getClienteId(),
                "estado", pedido.getEstado(),
                "fecha", pedido.getFecha(),
                "total", total,
                "detalles", detalles
        );
    }

    private PedidoResponse toResponse(Pedido pedido) {
        List<PedidoDetalleResponse> detalles = pedido.getDetalles()
                .stream()
                .map(detalle -> new PedidoDetalleResponse(
                        detalle.getProducto(),
                        detalle.getCantidad(),
                        detalle.getPrecioUnitario(),
                        detalle.getSubtotal()
                ))
                .collect(Collectors.toList());

        BigDecimal total = detalles.stream()
                .map(PedidoDetalleResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PedidoResponse(
                pedido.getId(),
                pedido.getClienteId(),
                null,
                PedidoEstado.validar(pedido.getEstado()),
                pedido.getFecha(),
                total,
                detalles
        );
    }
}
