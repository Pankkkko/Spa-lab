
package cl.duoc.api.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.api.dto.KpiResponse;
import cl.duoc.api.dto.PedidoEstadoResponse;
import cl.duoc.api.dto.ProductoTopResponse;
import cl.duoc.api.dto.TiempoEntregaResponse;
import cl.duoc.api.dto.VentaPorHoraResponse;
import cl.duoc.api.repository.ReportRepository;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportRepository reportRepository;

    public ReportController(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    // =========================================================
    // OBTENER KPIs GENERALES
    // GET /api/report/kpis
    // =========================================================
    @GetMapping("/kpis")
    public ResponseEntity<KpiResponse> obtenerKpis() {

        return ResponseEntity.ok(
                reportRepository.obtenerKpis()
        );
    }

    // =========================================================
    // OBTENER VENTAS POR HORA
    // GET /api/report/ventas/hora
    // =========================================================
    @GetMapping("/ventas/hora")
    public ResponseEntity<List<VentaPorHoraResponse>> obtenerVentasPorHora(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {

        validarRangoFechas(desde, hasta);

        return ResponseEntity.ok(
                reportRepository.obtenerVentasPorHora(desde, hasta)
        );
    }

    // =========================================================
    // OBTENER PRODUCTOS MÁS VENDIDOS
    // GET /api/report/productos/top
    // =========================================================
    @GetMapping("/productos/top")
    public ResponseEntity<List<ProductoTopResponse>> obtenerProductosTop(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {

        validarRangoFechas(desde, hasta);

        return ResponseEntity.ok(
                reportRepository.obtenerProductosTop(desde, hasta)
        );
    }

    // =========================================================
    // OBTENER PEDIDOS POR ESTADO
    // GET /api/report/pedidos/estados
    // =========================================================
    @GetMapping("/pedidos/estados")
    public ResponseEntity<List<PedidoEstadoResponse>> obtenerPedidosPorEstado() {

        return ResponseEntity.ok(
                reportRepository.obtenerPedidosPorEstado()
        );
    }

    // =========================================================
    // OBTENER TIEMPOS DE ENTREGA
    // GET /api/report/tiempos-entrega
    // =========================================================
    @GetMapping("/tiempos-entrega")
    public ResponseEntity<TiempoEntregaResponse> obtenerTiemposEntrega() {

        return ResponseEntity.ok(
                reportRepository.obtenerTiemposEntrega()
        );
    }

    // =========================================================
    // VALIDAR RANGO DE FECHAS
    // =========================================================
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
