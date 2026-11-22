
package cl.duoc.api.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

import cl.duoc.api.dto.KpiResponse;
import cl.duoc.api.dto.PedidoEstadoResponse;
import cl.duoc.api.dto.ProductoTopResponse;
import cl.duoc.api.dto.TiempoEntregaResponse;
import cl.duoc.api.dto.VentaPorHoraResponse;

@Repository
public class ReportRepository {

    private final RestClient restClient;

    public ReportRepository(
            RestClient.Builder restClientBuilder,
            @Value("${microservices.report.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    // =========================================================
    // OBTENER KPIs GENERALES
    // =========================================================
    public KpiResponse obtenerKpis() {

        return restClient
                .get()
                .uri("/api/report/kpis")
                .retrieve()
                .body(KpiResponse.class);
    }

    // =========================================================
    // OBTENER VENTAS POR HORA
    // =========================================================
    public List<VentaPorHoraResponse> obtenerVentasPorHora(
            LocalDate desde,
            LocalDate hasta
    ) {

        return restClient
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/api/report/ventas/hora");

                    if (desde != null && hasta != null) {
                        uriBuilder
                                .queryParam("desde", desde)
                                .queryParam("hasta", hasta);
                    }

                    return uriBuilder.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<List<VentaPorHoraResponse>>() {});
    }

    // =========================================================
    // OBTENER PRODUCTOS MÁS VENDIDOS
    // =========================================================
    public List<ProductoTopResponse> obtenerProductosTop(
            LocalDate desde,
            LocalDate hasta
    ) {

        return restClient
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/api/report/productos/top");

                    if (desde != null && hasta != null) {
                        uriBuilder
                                .queryParam("desde", desde)
                                .queryParam("hasta", hasta);
                    }

                    return uriBuilder.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProductoTopResponse>>() {});
    }

    // =========================================================
    // OBTENER PEDIDOS POR ESTADO
    // =========================================================
    public List<PedidoEstadoResponse> obtenerPedidosPorEstado() {

        return restClient
                .get()
                .uri("/api/report/pedidos/estados")
                .retrieve()
                .body(new ParameterizedTypeReference<List<PedidoEstadoResponse>>() {});
    }

    // =========================================================
    // OBTENER TIEMPOS DE ENTREGA
    // =========================================================
    public TiempoEntregaResponse obtenerTiemposEntrega() {

        return restClient
                .get()
                .uri("/api/report/tiempos-entrega")
                .retrieve()
                .body(TiempoEntregaResponse.class);
    }
}
