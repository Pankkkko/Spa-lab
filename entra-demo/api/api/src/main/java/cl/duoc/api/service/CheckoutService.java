package cl.duoc.api.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import cl.duoc.api.dto.CheckoutItemRequest;
import cl.duoc.api.dto.CheckoutRequest;
import cl.duoc.api.dto.ClienteResponse;
import cl.duoc.api.dto.PedidoCompraInternaRequest;
import cl.duoc.api.dto.PedidoDetalleCompraInternaRequest;
import cl.duoc.api.dto.PedidoResponse;
import cl.duoc.api.dto.ProductoResponse;
import cl.duoc.api.dto.ReservaStockDetalleRequest;
import cl.duoc.api.dto.ReservaStockRequest;
import cl.duoc.api.repository.ClienteRepository;
import cl.duoc.api.repository.PedidoRepository;
import cl.duoc.api.repository.ProductoRepository;

@Service
public class CheckoutService {

    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;

    public CheckoutService(
        ClienteRepository clienteRepository,
        ProductoRepository productoRepository,
        PedidoRepository pedidoRepository
    ) {
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public PedidoResponse realizarCheckout(
        String entraObjectId,
        CheckoutRequest request
    ) {
        validarSolicitud(entraObjectId, request);

        // 1. Obtener el perfil vinculado a la identidad autenticada.
        ClienteResponse cliente =
            clienteRepository.buscarPorIdentidad(entraObjectId);

        if (cliente == null || cliente.id() == null) {
            throw new IllegalStateException(
                "No se encontró un perfil válido para el cliente autenticado"
            );
        }

        List<PedidoDetalleCompraInternaRequest> detallesPedido =
            new ArrayList<>();

        List<ReservaStockDetalleRequest> detallesReserva =
            new ArrayList<>();

        Set<String> skus = new HashSet<>();

        // 2. Consultar cada producto y preparar el pedido.
        for (CheckoutItemRequest item : request.items()) {

            String sku = item.sku().trim();
            String skuNormalizado = sku.toLowerCase(Locale.ROOT);

            if (!skus.add(skuNormalizado)) {
                throw new IllegalArgumentException(
                    "El checkout contiene SKU duplicados: " + sku
                );
            }

            ProductoResponse producto =
                productoRepository.buscarPorSku(sku);

            if (producto == null
                || producto.precio() == null
                || producto.precio().compareTo(BigDecimal.ZERO) <= 0
                || producto.nombre() == null
                || producto.nombre().isBlank()) {

                throw new IllegalStateException(
                    "El producto no tiene datos válidos: " + sku
                );
            }

            detallesPedido.add(
                new PedidoDetalleCompraInternaRequest(
                    producto.nombre(),
                    item.cantidad(),
                    producto.precio()
                )
            );

            detallesReserva.add(
                new ReservaStockDetalleRequest(
                    producto.sku(),
                    item.cantidad()
                )
            );
        }

        ReservaStockRequest reserva =
            new ReservaStockRequest(detallesReserva);

        // 3. Reservar el stock de forma atómica en Catálogo.
        productoRepository.reservarStock(reserva);

        // 4. Crear el pedido. Si falla, intentar liberar el stock.
        try {
            PedidoCompraInternaRequest pedido =
                new PedidoCompraInternaRequest(
                    cliente.id(),
                    detallesPedido
                );

            PedidoResponse creado =
                pedidoRepository.crearCompraInterna(pedido);

            if (creado == null) {
                throw new IllegalStateException(
                    "Pedidos no devolvió el pedido creado"
                );
            }

            return creado;

        } catch (RuntimeException errorCreacion) {

            try {
                productoRepository.liberarStock(reserva);
            } catch (RuntimeException errorLiberacion) {
                errorCreacion.addSuppressed(errorLiberacion);
            }

            throw errorCreacion;
        }
    }

    private void validarSolicitud(
        String entraObjectId,
        CheckoutRequest request
    ) {
        if (entraObjectId == null || entraObjectId.isBlank()) {
            throw new IllegalArgumentException(
                "No se pudo identificar al cliente autenticado"
            );
        }

        if (request == null
            || request.items() == null
            || request.items().isEmpty()) {

            throw new IllegalArgumentException(
                "El checkout debe contener al menos un producto"
            );
        }

        for (CheckoutItemRequest item : request.items()) {
            if (item == null
                || item.sku() == null
                || item.sku().isBlank()
                || item.cantidad() == null
                || item.cantidad() <= 0) {

                throw new IllegalArgumentException(
                    "Cada producto debe tener un SKU y una cantidad válida"
                );
            }
        }
    }
}