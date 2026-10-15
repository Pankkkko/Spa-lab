package cl.duoc.mspedidos.entity;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum PedidoEstado {
    CREADO,
    ACEPTADO,
    EN_PREPARACION,
    DESPACHADO,
    ENTREGADO,
    CANCELADO;

    public static String validar(String estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado del pedido es obligatorio");
        }

        String estadoNormalizado = estado.trim().toUpperCase();
        if (estadoNormalizado.equals("PENDIENTE")) {
            return CREADO.name();
        }
        if (estadoNormalizado.equals("ENVIADO")) {
            return DESPACHADO.name();
        }

        return Arrays.stream(values())
            .map(Enum::name)
            .filter(estadoNormalizado::equals)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Estado inválido. Valores permitidos: " + Arrays.toString(values())));
    }

    public static void validarTransicion(String estadoActual, String nuevoEstado) {
        PedidoEstado actual = valueOf(validar(estadoActual));
        PedidoEstado siguiente = valueOf(validar(nuevoEstado));

        if (actual == siguiente) {
            return;
        }

        Set<PedidoEstado> estadosPermitidos = Map.of(
            CREADO, EnumSet.of(ACEPTADO, CANCELADO),
            ACEPTADO, EnumSet.of(EN_PREPARACION, DESPACHADO, CANCELADO),
            EN_PREPARACION, EnumSet.of(DESPACHADO, CANCELADO),
            DESPACHADO, EnumSet.of(ENTREGADO),
            ENTREGADO, EnumSet.noneOf(PedidoEstado.class),
            CANCELADO, EnumSet.noneOf(PedidoEstado.class)
        ).get(actual);

        if (!estadosPermitidos.contains(siguiente)) {
            throw new IllegalArgumentException(
                "No se puede cambiar el pedido de " + actual + " a " + siguiente);
        }
    }
}
