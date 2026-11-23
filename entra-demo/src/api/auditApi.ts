
import type {
    AccountInfo,
    IPublicClientApplication,
} from '@azure/msal-browser';

import { obtenerToken } from '../token';

const BFF_BASE_URL = 'http://localhost:8080/api' 
//const BFF_BASE_URL = 'http://54.80.126.170:8080/api';
// const BFF_BASE_URL = 'https://v9okws1wjb.execute-api.us-east-1.amazonaws.com/v1/api';

// =========================================================
// INTERFACES
// =========================================================

export interface AuditRegistroResponse {
    id: number;
    eventId: string;
    usuarioId: string | null;
    usuarioNombre: string | null;
    tipoEvento: string;
    entidad: string;
    entidadId: string;
    descripcion: string | null;
    fechaEvento: string;
    origen: string | null;
    traceId: string | null;
    correlationId: string | null;
}

export interface AuditRegistroDetalleResponse
    extends AuditRegistroResponse {
    payload: string | null;
    fechaRecepcion: string;
}

export interface PageResponse<T> {
    content: T[];
    number: number;
    size: number;
    totalPages: number;
    totalElements: number;
    numberOfElements: number;
    first: boolean;
    last: boolean;
    empty: boolean;
}

export interface AuditFiltros {
    usuarioId?: string;
    tipoEvento?: string;
    desde?: string;
    hasta?: string;
    page?: number;
    size?: number;
}

// =========================================================
// TOKEN Y PETICIONES
// =========================================================

async function obtenerTokenAudit(
    instance: IPublicClientApplication,
    account: AccountInfo,
): Promise<string> {
    const result = await obtenerToken(instance, account);

    if (!result.accessToken) {
        throw new Error('No se obtuvo access token');
    }

    return result.accessToken;
}

async function obtenerDesdeBFF<T>(
    instance: IPublicClientApplication,
    account: AccountInfo,
    endpoint: string,
): Promise<T> {
    const accessToken = await obtenerTokenAudit(instance, account);

    const response = await fetch(`${BFF_BASE_URL}${endpoint}`, {
        method: 'GET',
        headers: {
            Authorization: `Bearer ${accessToken}`,
            'Content-Type': 'application/json',
        },
    });

    const body = await response.text();

    if (!response.ok) {
        throw new Error(
            `Error al obtener información de Audit. HTTP ${response.status}: ${body}`,
        );
    }

    return JSON.parse(body) as T;
}

// =========================================================
// OBTENER REGISTROS DE AUDITORÍA CON FILTROS
// GET /api/audit
// =========================================================

export async function obtenerRegistrosAudit(
    instance: IPublicClientApplication,
    account: AccountInfo,
    filtros: AuditFiltros = {},
): Promise<PageResponse<AuditRegistroResponse>> {
    const params = new URLSearchParams();

    if (filtros.usuarioId) {
        params.append('usuarioId', filtros.usuarioId);
    }

    if (filtros.tipoEvento) {
        params.append('tipoEvento', filtros.tipoEvento);
    }

    if (filtros.desde) {
        params.append('desde', filtros.desde);
    }

    if (filtros.hasta) {
        params.append('hasta', filtros.hasta);
    }

    params.append('page', String(filtros.page ?? 0));
    params.append('size', String(filtros.size ?? 20));

    return obtenerDesdeBFF<PageResponse<AuditRegistroResponse>>(
        instance,
        account,
        `/audit?${params.toString()}`,
    );
}

// =========================================================
// OBTENER HISTORIAL DE UN PEDIDO
// GET /api/audit/pedido/{pedidoId}
// =========================================================

export async function obtenerAuditPorPedido(
    instance: IPublicClientApplication,
    account: AccountInfo,
    pedidoId: number | string,
    page: number = 0,
    size: number = 20,
): Promise<PageResponse<AuditRegistroResponse>> {
    const params = new URLSearchParams({
        page: String(page),
        size: String(size),
    });

    return obtenerDesdeBFF<PageResponse<AuditRegistroResponse>>(
        instance,
        account,
        `/audit/pedido/${encodeURIComponent(String(pedidoId))}?${params.toString()}`,
    );
}

// =========================================================
// OBTENER DETALLE DE UN REGISTRO
// GET /api/audit/{id}
// =========================================================

export async function obtenerDetalleAudit(
    instance: IPublicClientApplication,
    account: AccountInfo,
    id: number,
): Promise<AuditRegistroDetalleResponse> {
    return obtenerDesdeBFF<AuditRegistroDetalleResponse>(
        instance,
        account,
        `/audit/${encodeURIComponent(String(id))}`,
    );
}
