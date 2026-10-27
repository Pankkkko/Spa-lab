
import type { Producto } from '../api/catalogoApi';
import { useEffect, useState } from 'react';

interface ProductoDetalleProps {
    producto: Producto;
    onVolver: () => void;
    onAgregarAlCarrito: (producto: Producto) => void;
    onComprarAhora: (producto: Producto) => void;
}

export default function ProductoDetalle({
    producto,
    onVolver,
    onAgregarAlCarrito,
    onComprarAhora,
}: ProductoDetalleProps) {
    const [imagenSeleccionada, setImagenSeleccionada] = useState(0);

    const imagenes = (producto.imagenes ?? []).filter(
        (imagen) => typeof imagen === 'string' && imagen.trim() !== ''
    );

    useEffect(() => {
        setImagenSeleccionada(0);
    }, [producto.sku]);

    const formatearPrecio = (precio: number) =>
        new Intl.NumberFormat('es-CL', {
            style: 'currency',
            currency: 'CLP',
            maximumFractionDigits: 0,
        }).format(precio);

    const agotado = producto.stock <= 0;

    const imagenAnterior = () => {
        setImagenSeleccionada((actual) =>
            (actual - 1 + imagenes.length) % imagenes.length
        );
    };

    const imagenSiguiente = () => {
        setImagenSeleccionada((actual) =>
            (actual + 1) % imagenes.length
        );
    };

    return (
        <main className="producto-detalle">
            <button
                type="button"
                className="detalle-volver"
                onClick={onVolver}
            >
                ← Volver al catálogo
            </button>

            <section className="detalle-contenido">
                <div className="detalle-imagen">
                    <div className="detalle-galeria">
                        <div className="detalle-imagen-principal">
                            {imagenes.length > 0 ? (
                                <img
                                    src={imagenes[imagenSeleccionada]}
                                    alt={`${producto.nombre}, imagen ${imagenSeleccionada + 1}`}
                                />
                            ) : (
                                <div className="detalle-imagen-placeholder">
                                    AIRSOFT
                                </div>
                            )}

                            {imagenes.length > 1 && (
                                <>
                                    <button
                                        type="button"
                                        className="detalle-flecha detalle-flecha-izquierda"
                                        onClick={imagenAnterior}
                                        aria-label="Imagen anterior"
                                    >
                                        ‹
                                    </button>

                                    <button
                                        type="button"
                                        className="detalle-flecha detalle-flecha-derecha"
                                        onClick={imagenSiguiente}
                                        aria-label="Imagen siguiente"
                                    >
                                        ›
                                    </button>
                                </>
                            )}
                        </div>

                        {imagenes.length > 1 && (
                            <div className="detalle-miniaturas">
                                {imagenes.map((imagen, indice) => (
                                    <button
                                        key={`${producto.sku}-${indice}`}
                                        type="button"
                                        className={`detalle-miniatura ${
                                            indice === imagenSeleccionada
                                                ? 'activa'
                                                : ''
                                        }`}
                                        onClick={() =>
                                            setImagenSeleccionada(indice)
                                        }
                                        aria-label={`Ver imagen ${indice + 1}`}
                                        aria-pressed={
                                            indice === imagenSeleccionada
                                        }
                                    >
                                        <img
                                            src={imagen}
                                            alt=""
                                            loading="lazy"
                                        />
                                    </button>
                                ))}
                            </div>
                        )}
                    </div>

                    
                </div>

                <div className="detalle-informacion">
                    <span className="detalle-etiqueta">
                        DETALLE DEL PRODUCTO
                    </span>

                    <span className="detalle-sku">
                        SKU: {producto.sku}
                    </span>

                    <h1>{producto.nombre}</h1>

                    <p className="detalle-descripcion">
                        {producto.descripcion}
                    </p>

                    <div className="detalle-separador" />

                    <span className="detalle-precio-label">
                        Precio
                    </span>

                    <strong className="detalle-precio">
                        {formatearPrecio(producto.precio)}
                    </strong>

                    <div
                        className={
                            agotado
                                ? 'detalle-stock agotado'
                                : 'detalle-stock disponible'
                        }
                    >
                        <span className="stock-dot" />

                        {agotado
                            ? 'Producto agotado'
                            : `${producto.stock} unidades disponibles`}
                    </div>

                    <div className="detalle-acciones">
                        <button
                            type="button"
                            className="btn-comprar"
                            disabled={agotado}
                            onClick={() => onComprarAhora(producto)}
                        >
                            <span aria-hidden="true">🛒</span>
                            {agotado
                                ? 'Producto agotado'
                                : 'Comprar ahora'}
                        </button>

                        <button
                            type="button"
                            className="btn-carrito"
                            disabled={agotado}
                            onClick={() => onAgregarAlCarrito(producto)}
                        >
                            <span aria-hidden="true">🛒</span>
                            {agotado
                                ? 'Producto agotado'
                                : 'Agregar al carrito'}
                        </button>
                    </div>

                    <p className="detalle-nota">
                        Puedes explorar el catálogo sin iniciar sesión.
                        Para confirmar tu compra debes iniciar sesión.
                        El pago real todavía no está habilitado.
                    </p>
                </div>
            </section>
        </main>
    );
}
