package cl.duoc.msclientes.service;

import org.springframework.stereotype.Service;

import cl.duoc.msclientes.dto.ClienteResponse;

@Service
public class ClienteService {

    public ClienteResponse buscarPorId(Long id) {
        return new ClienteResponse(
            id,
            "Wacoldo Soto",
            "waco.soto@duocuc.cl"
        );
    }
}