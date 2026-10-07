package cl.duoc.api.controller;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.api.dto.ProductoPageResponse;
import cl.duoc.api.dto.ProductoRequest;
import cl.duoc.api.dto.ProductoResponse;
import cl.duoc.api.repository.ProductoRepository;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository productoRepository;

    public ProductoController(
        ProductoRepository productoRepository
    ) {
        this.productoRepository = productoRepository;
    }

    // =========================================================
    // GET TODOS
    // =========================================================

    @GetMapping
    public ResponseEntity<ProductoPageResponse> listar(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {

        return ResponseEntity.ok(
            productoRepository.listarTodos(page, size)
        );
    }

    // =========================================================
    // GET POR SKU
    // =========================================================

    @GetMapping("/{sku}")
    public ResponseEntity<ProductoResponse> buscarPorSku(
        @PathVariable String sku
    ) {

        return ResponseEntity.ok(
            productoRepository.buscarPorSku(sku)
        );
    }

    // =========================================================
    // GET STOCK
    // =========================================================

    @GetMapping("/{sku}/stock")
    public ResponseEntity<Integer> obtenerStock(
        @PathVariable String sku
    ) {

        return ResponseEntity.ok(
            productoRepository.obtenerStock(sku)
        );
    }

    // =========================================================
    // BUSCAR POR NOMBRE
    // =========================================================

    @GetMapping("/buscar/nombre")
    public ResponseEntity<ProductoPageResponse> buscarPorNombre(
        @RequestParam String nombre,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {

        return ResponseEntity.ok(
            productoRepository.buscarPorNombre(
                nombre,
                page,
                size
            )
        );
    }

    // =========================================================
    // BUSCAR POR CATEGORIA
    // =========================================================

    @GetMapping("/buscar/categoria")
    public ResponseEntity<ProductoPageResponse> buscarPorCategoria(
        @RequestParam String categoria,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {

        return ResponseEntity.ok(
            productoRepository.buscarPorCategoria(
                categoria,
                page,
                size
            )
        );
    }

    // =========================================================
    // BUSCAR POR PRECIO
    // =========================================================

    @GetMapping("/buscar/precio")
    public ResponseEntity<ProductoPageResponse> buscarPorPrecio(
        @RequestParam BigDecimal min,
        @RequestParam BigDecimal max,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {

        return ResponseEntity.ok(
            productoRepository.buscarPorPrecio(
                min,
                max,
                page,
                size
            )
        );
    }

    // =========================================================
    // CREAR
    // =========================================================

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(
        @RequestBody ProductoRequest request
    ) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                productoRepository.crear(request)
            );
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    @PutMapping("/{sku}")
    public ResponseEntity<ProductoResponse> actualizar(
        @PathVariable String sku,
        @RequestBody ProductoRequest request
    ) {

        return ResponseEntity.ok(
            productoRepository.actualizar(
                sku,
                request
            )
        );
    }

    // =========================================================
    // ACTUALIZAR STOCK
    // =========================================================

    @PutMapping("/{sku}/stock")
    public ResponseEntity<Integer> actualizarStock(
        @PathVariable String sku,
        @RequestBody Integer stock
    ) {

        return ResponseEntity.ok(
            productoRepository.actualizarStock(
                sku,
                stock
            )
        );
    }

    // =========================================================
    // ELIMINAR
    // =========================================================

    @DeleteMapping("/{sku}")
    public ResponseEntity<Void> eliminar(
        @PathVariable String sku
    ) {

        productoRepository.eliminar(sku);

        return ResponseEntity
            .noContent()
            .build();
    }

    // =========================================================
    // EXISTE SKU
    // =========================================================

    @GetMapping("/exists/{sku}")
    public ResponseEntity<Void> existeSku(
        @PathVariable String sku
    ) {

        productoRepository.existeSku(sku);

        return ResponseEntity.ok().build();
    }
}