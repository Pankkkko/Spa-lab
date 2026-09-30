package cl.duoc.ms_pedidos360_catalog.mapper;

import cl.duoc.ms_pedidos360_catalog.dto.ProductoResponseDTO;
import cl.duoc.ms_pedidos360_catalog.models.Producto;

public class ProductoMapper {

    public static ProductoResponseDTO toDTO(Producto producto) {
        return new ProductoResponseDTO(
                producto.getSku(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getCategoria(),
                producto.getPrecio(),
                producto.getImagenes()
        );
    }
}