package cl.duoc.mspedidos.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.mspedidos.dto.PedidoResponse;
import cl.duoc.mspedidos.service.PedidoService;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping("/cliente/{clienteId}")
    public List<PedidoResponse> buscarPorCliente(@PathVariable Long clienteId) {
        return pedidoService.buscarPorCliente(clienteId);
    }
}