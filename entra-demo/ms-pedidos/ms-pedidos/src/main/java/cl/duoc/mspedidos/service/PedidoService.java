package cl.duoc.mspedidos.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import cl.duoc.mspedidos.dto.PedidoDetalleResponse;
import cl.duoc.mspedidos.dto.PedidoResponse;

@Service
public class PedidoService {

    public List<PedidoResponse> buscarPorCliente(Long clienteId) {
        List<PedidoDetalleResponse> detalles1 = List.of(
            new PedidoDetalleResponse("Notebook Lenovo", 1, 749990.0, 749990.0),
            new PedidoDetalleResponse("Mouse Logitech", 2, 29990.0, 59980.0)
        );

        List<PedidoDetalleResponse> detalles2 = List.of(
            new PedidoDetalleResponse("Teclado Mecánico", 1, 89990.0, 89990.0)
        );

        List<PedidoDetalleResponse> detalles3 = List.of(
            new PedidoDetalleResponse("Monitor 24\"", 1, 149990.0, 149990.0),
            new PedidoDetalleResponse("Cable HDMI", 1, 7990.0, 7990.0)
        );

        return List.of(
            new PedidoResponse(1L, clienteId, "Wacoldo Soto", "ENVIADO",
                LocalDate.of(2026, 9, 1),
                detalles1.stream().mapToDouble(PedidoDetalleResponse::subtotal).sum(),
                detalles1),
            new PedidoResponse(2L, clienteId, "Wacoldo Soto", "ENTREGADO",
                LocalDate.of(2026, 8, 15),
                detalles2.stream().mapToDouble(PedidoDetalleResponse::subtotal).sum(),
                detalles2),
            new PedidoResponse(3L, clienteId, "Wacoldo Soto", "PENDIENTE",
                LocalDate.of(2026, 9, 12),
                detalles3.stream().mapToDouble(PedidoDetalleResponse::subtotal).sum(),
                detalles3)
        );
    }
}