package cl.duoc.ms_pedidos360_notify.messaging;

import com.rabbitmq.client.Channel;

import cl.duoc.ms_pedidos360_notify.config.RabbitMessagingConfig;
import cl.duoc.ms_pedidos360_notify.dto.PedidoCreadoEvent;
import cl.duoc.ms_pedidos360_notify.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PedidoNotificationListener {

    private final NotificacionService notificacionService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(
            queues = RabbitMessagingConfig.QUEUE_NOTIFICACIONES,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void recibirPedido(
            @Payload PedidoCreadoEvent evento,
            Message mensaje,
            Channel channel
    ) throws IOException {

        long deliveryTag =
                mensaje.getMessageProperties().getDeliveryTag();

        Object header = mensaje.getMessageProperties()
                .getHeaders()
                .get(RabbitMessagingConfig.HEADER_REINTENTOS);

        int intentos = header instanceof Number numero
                ? numero.intValue()
                : 0;

        try {
            notificacionService.procesarPedidoCreado(evento);

            // ACK: el mensaje fue procesado correctamente.
            channel.basicAck(deliveryTag, false);

            log.info(
                    "Mensaje confirmado. Pedido ID: {}",
                    evento.pedidoId()
            );

        } catch (Exception ex) {

            log.error(
                    "Error procesando evento del pedido {}. Intento: {}",
                    evento != null ? evento.pedidoId() : null,
                    intentos + 1,
                    ex
            );

            if (intentos + 1 < RabbitMessagingConfig.MAX_INTENTOS) {

                try {
                    // Reenvía el evento a la cola con espera de 5 segundos.
                    rabbitTemplate.convertAndSend(
                            "",
                            RabbitMessagingConfig.QUEUE_REINTENTOS,
                            evento,
                            mensajeReintento -> {
                                mensajeReintento.getMessageProperties()
                                        .setHeader(
                                                RabbitMessagingConfig.HEADER_REINTENTOS,
                                                intentos + 1
                                        );

                                return mensajeReintento;
                            }
                    );

                    // Confirmamos el original después de publicar el reintento.
                    channel.basicAck(deliveryTag, false);

                    log.warn(
                            "Pedido enviado a reintento número {}",
                            intentos + 1
                    );

                } catch (Exception errorReintento) {

                    log.error(
                            "No se pudo publicar el reintento. Se enviará el original a la DLQ.",
                            errorReintento
                    );

                    channel.basicNack(deliveryTag, false, false);
                }

            } else {

                // NACK sin reencolar: la configuración de la cola
                // deriva el mensaje fallido a la DLQ.
                channel.basicNack(deliveryTag, false, false);

                log.error(
                        "Se agotaron los intentos. Mensaje enviado a la DLQ."
                );
            }
        }
    }
}