package cl.duoc.api.repository;

import cl.duoc.api.dto.ClienteResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

import java.util.List;

@Repository
public class ClienteRepository {

    private final RestClient restClient;

    public ClienteRepository(
        RestClient.Builder restClientBuilder,
        @Value("${microservices.clientes.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    // GET por ID
    public ClienteResponse buscarPorId(Long id) {
        return restClient
            .get()
            .uri("/api/clientes/{id}", id)
            .retrieve()
            .body(ClienteResponse.class);
    }

    // GET todos
    public List<ClienteResponse> listarTodos() {
        return restClient
            .get()
            .uri("/api/clientes")
            .retrieve()
            .body(new ParameterizedTypeReference<List<ClienteResponse>>() {});
    }

    // POST
    public ClienteResponse crear(ClienteResponse request) {
        return restClient
            .post()
            .uri("/api/clientes")
            .body(request)
            .retrieve()
            .body(ClienteResponse.class);
    }

    // PUT
    public ClienteResponse actualizar(Long id, ClienteResponse request) {
        return restClient
            .put()
            .uri("/api/clientes/{id}", id)
            .body(request)
            .retrieve()
            .body(ClienteResponse.class);
    }

    // DELETE
    public void eliminar(Long id) {
        restClient
            .delete()
            .uri("/api/clientes/{id}", id)
            .retrieve()
            .toBodilessEntity();
    }
}