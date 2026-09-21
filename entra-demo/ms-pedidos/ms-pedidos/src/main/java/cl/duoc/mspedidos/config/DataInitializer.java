package cl.duoc.mspedidos.config;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.duoc.mspedidos.entity.Pedido;
import cl.duoc.mspedidos.entity.PedidoDetalle;
import cl.duoc.mspedidos.repository.PedidoRepository;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initPedidos(PedidoRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                // Pedido 1: ENVIADO
                Pedido p1 = new Pedido(1L, "ENVIADO", LocalDate.of(2026, 9, 1));
                p1.agregarDetalle(new PedidoDetalle("Notebook Lenovo", 1, 749990.0));
                p1.agregarDetalle(new PedidoDetalle("Mouse Logitech", 2, 29990.0));
                repository.save(p1);

                // Pedido 2: ENTREGADO
                Pedido p2 = new Pedido(1L, "ENTREGADO", LocalDate.of(2026, 8, 15));
                p2.agregarDetalle(new PedidoDetalle("Teclado Mecánico", 1, 89990.0));
                repository.save(p2);

                // Pedido 3: PENDIENTE
                Pedido p3 = new Pedido(1L, "PENDIENTE", LocalDate.of(2026, 9, 12));
                p3.agregarDetalle(new PedidoDetalle("Monitor 24\"", 1, 149990.0));
                p3.agregarDetalle(new PedidoDetalle("Cable HDMI", 1, 7990.0));
                repository.save(p3);

                System.out.println("✅ Datos iniciales de pedidos cargados");
            } else {
                System.out.println("ℹ️ Pedidos ya existen, no se cargan datos iniciales");
            }
        };
    }
}