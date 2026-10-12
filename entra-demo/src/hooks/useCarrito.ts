
import { useCallback, useEffect, useState } from 'react';
import type { Producto } from '../api/catalogoApi';

export interface CarritoItem {
    producto: Producto;
    cantidad: number;
}

const STORAGE_KEY = 'pedidos360_carrito_v1';

function cargarCarritoGuardado(): CarritoItem[] {
    try {
        const guardado = localStorage.getItem(STORAGE_KEY);

        if (!guardado) {
            return [];
        }

        const datos: unknown = JSON.parse(guardado);

        if (!Array.isArray(datos)) {
            return [];
        }

        return datos
            .filter((item): item is CarritoItem => {
                return (
                    item !== null &&
                    typeof item === 'object' &&
                    'producto' in item &&
                    'cantidad' in item &&
                    typeof item.cantidad === 'number' &&
                    Number.isFinite(item.cantidad) &&
                    item.producto !== null &&
                    typeof item.producto === 'object' &&
                    'sku' in item.producto &&
                    'stock' in item.producto &&
                    'precio' in item.producto
                );
            })
            .map((item) => {
                const stock = Math.max(
                    0,
                    Math.floor(Number(item.producto.stock) || 0),
                );

                const cantidad = Math.floor(item.cantidad);

                return {
                    producto: item.producto,
                    cantidad: Math.max(1, Math.min(cantidad, stock)),
                };
            })
            .filter((item) => item.producto.stock > 0)
            .filter((item) => item.cantidad > 0)
            .reduce<CarritoItem[]>((resultado, item) => {
                const existente = resultado.find(
                    (actual) => actual.producto.sku === item.producto.sku,
                );

                if (existente) {
                    existente.cantidad = Math.min(
                        existente.cantidad + item.cantidad,
                        item.producto.stock,
                    );
                } else {
                    resultado.push(item);
                }

                return resultado;
            }, []);
    } catch (error) {
        console.error('No se pudo recuperar el carrito:', error);
        return [];
    }
}

export function useCarrito() {
    const [items, setItems] = useState<CarritoItem[]>(cargarCarritoGuardado);

    useEffect(() => {
        try {
            localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
        } catch (error) {
            console.error('No se pudo guardar el carrito:', error);
        }
    }, [items]);

    const agregarProducto = useCallback((producto: Producto) => {
        const stock = Math.max(0, Math.floor(Number(producto.stock) || 0));

        if (stock < 1) {
            return false;
        }

        setItems((actuales) => {
            const existente = actuales.find(
                (item) => item.producto.sku === producto.sku,
            );

            if (existente) {
                return actuales.map((item) =>
                    item.producto.sku === producto.sku
                        ? {
                              producto,
                              cantidad: Math.min(item.cantidad + 1, stock),
                          }
                        : item,
                );
            }

            return [
                ...actuales,
                {
                    producto,
                    cantidad: 1,
                },
            ];
        });

        return true;
    }, []);

    const cambiarCantidad = useCallback(
        (sku: string, nuevaCantidad: number) => {
            setItems((actuales) =>
                actuales.map((item) => {
                    if (item.producto.sku !== sku) {
                        return item;
                    }

                    const stock = Math.max(
                        0,
                        Math.floor(Number(item.producto.stock) || 0),
                    );

                    return {
                        ...item,
                        cantidad: Math.max(
                            1,
                            Math.min(
                                Math.floor(Number(nuevaCantidad) || 1),
                                stock,
                            ),
                        ),
                    };
                }).filter((item) => item.producto.stock > 0),
            );
        },
        [],
    );

    const eliminarProducto = useCallback((sku: string) => {
        setItems((actuales) =>
            actuales.filter((item) => item.producto.sku !== sku),
        );
    }, []);

    const vaciarCarrito = useCallback(() => {
        setItems([]);
    }, []);

    const reconciliarStock = useCallback((productos: Producto[]) => {
        setItems((actuales) =>
            actuales
                .map((item) => {
                    const actualizado = productos.find(
                        (producto) => producto.sku === item.producto.sku,
                    );

                    // Si no aparece en la respuesta, conservamos el artículo.
                    if (!actualizado) {
                        return item;
                    }

                    const stock = Math.max(
                        0,
                        Math.floor(Number(actualizado.stock) || 0),
                    );

                    if (stock < 1) {
                        return null;
                    }

                    return {
                        producto: actualizado,
                        cantidad: Math.max(
                            1,
                            Math.min(item.cantidad, stock),
                        ),
                    };
                })
                .filter((item): item is CarritoItem => item !== null),
        );
    }, []);

    const cantidadTotal = items.reduce(
        (total, item) => total + item.cantidad,
        0,
    );

    const precioTotal = items.reduce(
        (total, item) => total + item.producto.precio * item.cantidad,
        0,
    );

    return {
        items,
        cantidadTotal,
        precioTotal,
        agregarProducto,
        cambiarCantidad,
        eliminarProducto,
        vaciarCarrito,
        reconciliarStock,
    };
}
