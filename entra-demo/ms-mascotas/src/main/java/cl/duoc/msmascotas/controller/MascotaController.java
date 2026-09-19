package cl.duoc.msmascotas.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.msmascotas.dto.MascotaResponse;
import cl.duoc.msmascotas.service.MascotaService;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    private final MascotaService mascotaService;

    public MascotaController(MascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    @GetMapping("/cliente/{clienteId}")
    public List<MascotaResponse> buscarPorCliente(@PathVariable Long clienteId) {
        return mascotaService.buscarPorCliente(clienteId);
    }
}