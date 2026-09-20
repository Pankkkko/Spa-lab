package cl.duoc.api.repository;

import cl.duoc.api.dto.PedidoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

import java.util.List;

@Repository
public class PedidoRepository {

    private final RestClient restClient;

    public PedidoRepository(
        RestClient.Builder restClientBuilder,
        @Value("${microservices.pedidos.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public List<PedidoResponse> buscarPorCliente(Long clienteId) {
        return restClient
            .get()
            .uri("/api/pedidos/cliente/{id}", clienteId)
            .retrieve()
            .body(new ParameterizedTypeReference<List<PedidoResponse>>() {});
    }
}