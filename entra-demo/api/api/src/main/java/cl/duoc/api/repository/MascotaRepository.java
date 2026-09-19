package cl.duoc.api.repository;

import cl.duoc.api.dto.MascotaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

import java.util.List;

@Repository
public class MascotaRepository {

    private final RestClient restClient;

    public MascotaRepository(
        RestClient.Builder restClientBuilder,
        @Value("${microservices.mascotas.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder
            .baseUrl(baseUrl)
            .build();
    }

    public List<MascotaResponse> buscarPorCliente(Long clienteId) {
        return restClient
            .get()
            .uri("/api/mascotas/cliente/{clienteId}", clienteId)
            .retrieve()
            .body(new ParameterizedTypeReference<List<MascotaResponse>>() {});
    }
}