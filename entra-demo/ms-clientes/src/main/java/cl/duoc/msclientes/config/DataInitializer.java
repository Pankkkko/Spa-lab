package cl.duoc.msclientes.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.duoc.msclientes.entity.Cliente;
import cl.duoc.msclientes.repository.ClienteRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initClientes(ClienteRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new Cliente("Wacoldo Soto", "waco.soto@duocuc.cl", "+56 9 1234 5678"));
                repository.save(new Cliente("Ana Perez", "ana.perez@duocuc.cl", "+56 9 8765 4321"));
                repository.save(new Cliente("Carlos Rojas", "carlos.rojas@duocuc.cl", "+56 9 5555 6666"));
                System.out.println("✅ Datos iniciales de clientes cargados");
            } else {
                System.out.println("ℹ️ Clientes ya existen, no se cargan datos iniciales");
            }
        };
    }
}