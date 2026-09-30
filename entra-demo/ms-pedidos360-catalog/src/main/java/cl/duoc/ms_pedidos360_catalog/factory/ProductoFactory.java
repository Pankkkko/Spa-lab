package cl.duoc.ms_pedidos360_catalog.factory;

import cl.duoc.ms_pedidos360_catalog.dto.ProductoRequestDTO;
import cl.duoc.ms_pedidos360_catalog.models.Producto;

public class ProductoFactory {

    public static Producto crearProducto(ProductoRequestDTO dto) {
        Producto producto = new Producto();
        producto.setSku(dto.getSku());
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setCategoria(dto.getCategoria());
        producto.setImagenes(dto.getImagenes());
        return producto;
    }
}