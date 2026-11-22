package cl.duoc.ms_pedidos360_report.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import cl.duoc.ms_pedidos360_report.dto.KpiResponse;
import cl.duoc.ms_pedidos360_report.dto.PedidoEstadoResponse;
import cl.duoc.ms_pedidos360_report.dto.ProductoTopResponse;
import cl.duoc.ms_pedidos360_report.dto.TiempoEntregaResponse;
import cl.duoc.ms_pedidos360_report.dto.VentaPorHoraResponse;
import cl.duoc.ms_pedidos360_report.entity.EstadoPedido;
import cl.duoc.ms_pedidos360_report.entity.PedidoResumen;
import cl.duoc.ms_pedidos360_report.entity.ProductoVenta;
import cl.duoc.ms_pedidos360_report.entity.VentaPorHora;
import cl.duoc.ms_pedidos360_report.repository.PedidoResumenRepository;
import cl.duoc.ms_pedidos360_report.repository.ProductoVentaRepository;
import cl.duoc.ms_pedidos360_report.repository.VentaPorHoraRepository;

@Service
public class ReportService {

    private final PedidoResumenRepository pedidoRepository;
    private final VentaPorHoraRepository ventaRepository;
    private final ProductoVentaRepository productoRepository;

    public ReportService(
            PedidoResumenRepository pedidoRepository,
            VentaPorHoraRepository ventaRepository,
            ProductoVentaRepository productoRepository
    ) {
        this.pedidoRepository = pedidoRepository;
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
    }

    // =====================================================
    // KPIs GENERALES
    // =====================================================

    public KpiResponse obtenerKpis() {

        long totalPedidos = pedidoRepository.count();

        long pedidosActivos =
                pedidoRepository.countByEstado(EstadoPedido.CREADO)
                + pedidoRepository.countByEstado(EstadoPedido.ACEPTADO)
                + pedidoRepository.countByEstado(EstadoPedido.EN_PREPARACION)
                + pedidoRepository.countByEstado(EstadoPedido.DESPACHADO);

        long pedidosEntregados =
                pedidoRepository.countByEstado(EstadoPedido.ENTREGADO);

        long pedidosCancelados =
                pedidoRepository.countByEstado(EstadoPedido.CANCELADO);

        BigDecimal ventasTotales = ventaRepository.findAll()
                .stream()
                .map(VentaPorHora::getMontoTotal)
                .filter(monto -> monto != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        TiempoEntregaResponse tiempos = obtenerTiemposEntrega();

        return new KpiResponse(
                totalPedidos,
                pedidosActivos,
                pedidosEntregados,
                pedidosCancelados,
                ventasTotales,
                tiempos.promedioMinutos()
        );
    }

    // =====================================================
    // VENTAS POR HORA
    // =====================================================

    public List<VentaPorHoraResponse> obtenerVentasPorHora(
            java.time.LocalDateTime desde,
            java.time.LocalDateTime hasta
    ) {

        List<VentaPorHora> ventas;

        if (desde == null && hasta == null) {
            ventas = ventaRepository.findAllByOrderByHoraInicioAsc();
        } else {
            ventas = ventaRepository
                    .findAllByHoraInicioBetweenOrderByHoraInicioAsc(
                            desde,
                            hasta
                    );
        }

        return ventas.stream()
                .map(venta -> new VentaPorHoraResponse(
                        venta.getHoraInicio(),
                        venta.getCantidadPedidos(),
                        venta.getMontoTotal()
                ))
                .toList();
    }

    // =====================================================
    // PRODUCTOS MÁS VENDIDOS
    // =====================================================

    public List<ProductoTopResponse> obtenerProductosTop(
            java.time.LocalDate desde,
            java.time.LocalDate hasta
    ) {

        List<ProductoVenta> productos;

        if (desde == null && hasta == null) {
            productos = productoRepository
                    .findTop10ByOrderByUnidadesVendidasDescMontoTotalDesc();
        } else {
            productos = productoRepository
                    .findTop10ByFechaVentaBetweenOrderByUnidadesVendidasDescMontoTotalDesc(
                            desde,
                            hasta
                    );
        }

        return productos.stream()
                .map(producto -> new ProductoTopResponse(
                        producto.getProductoKey(),
                        producto.getProductoNombre(),
                        producto.getUnidadesVendidas(),
                        producto.getMontoTotal(),
                        producto.getFechaVenta()
                ))
                .toList();
    }

    // =====================================================
    // PEDIDOS POR ESTADO
    // =====================================================

    public List<PedidoEstadoResponse> obtenerPedidosPorEstado() {

        List<PedidoEstadoResponse> resultado = new ArrayList<>();

        for (EstadoPedido estado : EstadoPedido.values()) {

            long cantidad = pedidoRepository.countByEstado(estado);

            resultado.add(
                    new PedidoEstadoResponse(estado, cantidad)
            );
        }

        return resultado;
    }

    // =====================================================
    // TIEMPOS DE ENTREGA
    // =====================================================

    public TiempoEntregaResponse obtenerTiemposEntrega() {

        List<PedidoResumen> entregados =
                pedidoRepository.findByFechaEntregaIsNotNull();

        List<Double> tiempos = entregados.stream()
                .filter(pedido -> pedido.getFechaCreacion() != null)
                .filter(pedido -> pedido.getFechaEntrega() != null)
                .map(pedido -> (double) Duration.between(
                        pedido.getFechaCreacion(),
                        pedido.getFechaEntrega()
                ).toMinutes())
                .filter(tiempo -> tiempo >= 0)
                .toList();

        if (tiempos.isEmpty()) {
            return new TiempoEntregaResponse(
                    0,
                    0,
                    0,
                    0
            );
        }

        double promedio = tiempos.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        double minimo = tiempos.stream()
                .mapToDouble(Double::doubleValue)
                .min()
                .orElse(0);

        double maximo = tiempos.stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0);

        return new TiempoEntregaResponse(
                tiempos.size(),
                promedio,
                minimo,
                maximo
        );
    }
}