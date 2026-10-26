package cl.duoc.api.repository;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

import cl.duoc.api.dto.ProductoPageResponse;
import cl.duoc.api.dto.ProductoRequest;
import cl.duoc.api.dto.ProductoResponse;
import cl.duoc.api.dto.ReservaStockRequest;

@Repository
public class ProductoRepository {

    private final RestClient restClient;
    private final String internalApiKey;

    public ProductoRepository(
            RestClient.Builder restClientBuilder,
            @Value("${microservices.catalogo.base-url}") String baseUrl,
            @Value("${internal.api-key}") String internalApiKey
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();

        this.internalApiKey = internalApiKey;
    }

    // =========================================================
    // GET TODOS LOS PRODUCTOS
    // =========================================================
    public ProductoPageResponse listarTodos(
            int page,
            int size
    ) {

        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/productos")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build()
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference<ProductoPageResponse>() {
                }
                );
    }

    // =========================================================
    // GET PRODUCTO POR SKU
    // =========================================================
    public ProductoResponse buscarPorSku(String sku) {

        return restClient
                .get()
                .uri("/api/productos/{sku}", sku)
                .retrieve()
                .body(ProductoResponse.class);
    }

    // =========================================================
    // BUSCAR POR NOMBRE
    // =========================================================
    public ProductoPageResponse buscarPorNombre(
            String nombre,
            int page,
            int size
    ) {

        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/productos/buscar/nombre")
                        .queryParam("nombre", nombre)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build()
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference<ProductoPageResponse>() {
                }
                );
    }

    // =========================================================
    // BUSCAR POR CATEGORIA
    // =========================================================
    public ProductoPageResponse buscarPorCategoria(
            String categoria,
            int page,
            int size
    ) {

        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/productos/buscar/categoria")
                        .queryParam("categoria", categoria)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build()
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference<ProductoPageResponse>() {
                }
                );
    }

    // =========================================================
    // BUSCAR POR PRECIO
    // =========================================================
    public ProductoPageResponse buscarPorPrecio(
            BigDecimal min,
            BigDecimal max,
            int page,
            int size
    ) {

        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/productos/buscar/precio")
                        .queryParam("min", min)
                        .queryParam("max", max)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build()
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference<ProductoPageResponse>() {
                }
                );
    }

    // =========================================================
    // CREAR PRODUCTO
    // =========================================================
    public ProductoResponse crear(
            ProductoRequest request
    ) {

        return restClient
                .post()
                .uri("/api/productos")
                .body(request)
                .retrieve()
                .body(ProductoResponse.class);
    }

    // =========================================================
    // ACTUALIZAR PRODUCTO
    // =========================================================
    public ProductoResponse actualizar(
            String sku,
            ProductoRequest request
    ) {

        return restClient
                .put()
                .uri("/api/productos/{sku}", sku)
                .body(request)
                .retrieve()
                .body(ProductoResponse.class);
    }

    // =========================================================
    // ELIMINAR PRODUCTO
    // =========================================================
    public void eliminar(String sku) {

        restClient
                .delete()
                .uri("/api/productos/{sku}", sku)
                .retrieve()
                .toBodilessEntity();
    }

    // =========================================================
    // OBTENER STOCK
    // =========================================================
    public Integer obtenerStock(String sku) {

        return restClient
                .get()
                .uri("/api/productos/{sku}/stock", sku)
                .retrieve()
                .body(Integer.class);
    }

    // =========================================================
    // ACTUALIZAR STOCK
    // =========================================================
    public Integer actualizarStock(
            String sku,
            Integer stock
    ) {

        return restClient
                .put()
                .uri("/api/productos/{sku}/stock", sku)
                .body(stock)
                .retrieve()
                .body(Integer.class);
    }

    // =========================================================
    // VERIFICAR SI EXISTE SKU
    // =========================================================
    public void existeSku(String sku) {

        restClient
                .get()
                .uri("/api/productos/exists/{sku}", sku)
                .retrieve()
                .toBodilessEntity();
    }

    // =========================================================
// RESERVAR STOCK PARA CHECKOUT
// =========================================================
    public void reservarStock(ReservaStockRequest request) {
        restClient
                .post()
                .uri("/internal/inventario/reservar")
                .header("X-Internal-Api-Key", internalApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

// =========================================================
// LIBERAR STOCK SI FALLA EL CHECKOUT
// =========================================================
    public void liberarStock(ReservaStockRequest request) {
        restClient
                .post()
                .uri("/internal/inventario/liberar")
                .header("X-Internal-Api-Key", internalApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
