package cl.duoc.ms_pedidos360_catalog.service;

import java.math.BigDecimal;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import cl.duoc.ms_pedidos360_catalog.dto.ProductoRequestDTO;
import cl.duoc.ms_pedidos360_catalog.dto.ProductoResponseDTO;
import cl.duoc.ms_pedidos360_catalog.exception.ProductoNotFoundException;
import cl.duoc.ms_pedidos360_catalog.exception.SkuDuplicadoException;
import cl.duoc.ms_pedidos360_catalog.factory.ProductoFactory;
import cl.duoc.ms_pedidos360_catalog.mapper.ProductoMapper;
import cl.duoc.ms_pedidos360_catalog.models.Producto;
import cl.duoc.ms_pedidos360_catalog.repository.ProductoRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.ms_pedidos360_catalog.dto.ReservaStockRequest;
import cl.duoc.ms_pedidos360_catalog.dto.ReservaStockDetalleRequest;

@Service
public class ProductoService {

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    @CacheEvict(value = {"productos", "producto"}, allEntries = true)
    public ProductoResponseDTO crearProducto(ProductoRequestDTO dto) {

        if (repository.existsBySkuIgnoreCase(dto.getSku())) {
            throw new SkuDuplicadoException(
                    "Ya existe un producto con ese SKU"
            );
        }

        Producto producto = ProductoFactory.crearProducto(dto);

        producto.setStock(dto.getStock());

        return ProductoMapper.toDTO(
                repository.save(producto)
        );
    }

    @Cacheable(value = "productos")
    public Page<ProductoResponseDTO> listarProductos(
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        return repository.findAll(pageable)
                .map(ProductoMapper::toDTO);
    }

    @Cacheable(value = "producto", key = "#sku")
    public ProductoResponseDTO obtenerPorSku(String sku) {

        Producto producto = repository.findBySkuIgnoreCase(sku)
                .orElseThrow(()
                        -> new ProductoNotFoundException(
                                "Producto no encontrado"
                        )
                );

        return ProductoMapper.toDTO(producto);
    }

    public Page<ProductoResponseDTO> buscarPorNombre(
            String nombre,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        return repository
                .findByNombreContainingIgnoreCase(nombre, pageable)
                .map(ProductoMapper::toDTO);
    }

    public Page<ProductoResponseDTO> buscarPorCategoria(
            String categoria,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        return repository
                .findByCategoriaIgnoreCase(categoria, pageable)
                .map(ProductoMapper::toDTO);
    }

    public Page<ProductoResponseDTO> buscarPorPrecio(
            BigDecimal min,
            BigDecimal max,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        return repository
                .findByPrecioBetween(min, max, pageable)
                .map(ProductoMapper::toDTO);
    }

    @CacheEvict(value = {"productos", "producto"}, allEntries = true)
    public ProductoResponseDTO actualizar(
            String sku,
            ProductoRequestDTO dto
    ) {

        Producto producto = repository.findBySkuIgnoreCase(sku)
                .orElseThrow(()
                        -> new ProductoNotFoundException(
                                "Producto no encontrado"
                        )
                );

        if (!producto.getSku().equalsIgnoreCase(dto.getSku())
                && repository.existsBySkuIgnoreCase(dto.getSku())) {

            throw new SkuDuplicadoException(
                    "Ya existe un producto con ese SKU"
            );
        }

        producto.setSku(dto.getSku());
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setCategoria(dto.getCategoria());
        producto.setImagenes(dto.getImagenes());
        producto.setStock(dto.getStock());

        return ProductoMapper.toDTO(
                repository.save(producto)
        );
    }

    @CacheEvict(value = {"productos", "producto"}, allEntries = true)
    public void eliminar(String sku) {

        Producto producto = repository.findBySkuIgnoreCase(sku)
                .orElseThrow(()
                        -> new ProductoNotFoundException(
                                "Producto no encontrado"
                        )
                );

        repository.delete(producto);
    }

    @Cacheable(value = "producto-stock", key = "#sku")
    public Integer obtenerStock(String sku) {

        Producto producto = repository.findBySkuIgnoreCase(sku)
                .orElseThrow(()
                        -> new ProductoNotFoundException(
                                "Producto no encontrado"
                        )
                );

        return producto.getStock();
    }

    @CacheEvict(
            value = {
                "productos",
                "producto",
                "producto-stock"
            },
            allEntries = true
    )
    public Integer actualizarStock(
            String sku,
            Integer stock
    ) {

        if (stock == null || stock < 0) {
            throw new IllegalArgumentException(
                    "El stock no puede ser negativo"
            );
        }

        Producto producto = repository.findBySkuIgnoreCase(sku)
                .orElseThrow(()
                        -> new ProductoNotFoundException(
                                "Producto no encontrado"
                        )
                );

        producto.setStock(stock);

        repository.save(producto);

        return producto.getStock();
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "productos", allEntries = true),
        @CacheEvict(value = "producto", allEntries = true),
        @CacheEvict(value = "producto-stock", allEntries = true)
    })
    public void reservarStock(ReservaStockRequest request) {
        validarReserva(request);

        // Orden estable para reducir el riesgo de interbloqueos.
        List<ReservaStockDetalleRequest> detalles = request.getDetalles()
                .stream()
                .sorted((a, b) -> a.getSku().compareToIgnoreCase(b.getSku()))
                .toList();

        for (ReservaStockDetalleRequest detalle : detalles) {
            int filas = repository.reservarStock(
                    detalle.getSku(),
                    detalle.getCantidad()
            );

            if (filas != 1) {
                throw new IllegalStateException(
                        "Producto inexistente o stock insuficiente para SKU: "
                        + detalle.getSku()
                );
            }
        }
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "productos", allEntries = true),
        @CacheEvict(value = "producto", allEntries = true),
        @CacheEvict(value = "producto-stock", allEntries = true)
    })
    public void liberarStock(ReservaStockRequest request) {
        validarReserva(request);

        List<ReservaStockDetalleRequest> detalles = request.getDetalles()
                .stream()
                .sorted((a, b) -> a.getSku().compareToIgnoreCase(b.getSku()))
                .toList();

        for (ReservaStockDetalleRequest detalle : detalles) {
            int filas = repository.liberarStock(
                    detalle.getSku(),
                    detalle.getCantidad()
            );

            if (filas != 1) {
                throw new IllegalStateException(
                        "No se pudo liberar el stock para SKU: "
                        + detalle.getSku()
                );
            }
        }
    }

    private void validarReserva(ReservaStockRequest request) {
        if (request == null
                || request.getDetalles() == null
                || request.getDetalles().isEmpty()) {
            throw new IllegalArgumentException(
                    "La reserva debe contener al menos un producto"
            );
        }

        Set<String> skus = new HashSet<>();

        for (ReservaStockDetalleRequest detalle : request.getDetalles()) {
            if (detalle == null
                    || detalle.getSku() == null
                    || detalle.getSku().isBlank()
                    || detalle.getCantidad() == null
                    || detalle.getCantidad() <= 0) {
                throw new IllegalArgumentException(
                        "Todos los productos deben tener SKU y cantidad válida"
                );
            }

            String skuNormalizado = detalle.getSku().trim().toLowerCase(
                    java.util.Locale.ROOT
            );

            if (!skus.add(skuNormalizado)) {
                throw new IllegalArgumentException(
                        "El carrito contiene SKU duplicados: " + detalle.getSku()
                );
            }
        }
    }

}
