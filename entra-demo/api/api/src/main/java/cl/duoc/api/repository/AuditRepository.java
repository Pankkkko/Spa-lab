
package cl.duoc.api.repository;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

import cl.duoc.api.dto.AuditRegistroDetalleResponse;
import cl.duoc.api.dto.AuditRegistroResponse;
import cl.duoc.api.dto.PageResponse;

@Repository
public class AuditRepository {

    private final RestClient restClient;

    public AuditRepository(
            RestClient.Builder restClientBuilder,
            @Value("${microservices.audit.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    // =========================================================
    // BUSCAR REGISTROS CON FILTROS Y PAGINACIÓN
    // =========================================================
    public PageResponse<AuditRegistroResponse> buscarRegistros(
            String usuarioId,
            String tipoEvento,
            LocalDate desde,
            LocalDate hasta,
            int page,
            int size
    ) {

        return restClient
                .get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/api/audit");

                    if (usuarioId != null && !usuarioId.isBlank()) {
                        uriBuilder.queryParam("usuarioId", usuarioId);
                    }

                    if (tipoEvento != null && !tipoEvento.isBlank()) {
                        uriBuilder.queryParam("tipoEvento", tipoEvento);
                    }

                    if (desde != null) {
                        uriBuilder.queryParam("desde", desde);
                    }

                    if (hasta != null) {
                        uriBuilder.queryParam("hasta", hasta);
                    }

                    uriBuilder
                            .queryParam("page", page)
                            .queryParam("size", size);

                    return uriBuilder.build();
                })
                .retrieve()
                .body(
                    new ParameterizedTypeReference<
                        PageResponse<AuditRegistroResponse>
                    >() {}
                );
    }

    // =========================================================
    // BUSCAR HISTORIAL DE UN PEDIDO
    // =========================================================
    public PageResponse<AuditRegistroResponse> buscarPorPedido(
            String pedidoId,
            int page,
            int size
    ) {

        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/audit/pedido/{pedidoId}")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(Map.of("pedidoId", pedidoId))
                )
                .retrieve()
                .body(
                    new ParameterizedTypeReference<
                        PageResponse<AuditRegistroResponse>
                    >() {}
                );
    }

    // =========================================================
    // BUSCAR DETALLE DE UN REGISTRO
    // =========================================================
    public AuditRegistroDetalleResponse buscarPorId(Long id) {

        return restClient
                .get()
                .uri("/api/audit/{id}", id)
                .retrieve()
                .body(AuditRegistroDetalleResponse.class);
    }
}
