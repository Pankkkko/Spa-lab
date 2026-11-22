package cl.duoc.ms_pedidos360_report.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.ms_pedidos360_report.dto.KpiResponse;
import cl.duoc.ms_pedidos360_report.dto.PedidoEstadoResponse;
import cl.duoc.ms_pedidos360_report.dto.ProductoTopResponse;
import cl.duoc.ms_pedidos360_report.dto.TiempoEntregaResponse;
import cl.duoc.ms_pedidos360_report.dto.VentaPorHoraResponse;
import cl.duoc.ms_pedidos360_report.service.ReportService;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarError(
            IllegalArgumentException exception
    ) {
        return ResponseEntity.badRequest().body(
                Map.of("error", exception.getMessage())
        );
    }

    // GET /api/report/kpis
    @GetMapping("/kpis")
    public ResponseEntity<KpiResponse> obtenerKpis() {

        return ResponseEntity.ok(
                reportService.obtenerKpis()
        );
    }

    // GET /api/report/ventas/hora
    // GET /api/report/ventas/hora?desde=2026-11-01&hasta=2026-11-21
    @GetMapping("/ventas/hora")
    public ResponseEntity<List<VentaPorHoraResponse>> obtenerVentasPorHora(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {

        validarRangoFechas(desde, hasta);

        LocalDateTime fechaDesde = desde == null
                ? null
                : desde.atStartOfDay();

        LocalDateTime fechaHasta = hasta == null
                ? null
                : hasta.atTime(LocalTime.MAX);

        return ResponseEntity.ok(
                reportService.obtenerVentasPorHora(
                        fechaDesde,
                        fechaHasta
                )
        );
    }

    // GET /api/report/productos/top
    // GET /api/report/productos/top?desde=2026-11-01&hasta=2026-11-21
    @GetMapping("/productos/top")
    public ResponseEntity<List<ProductoTopResponse>> obtenerProductosTop(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {

        validarRangoFechas(desde, hasta);

        return ResponseEntity.ok(
                reportService.obtenerProductosTop(desde, hasta)
        );
    }

    // GET /api/report/pedidos/estados
    @GetMapping("/pedidos/estados")
    public ResponseEntity<List<PedidoEstadoResponse>> obtenerPedidosPorEstado() {

        return ResponseEntity.ok(
                reportService.obtenerPedidosPorEstado()
        );
    }

    // GET /api/report/tiempos-entrega
    @GetMapping("/tiempos-entrega")
    public ResponseEntity<TiempoEntregaResponse> obtenerTiemposEntrega() {

        return ResponseEntity.ok(
                reportService.obtenerTiemposEntrega()
        );
    }

    private void validarRangoFechas(LocalDate desde, LocalDate hasta) {

        if ((desde == null) != (hasta == null)) {
            throw new IllegalArgumentException(
                    "Debes proporcionar ambos parámetros: desde y hasta."
            );
        }

        if (desde != null && hasta.isBefore(desde)) {
            throw new IllegalArgumentException(
                    "La fecha hasta no puede ser anterior a desde."
            );
        }
    }
}
