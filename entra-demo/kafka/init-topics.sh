
#!/bin/bash

# ==========================================================
# PEDIDOS360 - KAFKA TOPIC INITIALIZER
# ==========================================================

set -euo pipefail

# Configuración de conexión
KAFKA_BOOTSTRAP_SERVERS="${KAFKA_BOOTSTRAP_SERVERS:-localhost:9092}"

# Configuración general
PARTITIONS=3
REPLICATION_FACTOR=1

# Retención en milisegundos
RETENTION_7_DAYS=604800000
RETENTION_14_DAYS=1209600000
RETENTION_30_DAYS=2592000000

# Ejecutable Kafka
KAFKA_TOPICS="${KAFKA_TOPICS_COMMAND:-kafka-topics.sh}"
KAFKA_CONFIGS="${KAFKA_CONFIGS_COMMAND:-kafka-configs.sh}"

# ==========================================================
# FUNCIONES
# ==========================================================

wait_for_kafka() {
    echo ""
    echo "Esperando disponibilidad de Kafka..."

    for attempt in $(seq 1 30); do
        if "$KAFKA_TOPICS" \
            --bootstrap-server "$KAFKA_BOOTSTRAP_SERVERS" \
            --list >/dev/null 2>&1; then

            echo "Kafka está disponible."
            return 0
        fi

        echo "Intento $attempt/30..."
        sleep 2
    done

    echo "ERROR: Kafka no respondió después de 60 segundos."
    exit 1
}

topic_exists() {
    local topic="$1"

    "$KAFKA_TOPICS" \
        --bootstrap-server "$KAFKA_BOOTSTRAP_SERVERS" \
        --list | grep -Fxq "$topic"
}

create_topic() {
    local topic="$1"
    local cleanup_policy="$2"
    local retention="$3"

    echo ""
    echo "--------------------------------------------------"
    echo "Procesando topic: $topic"
    echo "--------------------------------------------------"

    if topic_exists "$topic"; then
        echo "El topic ya existe. Se conservará."
    else
        echo "Creando topic..."

        "$KAFKA_TOPICS" \
            --bootstrap-server "$KAFKA_BOOTSTRAP_SERVERS" \
            --create \
            --topic "$topic" \
            --partitions "$PARTITIONS" \
            --replication-factor "$REPLICATION_FACTOR"

        echo "Topic creado."
    fi

    echo "Aplicando configuración..."

    "$KAFKA_CONFIGS" \
        --bootstrap-server "$KAFKA_BOOTSTRAP_SERVERS" \
        --entity-type topics \
        --entity-name "$topic" \
        --alter \
        --add-config "cleanup.policy=[$cleanup_policy],retention.ms=$retention"

    echo "Configuración aplicada correctamente."
}

verify_topic() {
    local topic="$1"

    echo ""
    echo "Verificando: $topic"

    "$KAFKA_TOPICS" \
        --bootstrap-server "$KAFKA_BOOTSTRAP_SERVERS" \
        --describe \
        --topic "$topic"

    "$KAFKA_CONFIGS" \
        --bootstrap-server "$KAFKA_BOOTSTRAP_SERVERS" \
        --entity-type topics \
        --entity-name "$topic" \
        --describe
}

# ==========================================================
# INICIALIZACIÓN
# ==========================================================

echo "=================================================="
echo " PEDIDOS360 - INICIALIZACIÓN DE KAFKA"
echo "=================================================="

echo "Broker: $KAFKA_BOOTSTRAP_SERVERS"
echo "Particiones: $PARTITIONS"
echo "Factor de replicación: $REPLICATION_FACTOR"

wait_for_kafka

# ==========================================================
# TOPICS PRINCIPALES
# ==========================================================

create_topic \
    "orders.events" \
    "delete" \
    "$RETENTION_7_DAYS"

create_topic \
    "audit.timeline" \
    "compact,delete" \
    "$RETENTION_30_DAYS"

# ==========================================================
# DEAD LETTER TOPICS
# ==========================================================

create_topic \
    "orders.events.reportes.DLT" \
    "delete" \
    "$RETENTION_14_DAYS"

create_topic \
    "orders.events.auditoria.DLT" \
    "delete" \
    "$RETENTION_14_DAYS"

# ==========================================================
# VERIFICACIÓN FINAL
# ==========================================================

echo ""
echo "=================================================="
echo " VERIFICACIÓN DE TOPICS"
echo "=================================================="

verify_topic "orders.events"
verify_topic "audit.timeline"
verify_topic "orders.events.reportes.DLT"
verify_topic "orders.events.auditoria.DLT"

echo ""
echo "=================================================="
echo " INICIALIZACIÓN COMPLETADA"
echo "=================================================="
