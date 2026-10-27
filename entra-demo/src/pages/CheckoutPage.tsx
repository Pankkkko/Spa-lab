
import { useState } from 'react';
import type {
    AccountInfo,
    IPublicClientApplication,
} from '@azure/msal-browser';

import {
    realizarCheckout,
    type PedidoCreado,
} from '../api/catalogoApi';
import type { CarritoItem } from '../hooks/useCarrito';

import '../styles/checkout.css';

interface CheckoutPageProps {
    items: CarritoItem[];
    instance: IPublicClientApplication;
    account: AccountInfo | null;
    onVolver: () => void;
    onLogin: () => void;
    onCompraExitosa: () => void;
}

function formatearPrecio(precio: number): string {
    return new Intl.NumberFormat('es-CL', {
        style: 'currency',
        currency: 'CLP',
        maximumFractionDigits: 0,
    }).format(precio);
}

export default function CheckoutPage({
    items,
    instance,
    account,
    onVolver,
    onLogin,
    onCompraExitosa,
}: CheckoutPageProps) {
    const [procesando, setProcesando] = useState(false);
    const [error, setError] = useState('');
    const [pedido, setPedido] = useState<PedidoCreado | null>(null);

    const totalReferencial = items.reduce(
        (total, item) =>
            total + item.producto.precio * item.cantidad,
        0,
    );

    async function confirmarCompra() {
        if (!account) {
            setError('Debes iniciar sesión antes de realizar una compra.');
            return;
        }

        if (items.length === 0) {
            setError('Tu carrito está vacío.');
            return;
        }

        setProcesando(true);
        setError('');

        try {
            const creado = await realizarCheckout(
                instance,
                account,
                items.map(({ producto, cantidad }) => ({
                    sku: producto.sku,
                    cantidad,
                })),
            );

            setPedido(creado);
            onCompraExitosa();
        } catch (e) {
            console.error('Error durante el checkout:', e);

            setError(
                e instanceof Error
                    ? e.message
                    : 'Ocurrió un error inesperado al procesar la compra.',
            );
        } finally {
            setProcesando(false);
        }
    }

    if (pedido) {
        return (
            <main className="checkout-page">
                <section className="checkout-resultado">
                    <div className="checkout-exito-icono">✓</div>

                    <span className="checkout-etiqueta">
                        PEDIDOS360
                    </span>

                    <h1>¡Pedido creado correctamente!</h1>

                    <p>
                        Tu compra fue registrada. El pedido ya está
                        disponible en el sistema.
                    </p>

                    <div className="checkout-pedido-dato">
                        <span>Número de pedido</span>
                        <strong>#{pedido.id}</strong>
                    </div>

                    <div className="checkout-pedido-dato">
                        <span>Estado</span>
                        <strong>{pedido.estado}</strong>
                    </div>

                    <div className="checkout-pedido-dato">
                        <span>Fecha</span>
                        <strong>{pedido.fecha}</strong>
                    </div>

                    <div className="checkout-resumen">
                        <h2>Resumen de compra</h2>

                        {pedido.detalles.map((detalle, index) => (
                            <div
                                className="checkout-linea"
                                key={`${detalle.producto}-${index}`}
                            >
                                <div>
                                    <strong>{detalle.producto}</strong>
                                    <span>
                                        {detalle.cantidad} ×{' '}
                                        {formatearPrecio(
                                            detalle.precioUnitario,
                                        )}
                                    </span>
                                </div>

                                <strong>
                                    {formatearPrecio(detalle.subtotal)}
                                </strong>
                            </div>
                        ))}

                        <div className="checkout-total">
                            <span>Total confirmado</span>
                            <strong>
                                {formatearPrecio(pedido.total)}
                            </strong>
                        </div>
                    </div>

                    <button
                        type="button"
                        className="checkout-boton-principal"
                        onClick={onVolver}
                    >
                        Volver al catálogo
                    </button>
                </section>
            </main>
        );
    }

    return (
        <main className="checkout-page">
            <button
                type="button"
                className="checkout-volver"
                onClick={onVolver}
                disabled={procesando}
            >
                ← Volver
            </button>

            <section className="checkout-contenido">
                <div className="checkout-principal">
                    <span className="checkout-etiqueta">
                        PEDIDOS360
                    </span>

                    <h1>Finalizar compra</h1>

                    <p className="checkout-descripcion">
                        Revisa tus productos antes de confirmar el pedido.
                    </p>

                    {!account && (
                        <div className="checkout-aviso">
                            <strong>Inicia sesión para continuar</strong>
                            <p>
                                Necesitamos identificar tu cuenta para
                                asociar el pedido a tu perfil.
                            </p>

                            <button
                                type="button"
                                className="checkout-boton-secundario"
                                onClick={onLogin}
                            >
                                Iniciar sesión
                            </button>
                        </div>
                    )}

                    {items.length === 0 ? (
                        <div className="checkout-vacio">
                            <h2>Tu carrito está vacío</h2>
                            <p>
                                Agrega productos al carrito para comenzar
                                una compra.
                            </p>

                            <button
                                type="button"
                                className="checkout-boton-secundario"
                                onClick={onVolver}
                            >
                                Explorar catálogo
                            </button>
                        </div>
                    ) : (
                        <div className="checkout-productos">
                            {items.map(({ producto, cantidad }) => (
                                <article
                                    className="checkout-producto"
                                    key={producto.sku}
                                >
                                    <div className="checkout-producto-imagen">
                                        {producto.imagenes?.[0] ? (
                                            <img
                                                src={producto.imagenes[0]}
                                                alt={producto.nombre}
                                            />
                                        ) : (
                                            <span>AIRSOFT</span>
                                        )}
                                    </div>

                                    <div className="checkout-producto-info">
                                        <span>{producto.sku}</span>
                                        <h2>{producto.nombre}</h2>
                                        <p>
                                            Cantidad: {cantidad}
                                        </p>
                                        <strong>
                                            {formatearPrecio(
                                                producto.precio * cantidad,
                                            )}
                                        </strong>
                                    </div>
                                </article>
                            ))}
                        </div>
                    )}
                </div>

                {items.length > 0 && (
                    <aside className="checkout-resumen-final">
                        <h2>Tu pedido</h2>

                        <div className="checkout-pedido-dato">
                            <span>Productos</span>
                            <strong>
                                {items.reduce(
                                    (suma, item) => suma + item.cantidad,
                                    0,
                                )}
                            </strong>
                        </div>

                        <div className="checkout-pedido-dato">
                            <span>Subtotal referencial</span>
                            <strong>
                                {formatearPrecio(totalReferencial)}
                            </strong>
                        </div>

                        <p className="checkout-nota">
                            El precio y el stock definitivos se validan
                            en el servidor al confirmar la compra.
                        </p>

                        {error && (
                            <div className="checkout-error" role="alert">
                                <strong>No se pudo completar la compra</strong>
                                <p>{error}</p>
                            </div>
                        )}

                        <button
                            type="button"
                            className="checkout-boton-principal"
                            onClick={confirmarCompra}
                            disabled={
                                procesando ||
                                !account ||
                                items.length === 0
                            }
                        >
                            {procesando
                                ? 'Procesando pedido...'
                                : 'Confirmar compra'}
                        </button>

                        <p className="checkout-nota">
                            Esta versión registra el pedido, pero no
                            procesa pagos reales.
                        </p>
                    </aside>
                )}
            </section>
        </main>
    );
}
