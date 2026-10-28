
import type {
    AccountInfo,
    IPublicClientApplication,
} from '@azure/msal-browser';

import { obtenerToken } from '../token';

//const BFF_BASE_URL = 'http://54.210.141.75:8080/api';
const BFF_BASE_URL = 'https://v9okws1wjb.execute-api.us-east-1.amazonaws.com/v1/api';

export interface ClientePerfil {
    id: number;
    nombre: string;
    email: string;
    telefono: string | null;
}

export interface CrearClientePerfilRequest {
    nombre: string;
    telefono: string;
}

export class ClienteApiError extends Error {
    constructor(
        message: string,
        public readonly status: number,
    ) {
        super(message);
        this.name = 'ClienteApiError';
    }
}

async function solicitar<T>(
    instance: IPublicClientApplication,
    account: AccountInfo,
    ruta: string,
    opciones: RequestInit = {},
): Promise<T> {
    const token = await obtenerToken(instance, account);

    if (!token.accessToken) {
        throw new Error('No se obtuvo un token de acceso.');
    }

    // Preparamos las cabeceras de forma compatible con RequestInit.
    const headers = new Headers(opciones.headers);

    headers.set('Authorization', `Bearer ${token.accessToken}`);
    headers.set('Content-Type', 'application/json');

    const response = await fetch(`${BFF_BASE_URL}${ruta}`, {
        ...opciones,
        headers,
    });

    const contenido = await response.text();

    if (!response.ok) {
        throw new ClienteApiError(
            contenido || `Error HTTP ${response.status}`,
            response.status,
        );
    }

    if (!contenido) {
        throw new Error('El servidor devolvió una respuesta vacía.');
    }

    return JSON.parse(contenido) as T;
}

export function obtenerMiPerfil(
    instance: IPublicClientApplication,
    account: AccountInfo,
): Promise<ClientePerfil> {
    return solicitar<ClientePerfil>(
        instance,
        account,
        '/clientes/me',
    );
}

export function crearMiPerfil(
    instance: IPublicClientApplication,
    account: AccountInfo,
    datos: CrearClientePerfilRequest,
): Promise<ClientePerfil> {
    return solicitar<ClientePerfil>(
        instance,
        account,
        '/clientes/perfil',
        {
            method: 'POST',
            body: JSON.stringify(datos),
        },
    );
}
