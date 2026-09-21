import type {
AccountInfo,
IPublicClientApplication,
} from '@azure/msal-browser';

import { obtenerToken } from '../token';

const PEDIDOS_BASE_URL = 'http://localhost:8082';

export interface PedidoDetalle {
producto: string;
cantidad: number;
precioUnitario: number;
subtotal: number;
}

export interface Pedido {
id: number;
clienteId: number;
clienteNombre: string;
estado: string;
fecha: string;
total: number;
detalles: PedidoDetalle[];
}

export async function obtenerPedidos(
instance: IPublicClientApplication,
account: AccountInfo,
clienteId: number,
): Promise<Pedido[]> {
const result = await obtenerToken(instance, account);


if (!result.accessToken) {
    throw new Error('No se obtuvo access token');
}

const response = await fetch(
    `${PEDIDOS_BASE_URL}/api/pedidos/cliente/${clienteId}`,
    {
        method: 'GET',
        headers: {
            Authorization: `Bearer ${result.accessToken}`,
            'Content-Type': 'application/json',
        },
    },
);

const body = await response.text();

if (!response.ok) {
    throw new Error(
        `Error al obtener pedidos. HTTP ${response.status}: ${body}`,
    );
}

return JSON.parse(body) as Pedido[];


}
