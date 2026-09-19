package cl.duoc.msmascotas.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cl.duoc.msmascotas.dto.MascotaResponse;

@Service
public class MascotaService {

    public List<MascotaResponse> buscarPorCliente(Long clienteId) {
        return List.of(
            new MascotaResponse(1L, "Firulais", "Perro", 5),
            new MascotaResponse(2L, "Michi", "Gato", 3)
        );
    }
}