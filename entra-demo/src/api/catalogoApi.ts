import type {
    AccountInfo,
    IPublicClientApplication,
} from '@azure/msal-browser';

import { obtenerToken } from '../token';

const BFF_BASE_URL = 'http://localhost:8080';
// const BFF_BASE_URL = 'https://tmbul2u2ic.execute-api.us-east-1.amazonaws.com/lanzar';

export interface Producto {
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
    instance: IPublicClientApplication,
    account: AccountInfo,
    page: number = 0,
    size: number = 20,
): Promise<ProductoPage> {

    const accessToken = await obtenerTokenCatalogo(
        instance,
        account,
    );

    const response = await fetch(
        `${BFF_BASE_URL}/productos?page=${page}&size=${size}`,
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