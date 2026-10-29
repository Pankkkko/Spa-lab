import type {
    AccountInfo,
    IPublicClientApplication,
} from '@azure/msal-browser';

import { obtenerToken } from '../token';

const BFF_BASE_URL = 'http://54.80.126.170:8080/api' 
//const BFF_BASE_URL = 'https://v9okws1wjb.execute-api.us-east-1.amazonaws.com/v1/api';


export interface Producto {
    sku: string;
    nombre: string;
    descripcion: string;
    categoria: string;
    precio: number;
    imagenes: string[];
    stock: number;
}

export interface ProductoRequest {
    sku: string;
    nombre: string;
    descripcion: string;
    categoria: string;
    precio: number;
    imagenes: string[];
    stock: number;
}

export interface ProductoPage {
    content: Producto[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    numberOfElements: number;
    first: boolean;
    last: boolean;
    empty: boolean;
}
export interface CheckoutItemRequest {
    sku: string;
    cantidad: number;
}

export interface PedidoCreado {
    id: number;
    clienteId: number;
    clienteNombre: string | null;
    estado: string;
    fecha: string;
    total: number;
    detalles: {
        producto: string;
        cantidad: number;
        precioUnitario: number;
        subtotal: number;
    }[];
}

async function obtenerTokenCatalogo(
    instance: IPublicClientApplication,
    account: AccountInfo,
): Promise<string> {

    const result = await obtenerToken(instance, account);

    if (!result.accessToken) {
        throw new Error('No se obtuvo access token');
    }

    return result.accessToken;
}

/**
 * Obtener todos los productos paginados.
 */
export async function obtenerProductos(
    instance?: IPublicClientApplication,
    account?: AccountInfo,
    page: number = 0,
    size: number = 20,
): Promise<ProductoPage> {
    const headers: Record<string, string> = {};

    if (instance && account) {
        const accessToken = await obtenerTokenCatalogo(
            instance,
            account,
        );

        headers.Authorization = `Bearer ${accessToken}`;
    }

    const response = await fetch(
        `${BFF_BASE_URL}/productos?page=${page}&size=${size}`,
        {
            method: 'GET',
            headers,
        },
    );

    const body = await response.text();

    if (!response.ok) {
        throw new Error(
            `Error al obtener productos desde el BFF. HTTP ${response.status}: ${body}`,
        );
    }

    return JSON.parse(body) as ProductoPage;
}

/**
 * Obtener un producto mediante su SKU.
 */
export async function obtenerProductoPorSku(
    instance: IPublicClientApplication,
    account: AccountInfo,
    sku: string,
): Promise<Producto> {

    const accessToken = await obtenerTokenCatalogo(
        instance,
        account,
    );

    const response = await fetch(
        `${BFF_BASE_URL}/productos/${encodeURIComponent(sku)}`,
        {
            method: 'GET',
            headers: {
                Authorization: `Bearer ${accessToken}`,
                'Content-Type': 'application/json',
            },
        },
    );

    const body = await response.text();

    if (!response.ok) {
        throw new Error(
            `Error al obtener el producto desde el BFF. HTTP ${response.status}: ${body}`,
        );
    }

    return JSON.parse(body) as Producto;
}

/**
 * Buscar productos por nombre.
 */
export async function buscarProductosPorNombre(
    instance: IPublicClientApplication,
    account: AccountInfo,
    nombre: string,
    page: number = 0,
    size: number = 20,
): Promise<ProductoPage> {

    const accessToken = await obtenerTokenCatalogo(
        instance,
        account,
    );

    const response = await fetch(
        `${BFF_BASE_URL}/productos/buscar/nombre?nombre=${encodeURIComponent(nombre)}&page=${page}&size=${size}`,
        {
            method: 'GET',
            headers: {
                Authorization: `Bearer ${accessToken}`,
                'Content-Type': 'application/json',
            },
        },
    );

    const body = await response.text();

    if (!response.ok) {
        throw new Error(
            `Error al buscar productos por nombre. HTTP ${response.status}: ${body}`,
        );
    }

    return JSON.parse(body) as ProductoPage;
}

/**
 * Buscar productos por categoría.
 */
export async function buscarProductosPorCategoria(
    instance: IPublicClientApplication,
    account: AccountInfo,
    categoria: string,
    page: number = 0,
    size: number = 20,
): Promise<ProductoPage> {

    const accessToken = await obtenerTokenCatalogo(
        instance,
        account,
    );

    const response = await fetch(
        `${BFF_BASE_URL}/productos/buscar/categoria?categoria=${encodeURIComponent(categoria)}&page=${page}&size=${size}`,
        {
            method: 'GET',
            headers: {
                Authorization: `Bearer ${accessToken}`,
                'Content-Type': 'application/json',
            },
        },
    );

    const body = await response.text();

    if (!response.ok) {
        throw new Error(
            `Error al buscar productos por categoría. HTTP ${response.status}: ${body}`,
        );
    }

    return JSON.parse(body) as ProductoPage;
}

/**
 * Buscar productos por rango de precio.
 */
export async function buscarProductosPorPrecio(
    instance: IPublicClientApplication,
    account: AccountInfo,
    min: number,
    max: number,
    page: number = 0,
    size: number = 20,
): Promise<ProductoPage> {

    const accessToken = await obtenerTokenCatalogo(
        instance,
        account,
    );

    const response = await fetch(
        `${BFF_BASE_URL}/productos/buscar/precio?min=${min}&max=${max}&page=${page}&size=${size}`,
        {
            method: 'GET',
            headers: {
                Authorization: `Bearer ${accessToken}`,
                'Content-Type': 'application/json',
            },
        },
    );

    const body = await response.text();

    if (!response.ok) {
        throw new Error(
            `Error al buscar productos por precio. HTTP ${response.status}: ${body}`,
        );
    }

    return JSON.parse(body) as ProductoPage;
}

/**
 * Obtener el stock de un producto.
 */
export async function obtenerStockProducto(
    instance: IPublicClientApplication,
    account: AccountInfo,
    sku: string,
): Promise<number> {

    const accessToken = await obtenerTokenCatalogo(
        instance,
        account,
    );

    const response = await fetch(
        `${BFF_BASE_URL}/productos/${encodeURIComponent(sku)}/stock`,
        {
            method: 'GET',
            headers: {
                Authorization: `Bearer ${accessToken}`,
                'Content-Type': 'application/json',
            },
        },
    );

    const body = await response.text();

    if (!response.ok) {
        throw new Error(
            `Error al obtener el stock del producto. HTTP ${response.status}: ${body}`,
        );
    }

    return JSON.parse(body) as number;

}

/**
 * Envía el carrito al BFF para realizar el checkout.
 * El servidor consulta los precios y valida el stock.
 */
export async function realizarCheckout(
    instance: IPublicClientApplication,
    account: AccountInfo,
    items: CheckoutItemRequest[],
): Promise<PedidoCreado> {
    const accessToken = await obtenerTokenCatalogo(instance, account);

    const response = await fetch(
        `${BFF_BASE_URL}/pedidos/checkout`,
        {
            method: 'POST',
            headers: {
                Authorization: `Bearer ${accessToken}`,
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ items }),
        },
    );

    const body = await response.text();

    if (!response.ok) {
        throw new Error(
            `No fue posible completar la compra. HTTP ${response.status}: ${body}`,
        );
    }

    if (!body.trim()) {
        throw new Error(
            'El servidor confirmó una respuesta vacía. Verifica el estado del pedido antes de volver a intentarlo.',
        );
    }

    return JSON.parse(body) as PedidoCreado;
}

// =========================================================
// CREAR PRODUCTO (ADMIN)
// =========================================================
export async function crearProducto(
    instance: IPublicClientApplication,
    account: AccountInfo,
    producto: ProductoRequest,
): Promise<Producto> {
    const token = await obtenerTokenCatalogo(instance, account);

    const response = await fetch(`${BFF_BASE_URL}/productos`, {
        method: 'POST',
        headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(producto),
    });

    const body = await response.text();

    if (!response.ok) {
        throw new Error(
            `Error al crear el producto. HTTP ${response.status}: ${body}`,
        );
    }

    return JSON.parse(body) as Producto;
}

// =========================================================
// EDITAR PRODUCTO (ADMIN)
// =========================================================
export async function actualizarProducto(
    instance: IPublicClientApplication,
    account: AccountInfo,
    sku: string,
    producto: ProductoRequest,
): Promise<Producto> {
    const token = await obtenerTokenCatalogo(instance, account);

    const response = await fetch(
        `${BFF_BASE_URL}/productos/${encodeURIComponent(sku)}`,
        {
            method: 'PUT',
            headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                ...producto,
                sku,
            }),
        },
    );

    const body = await response.text();

    if (!response.ok) {
        throw new Error(
            `Error al actualizar el producto. HTTP ${response.status}: ${body}`,
        );
    }

    return JSON.parse(body) as Producto;
}