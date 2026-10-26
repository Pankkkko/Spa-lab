package cl.duoc.api.repository;

import cl.duoc.api.dto.PedidoCompraInternaRequest;
import cl.duoc.api.dto.PedidoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

@Repository
public class PedidoRepository {

    private final RestClient restClient;
    private final String internalApiKey;

    public PedidoRepository(
            RestClient.Builder restClientBuilder,
            @Value("${microservices.pedidos.base-url}") String baseUrl,
            @Value("${internal.api-key}") String internalApiKey
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();

        this.internalApiKey = internalApiKey;
    }

    // GET por cliente
    public List<PedidoResponse> buscarPorCliente(Long clienteId) {
        return restClient
                .get()
                .uri("/api/pedidos/cliente/{id}", clienteId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PedidoResponse>>() {
                });
    }

    // GET por ID
    public PedidoResponse buscarPorId(Long id) {
        return restClient
                .get()
                .uri("/api/pedidos/{id}", id)
                .retrieve()
                .body(PedidoResponse.class);
    }

    // GET todos
    public List<PedidoResponse> listarTodos() {
        return restClient
                .get()
                .uri("/api/pedidos")
                .retrieve()
                .body(new ParameterizedTypeReference<List<PedidoResponse>>() {
                });
    }

    public List<PedidoResponse> buscarPorEstado(String estado) {
        return restClient
                .get()
                .uri("/api/pedidos/estado/{estado}", estado)
                .retrieve()
                .body(new ParameterizedTypeReference<List<PedidoResponse>>() {
                });
    }

    // POST
    public PedidoResponse crear(PedidoResponse request) {
        return restClient
                .post()
                .uri("/api/pedidos")
                .body(request)
                .retrieve()
                .body(PedidoResponse.class);
    }

    // PUT
    public PedidoResponse actualizar(Long id, PedidoResponse request) {
        return restClient
                .put()
                .uri("/api/pedidos/{id}", id)
                .body(request)
                .retrieve()
                .body(PedidoResponse.class);
    }

    public PedidoResponse cambiarEstado(Long id, String estado) {
        return restClient
                .put()
                .uri("/api/pedidos/{id}/status", id)
                .body(Map.of("status", estado))
                .retrieve()
                .body(PedidoResponse.class);
    }

    // DELETE
    public void eliminar(Long id) {
        restClient
                .delete()
                .uri("/api/pedidos/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }

    // =========================================================
// CREAR PEDIDO DESDE CHECKOUT
// =========================================================
    public PedidoResponse crearCompraInterna(
    PedidoCompraInternaRequest request
) {
    return restClient
        .post()
        .uri("/api/pedidos/checkout-interno")
        .header("X-Internal-Api-Key", internalApiKey)
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .body(PedidoResponse.class);
}
}
