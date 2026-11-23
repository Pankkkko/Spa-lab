
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

export interface KpiResponse {
    totalPedidos: number;
    pedidosActivos: number;
    pedidosEntregados: number;
    pedidosCancelados: number;
    ventasTotales: number;
    tiempoPromedioEntregaMinutos: number;
}

export interface VentaPorHoraResponse {
    hora: string;
    cantidadPedidos: number;
    montoTotal: number;
}

export interface ProductoTopResponse {
    productoKey: string;
    productoNombre: string;
    unidadesVendidas: number;
    montoTotal: number;
    fechaVenta: string;
}

export interface PedidoEstadoResponse {
    estado: string;
    cantidad: number;
}

export interface TiempoEntregaResponse {
    pedidosEntregados: number;
    promedioMinutos: number;
    minimoMinutos: number;
    maximoMinutos: number;
}

// =========================================================
// TOKEN Y PETICIONES
// =========================================================

async function obtenerTokenReport(
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
    const accessToken = await obtenerTokenReport(instance, account);

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
            `Error al obtener información de Report. HTTP ${response.status}: ${body}`,
        );
    }

    return JSON.parse(body) as T;
}

// =========================================================
// OBTENER KPIs GENERALES
// GET /api/report/kpis
// =========================================================

export async function obtenerKpis(
    instance: IPublicClientApplication,
    account: AccountInfo,
): Promise<KpiResponse> {
    return obtenerDesdeBFF<KpiResponse>(
        instance,
        account,
        '/report/kpis',
    );
}

// =========================================================
// OBTENER VENTAS POR HORA
// GET /api/report/ventas/hora?desde=&hasta=
// =========================================================

export async function obtenerVentasPorHora(
    instance: IPublicClientApplication,
    account: AccountInfo,
    desde?: string,
    hasta?: string,
): Promise<VentaPorHoraResponse[]> {
    const params = new URLSearchParams();

    if (desde) params.append('desde', desde);
    if (hasta) params.append('hasta', hasta);

    const query = params.toString();
    const endpoint = `/report/ventas/hora${query ? `?${query}` : ''}`;

    return obtenerDesdeBFF<VentaPorHoraResponse[]>(
        instance,
        account,
        endpoint,
    );
}

// =========================================================
// OBTENER PRODUCTOS MÁS VENDIDOS
// GET /api/report/productos/top?desde=&hasta=
// =========================================================

export async function obtenerProductosTop(
    instance: IPublicClientApplication,
    account: AccountInfo,
    desde?: string,
    hasta?: string,
): Promise<ProductoTopResponse[]> {
    const params = new URLSearchParams();

    if (desde) params.append('desde', desde);
    if (hasta) params.append('hasta', hasta);

    const query = params.toString();
    const endpoint = `/report/productos/top${query ? `?${query}` : ''}`;

    return obtenerDesdeBFF<ProductoTopResponse[]>(
        instance,
        account,
        endpoint,
    );
}

// =========================================================
// OBTENER PEDIDOS POR ESTADO
// GET /api/report/pedidos/estados
// =========================================================

export async function obtenerPedidosPorEstado(
    instance: IPublicClientApplication,
    account: AccountInfo,
): Promise<PedidoEstadoResponse[]> {
    return obtenerDesdeBFF<PedidoEstadoResponse[]>(
        instance,
        account,
        '/report/pedidos/estados',
    );
}

// =========================================================
// OBTENER TIEMPOS DE ENTREGA
// GET /api/report/tiempos-entrega
// =========================================================

export async function obtenerTiemposEntrega(
    instance: IPublicClientApplication,
    account: AccountInfo,
): Promise<TiempoEntregaResponse> {
    return obtenerDesdeBFF<TiempoEntregaResponse>(
        instance,
        account,
        '/report/tiempos-entrega',
    );
}
