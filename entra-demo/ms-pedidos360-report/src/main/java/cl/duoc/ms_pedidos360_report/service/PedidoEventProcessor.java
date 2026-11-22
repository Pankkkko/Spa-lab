package cl.duoc.ms_pedidos360_report.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.ms_pedidos360_report.dto.PedidoKafkaEvent;
import cl.duoc.ms_pedidos360_report.entity.EstadoPedido;
import cl.duoc.ms_pedidos360_report.entity.EventoProcesado;
import cl.duoc.ms_pedidos360_report.entity.PedidoResumen;
import cl.duoc.ms_pedidos360_report.entity.ProductoVenta;
import cl.duoc.ms_pedidos360_report.entity.VentaPorHora;
import cl.duoc.ms_pedidos360_report.repository.EventoProcesadoRepository;
import cl.duoc.ms_pedidos360_report.repository.PedidoResumenRepository;
import cl.duoc.ms_pedidos360_report.repository.ProductoVentaRepository;
import cl.duoc.ms_pedidos360_report.repository.VentaPorHoraRepository;
import tools.jackson.databind.JsonNode;

@Service
public class PedidoEventProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(PedidoEventProcessor.class);

    private final EventoProcesadoRepository eventoRepository;
    private final PedidoResumenRepository pedidoRepository;
    private final VentaPorHoraRepository ventaRepository;
    private final ProductoVentaRepository productoRepository;

    public PedidoEventProcessor(
            EventoProcesadoRepository eventoRepository,
            PedidoResumenRepository pedidoRepository,
            VentaPorHoraRepository ventaRepository,
            ProductoVentaRepository productoRepository) {

        this.eventoRepository = eventoRepository;
        this.pedidoRepository = pedidoRepository;
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public void procesar(PedidoKafkaEvent evento) {

        // 1. Validar que el evento no haya sido procesado anteriormente.
        if (eventoRepository.existsByEventId(evento.eventId())) {

            log.info(
                    "Evento duplicado ignorado: {}",
                    evento.eventId()
            );

            return;
        }

        JsonNode payload = evento.payload();

        // 2. Obtener información del pedido.
        Long pedidoId = obtenerLong(payload, "pedidoId");

        if (pedidoId == null) {
            throw new IllegalArgumentException(
                    "El evento no contiene un pedidoId válido"
            );
        }

        String estadoTexto = obtenerTexto(payload, "estado");

        if (estadoTexto == null) {
            throw new IllegalArgumentException(
                    "El evento no contiene un estado válido"
            );
        }

        EstadoPedido nuevoEstado;

        try {
            nuevoEstado = EstadoPedido.valueOf(estadoTexto);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Estado de pedido desconocido: " + estadoTexto,
                    ex
            );
        }

        Instant ahora = Instant.now();

        // 3. Buscar o crear el resumen del pedido.
        PedidoResumen pedido = pedidoRepository
                .findById(pedidoId)
                .orElseGet(PedidoResumen::new);

        boolean esPedidoNuevo = pedido.getPedidoId() == null;

        EstadoPedido estadoAnterior = pedido.getEstado();

        if (esPedidoNuevo) {
            pedido.setPedidoId(pedidoId);
            pedido.setFechaCreacion(
                    obtenerFecha(payload, "fecha", evento.timestamp())
            );
        }

        pedido.setClienteId(
                obtenerLong(payload, "clienteId")
        );

        pedido.setEstado(nuevoEstado);

        BigDecimal total = obtenerDecimal(payload, "total");

        if (total != null) {
            pedido.setTotal(total);
        }

        if (nuevoEstado == EstadoPedido.ENTREGADO
                && pedido.getFechaEntrega() == null) {

            pedido.setFechaEntrega(evento.timestamp());
        }

        pedido.setActualizadoEn(ahora);

        // 4. Guardar el resumen.
        pedidoRepository.save(pedido);

        // 5. Registrar la venta solamente al llegar a ACEPTADO.
        boolean primeraAceptacion =
                nuevoEstado == EstadoPedido.ACEPTADO
                && estadoAnterior != EstadoPedido.ACEPTADO;

        if (primeraAceptacion) {

            registrarVenta(evento, payload, pedido);

        }

        // 6. Registrar el evento como procesado.
        EventoProcesado eventoProcesado = new EventoProcesado(
                evento.eventId(),
                evento.eventType(),
                pedidoId,
                evento.timestamp(),
                ahora,
                payload.toString()
        );

        eventoRepository.save(eventoProcesado);

        log.info(
                "Evento procesado correctamente | Pedido: {} | Estado: {}",
                pedidoId,
                nuevoEstado
        );
    }

    private void registrarVenta(
            PedidoKafkaEvent evento,
            JsonNode payload,
            PedidoResumen pedido) {

        BigDecimal total = pedido.getTotal();

        if (total == null) {
            total = BigDecimal.ZERO;
        }

        // Agrupar la venta por hora UTC.
        LocalDateTime horaInicio = LocalDateTime
                .ofInstant(evento.timestamp(), ZoneOffset.UTC)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        VentaPorHora venta = ventaRepository
                .findByHoraInicio(horaInicio)
                .orElseGet(() -> {
                    VentaPorHora nueva = new VentaPorHora();
                    nueva.setHoraInicio(horaInicio);
                    return nueva;
                });

        venta.setCantidadPedidos(
                venta.getCantidadPedidos() + 1
        );

        venta.setMontoTotal(
                venta.getMontoTotal().add(total)
        );

        ventaRepository.save(venta);

        // Registrar estadísticas por producto.
        JsonNode detalles = payload.path("detalles");

        if (detalles.isArray()) {

            LocalDate fechaVenta = evento.timestamp()
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate();

            for (JsonNode detalle : detalles) {
                registrarProducto(detalle, fechaVenta);
            }
        }

        log.info(
                "Venta contabilizada | Pedido: {} | Total: {}",
                pedido.getPedidoId(),
                total
        );
    }

    private void registrarProducto(
            JsonNode detalle,
            LocalDate fechaVenta) {

        JsonNode producto = detalle.path("producto");

        if (producto.isMissingNode() || producto.isNull()) {
            log.warn("Detalle ignorado: producto inexistente");
            return;
        }

        String productoKey = obtenerClaveProducto(producto);

        if (productoKey == null || productoKey.isBlank()) {
            log.warn("Detalle ignorado: producto sin identificador");
            return;
        }

        String nombre = obtenerNombreProducto(producto, productoKey);

        Long cantidad = obtenerLong(detalle, "cantidad");

        if (cantidad == null || cantidad <= 0) {
            log.warn(
                    "Detalle ignorado: cantidad inválida para producto {}",
                    productoKey
            );
            return;
        }

        BigDecimal subtotal = obtenerDecimal(detalle, "subtotal");

        if (subtotal == null) {

            BigDecimal precioUnitario =
                    obtenerDecimal(detalle, "precioUnitario");

            if (precioUnitario == null) {
                log.warn(
                        "Detalle ignorado: precio inexistente para producto {}",
                        productoKey
                );
                return;
            }

            subtotal = precioUnitario.multiply(
                    BigDecimal.valueOf(cantidad)
            );
        }

        ProductoVenta productoVenta = productoRepository
                .findByProductoKeyAndFechaVenta(
                        productoKey,
                        fechaVenta
                )
                .orElseGet(() -> {
                    ProductoVenta nuevo = new ProductoVenta();
                    nuevo.setProductoKey(productoKey);
                    nuevo.setProductoNombre(nombre);
                    nuevo.setFechaVenta(fechaVenta);
                    return nuevo;
                });

        productoVenta.setUnidadesVendidas(
                productoVenta.getUnidadesVendidas() + cantidad
        );

        productoVenta.setMontoTotal(
                productoVenta.getMontoTotal().add(subtotal)
        );

        productoRepository.save(productoVenta);
    }

    private String obtenerClaveProducto(JsonNode producto) {

        if (producto.isObject()) {

            String[] posiblesIds = {
                    "id",
                    "productoId",
                    "sku"
            };

            for (String campo : posiblesIds) {

                String valor = obtenerTexto(producto, campo);

                if (valor != null && !valor.isBlank()) {
                    return valor;
                }
            }
        }

        if (producto.isValueNode()) {
            return producto.asText();
        }

        return producto.toString();
    }

    private String obtenerNombreProducto(
            JsonNode producto,
            String clave) {

        if (producto.isObject()) {

            String nombre = obtenerTexto(producto, "nombre");

            if (nombre == null) {
                nombre = obtenerTexto(producto, "name");
            }

            if (nombre != null) {
                return nombre;
            }
        }

        return clave;
    }

    private String obtenerTexto(JsonNode nodo, String campo) {

        JsonNode valor = nodo.path(campo);

        if (valor.isMissingNode() || valor.isNull()) {
            return null;
        }

        return valor.asText();
    }

    private Long obtenerLong(JsonNode nodo, String campo) {

        JsonNode valor = nodo.path(campo);

        if (valor.isMissingNode() || valor.isNull()
                || !valor.isNumber()) {
            return null;
        }

        return valor.longValue();
    }

    private BigDecimal obtenerDecimal(JsonNode nodo, String campo) {

        JsonNode valor = nodo.path(campo);

        if (valor.isMissingNode() || valor.isNull()
                || !valor.isNumber()) {
            return null;
        }

        return valor.decimalValue();
    }

    private Instant obtenerFecha(
            JsonNode payload,
            String campo,
            Instant fechaAlternativa) {

        String fechaTexto = obtenerTexto(payload, campo);

        if (fechaTexto == null || fechaTexto.isBlank()) {
            return fechaAlternativa;
        }

        try {
            return Instant.parse(fechaTexto);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(fechaTexto)
                        .toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException ex) {
                log.warn(
                        "Fecha inválida en payload: {}. Se utilizará timestamp del evento.",
                        fechaTexto
                );

                return fechaAlternativa;
            }
        }
    }
}