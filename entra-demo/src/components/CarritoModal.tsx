
import { useEffect } from 'react';
import type { CarritoItem } from '../hooks/useCarrito';
import '../styles/carrito.css';

interface CarritoModalProps {
    abierto: boolean;
    items: CarritoItem[];
    onCerrar: () => void;
    onCambiarCantidad: (sku: string, cantidad: number) => void;
    onEliminar: (sku: string) => void;
    onVaciar: () => void;
    onCheckout: () => void;
}

function formatearPrecio(precio: number): string {
    return new Intl.NumberFormat('es-CL', {
        style: 'currency',
        currency: 'CLP',
        maximumFractionDigits: 0,
    }).format(precio);
}

export default function CarritoModal({
    abierto,
    items,
    onCerrar,
    onCambiarCantidad,
    onEliminar,
    onVaciar,
    onCheckout,
}: CarritoModalProps) {
    useEffect(() => {
        if (!abierto) {
            return;
        }

        const manejarEscape = (evento: KeyboardEvent) => {
            if (evento.key === 'Escape') {
                onCerrar();
            }
        };

        window.addEventListener('keydown', manejarEscape);

        return () => {
            window.removeEventListener('keydown', manejarEscape);
        };
    }, [abierto, onCerrar]);

    if (!abierto) {
        return null;
    }

    const total = items.reduce(
        (suma, item) => suma + item.producto.precio * item.cantidad,
        0,
    );

    return (
        <div
            className="carrito-overlay"
            onMouseDown={(evento) => {
                if (evento.target === evento.currentTarget) {
                    onCerrar();
                }
            }}
        >
            <section
                className="carrito-modal"
                role="dialog"
                aria-modal="true"
                aria-labelledby="carrito-titulo"
            >
                <header className="carrito-header">
                    <div>
                        <span className="carrito-etiqueta">
                            PEDIDOS360
                        </span>
                        <h2 id="carrito-titulo">
                            Tu carrito <span>🛒</span>
                        </h2>
                        <p>
                            {items.length === 0
                                ? 'Todavía no has agregado productos.'
                                : `${items.length} ${items.length === 1
                                    ? 'producto diferente'
                                    : 'productos diferentes'
                                }`}
                        </p>
                    </div>

                    <button
                        type="button"
                        className="carrito-cerrar"
                        onClick={onCerrar}
                        aria-label="Cerrar carrito"
                    >
                        ×
                    </button>
                </header>

                {items.length === 0 ? (
                    <div className="carrito-vacio">
                        <span className="carrito-vacio-icono">🛍️</span>
                        <h3>Tu carrito está vacío</h3>
                        <p>
                            Explora el catálogo y agrega algo que te interese.
                        </p>
                        <button
                            type="button"
                            className="carrito-seguir"
                            onClick={onCerrar}
                        >
                            Seguir explorando
                        </button>
                    </div>
                ) : (
                    <>
                        <div className="carrito-lista">
                            {items.map(({ producto, cantidad }) => (
                                <article
                                    className="carrito-item"
                                    key={producto.sku}
                                >
                                    <div className="carrito-item-imagen">
                                        {producto.imagenes?.[0] ? (
                                            <img
                                                src={producto.imagenes[0]}
                                                alt={producto.nombre}
                                            />
                                        ) : (
                                            <span>AIRSOFT</span>
                                        )}
                                    </div>

                                    <div className="carrito-item-info">
                                        <span className="carrito-item-sku">
                                            SKU: {producto.sku}
                                        </span>

                                        <h3>{producto.nombre}</h3>

                                        <strong className="carrito-item-precio">
                                            {formatearPrecio(producto.precio)}
                                        </strong>

                                        <span className="carrito-item-stock">
                                            Stock disponible: {producto.stock}
                                        </span>

                                        <div className="carrito-item-controles">
                                            <div
                                                className="carrito-cantidad"
                                                aria-label={`Cantidad de ${producto.nombre}`}
                                            >
                                                <button
                                                    type="button"
                                                    onClick={() =>
                                                        onCambiarCantidad(
                                                            producto.sku,
                                                            cantidad - 1,
                                                        )
                                                    }
                                                    disabled={cantidad <= 1}
                                                    aria-label="Disminuir cantidad"
                                                >
                                                    −
                                                </button>

                                                <span aria-live="polite">
                                                    {cantidad}
                                                </span>

                                                <button
                                                    type="button"
                                                    onClick={() =>
                                                        onCambiarCantidad(
                                                            producto.sku,
                                                            cantidad + 1,
                                                        )
                                                    }
                                                    disabled={
                                                        cantidad >= producto.stock
                                                    }
                                                    aria-label="Aumentar cantidad"
                                                >
                                                    +
                                                </button>
                                            </div>

                                            <button
                                                type="button"
                                                className="carrito-eliminar"
                                                onClick={() =>
                                                    onEliminar(producto.sku)
                                                }
                                            >
                                                Eliminar
                                            </button>
                                        </div>
                                    </div>

                                    <strong className="carrito-subtotal">
                                        {formatearPrecio(
                                            producto.precio * cantidad,
                                        )}
                                    </strong>
                                </article>
                            ))}
                        </div>

                        <footer className="carrito-footer">
                            <button
                                type="button"
                                className="carrito-vaciar"
                                onClick={onVaciar}
                            >
                                Vaciar carrito
                            </button>

                            <div className="carrito-total">
                                <span>Total</span>
                                <strong>{formatearPrecio(total)}</strong>
                            </div>

                            <p className="carrito-aviso">
                                El precio es referencial. El servidor validará los precios
                                y el stock al confirmar tu compra.
                            </p>

                            <button
                                type="button"
                                className="carrito-comprar"
                                disabled={items.length === 0}
                                onClick={onCheckout}
                            >
                                Continuar al checkout
                            </button>
                        </footer>
                    </>
                )}
            </section>
        </div>
    );
}
