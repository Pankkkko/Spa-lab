import type {
    AccountInfo,
    IPublicClientApplication,
} from '@azure/msal-browser';

import { obtenerToken } from '../token';

const BFF_BASE_URL = 'https://tmbul2u2ic.execute-api.us-east-1.amazonaws.com/lanzar';

export interface Cliente {
    id: number;
    nombre: string;
    email: string;
    telefono: string;
}

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

export async function obtenerClientes(
    instance: IPublicClientApplication,
    account: AccountInfo,
): Promise<Cliente[]> {
    const result = await obtenerToken(instance, account);

    if (!result.accessToken) {
        throw new Error('No se obtuvo access token');
    }

    const response = await fetch(
        `${BFF_BASE_URL}/clientes`,
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
            `Error al obtener clientes desde el BFF. HTTP ${response.status}: ${body}`,
        );
    }

    return JSON.parse(body) as Cliente[];
}

export async function obtenerTodosLosPedidos(
    instance: IPublicClientApplication,
    account: AccountInfo,
): Promise<Pedido[]> {
    const result = await obtenerToken(instance, account);

    if (!result.accessToken) {
        throw new Error('No se obtuvo access token');
    }

    const response = await fetch(
        `${BFF_BASE_URL}/pedidos`,
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
            `Error al obtener todos los pedidos desde el BFF. HTTP ${response.status}: ${body}`,
        );
    }

    return JSON.parse(body) as Pedido[];
}

