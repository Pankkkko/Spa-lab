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
                .orElseThrow(() ->
                    new ProductoNotFoundException(
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
                .orElseThrow(() ->
                    new ProductoNotFoundException(
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
                .orElseThrow(() ->
                    new ProductoNotFoundException(
                        "Producto no encontrado"
                    )
                );

        repository.delete(producto);
    }

    @Cacheable(value = "producto-stock", key = "#sku")
    public Integer obtenerStock(String sku) {

        Producto producto = repository.findBySkuIgnoreCase(sku)
                .orElseThrow(() ->
                    new ProductoNotFoundException(
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
                .orElseThrow(() ->
                    new ProductoNotFoundException(
                        "Producto no encontrado"
                    )
                );

        producto.setStock(stock);

        repository.save(producto);

        return producto.getStock();
    }
}