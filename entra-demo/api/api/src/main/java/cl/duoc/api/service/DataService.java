package cl.duoc.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import cl.duoc.api.dto.ClienteResponse;
import cl.duoc.api.dto.MascotaResponse;
import cl.duoc.api.repository.ClienteRepository;
import cl.duoc.api.repository.MascotaRepository;

@Service
public class DataService {

    private final ClienteRepository clienteRepository;
    private final MascotaRepository mascotaRepository;

    public DataService(
        ClienteRepository clienteRepository,
        MascotaRepository mascotaRepository
    ) {
        this.clienteRepository = clienteRepository;
        this.mascotaRepository = mascotaRepository;
    }

    public ClienteResponse obtenerData() {
        ClienteResponse cliente = clienteRepository.buscarPorId(1L);
        List<MascotaResponse> mascotas = mascotaRepository.buscarPorCliente(1L);

        return new ClienteResponse(
            cliente.id(),
            cliente.nombre(),
            cliente.email(),
            mascotas
        );
    }
}