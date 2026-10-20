
package cl.duoc.ms_pedidos360_notify.messaging;

import cl.duoc.ms_pedidos360_notify.config.RabbitMessagingConfig;
import cl.duoc.ms_pedidos360_notify.dto.ComandoEnvelope;
import cl.duoc.ms_pedidos360_notify.service.NotificacionService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoNotificationListener {

    private final NotificacionService notificacionService;

    @RabbitListener(
            queues = RabbitMessagingConfig.QUEUE_EMAIL,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void recibirEmail(
            @Payload ComandoEnvelope evento,
            Message mensaje,
            Channel canal
    ) throws IOException {
        procesarConConfirmacion(
                "email",
                evento,
                mensaje,
                canal,
                () -> notificacionService.procesarEmail(evento)
        );
    }

    @RabbitListener(
            queues = RabbitMessagingConfig.QUEUE_PEDIDOS,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void recibirPedido(
            @Payload ComandoEnvelope evento,
            Message mensaje,
            Channel canal
    ) throws IOException {
        procesarConConfirmacion(
                "pedidos",
                evento,
                mensaje,
                canal,
                () -> notificacionService.procesarPedido(evento)
        );
    }

    @RabbitListener(
            queues = RabbitMessagingConfig.QUEUE_INVOICE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void recibirFactura(
            @Payload ComandoEnvelope evento,
            Message mensaje,
            Channel canal
    ) throws IOException {
        procesarConConfirmacion(
                "invoice",
                evento,
                mensaje,
                canal,
                () -> notificacionService.procesarFactura(evento)
        );
    }

    private void procesarConConfirmacion(
            String flujo,
            ComandoEnvelope evento,
            Message mensaje,
            Channel canal,
            Procesador procesador
    ) throws IOException {

        long deliveryTag =
                mensaje.getMessageProperties().getDeliveryTag();

        for (int intento = 1;
             intento <= RabbitMessagingConfig.MAX_INTENTOS;
             intento++) {

            try {
                procesador.ejecutar();

                // ACK: el mensaje se procesó correctamente.
                canal.basicAck(deliveryTag, false);

                log.info(
                        "ACK confirmado. Flujo: {}, eventId: {}, intento: {}",
                        flujo,
                        evento.eventId(),
                        intento
                );

                return;

            } catch (Exception ex) {

                log.error(
                        "Falló el flujo {}. eventId: {}, intento: {}/{}",
                        flujo,
                        evento != null ? evento.eventId() : null,
                        intento,
                        RabbitMessagingConfig.MAX_INTENTOS,
                        ex
                );

                if (intento < RabbitMessagingConfig.MAX_INTENTOS) {
                    try {
                        Thread.sleep(
                                RabbitMessagingConfig.ESPERA_REINTENTO_MS
                        );
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();

                        canal.basicNack(deliveryTag, false, false);
                        return;
                    }
                }
            }
        }

        // NACK sin reencolar. La configuración de la cola
        // deriva el mensaje a su DLQ correspondiente.
        canal.basicNack(deliveryTag, false, false);

        log.error(
                "Mensaje enviado a DLQ tras agotar intentos. Flujo: {}, eventId: {}",
                flujo,
                evento != null ? evento.eventId() : null
        );
    }

    @FunctionalInterface
    private interface Procesador {
        void ejecutar() throws Exception;
    }
}
