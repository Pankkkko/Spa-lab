
# Pedidos360 - Kafka

Scripts para inicializar y verificar la infraestructura Kafka
del proyecto Pedidos360.

## Requisitos

- Docker con Kafka en ejecución.
- Apache Kafka CLI disponible.
- Acceso al broker Kafka.

## Configuración local

Broker predeterminado:

localhost:9092

## Ejecutar inicialización

Dar permisos de ejecución:

```bash
chmod +x init-topics.sh
```

Ejecutar:

```bash
./init-topics.sh
```

## Ejecutar contra otro broker

```bash
KAFKA_BOOTSTRAP_SERVERS=10.0.0.10:9092 ./init-topics.sh
```

## Topics configurados

| Topic | Particiones | Cleanup | Retención |
|---|---:|---|---|
| orders.events | 3 | delete | 7 días |
| audit.timeline | 3 | compact,delete | 30 días |
| orders.events.reportes.DLT | 3 | delete | 14 días |
| orders.events.auditoria.DLT | 3 | delete | 14 días |

Factor de replicación: 1 (laboratorio de un solo broker).
