import type { KeyboardEvent } from 'react';
import type { Producto } from '../api/catalogoApi';

interface ProductoCardProps {
    producto: Producto;
    onClick: (producto: Producto) => void;
}

export default function ProductoCard({
    producto,
    onClick,
}: ProductoCardProps) {
    const formatearPrecio = (precio: number) =>
        new Intl.NumberFormat('es-CL', {
            style: 'currency',
            currency: 'CLP',
            maximumFractionDigits: 0,
        }).format(precio);

    function manejarTeclado(event: KeyboardEvent<HTMLElement>) {
        if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            onClick(producto);
        }
    }

    return (
        <article
            className="producto-card"
            role="button"
            tabIndex={0}
            aria-label={`Ver detalles de ${producto.nombre}`}
            onClick={() => onClick(producto)}
            onKeyDown={manejarTeclado}
        >
            <div className="producto-imagen">
                {producto.imagenes?.length > 0 ? (
                    <img
                        src={producto.imagenes[0]}
                        alt={producto.nombre}
                    />
                ) : (
                    <div className="imagen-placeholder">
                        AIRSOFT
                    </div>
                )}

                <span className="producto-categoria">
                    {producto.categoria}
                </span>
            </div>

            <div className="producto-info">
                <span className="producto-sku">
                    {producto.sku}
                </span>

                <h3>{producto.nombre}</h3>

                <p className="producto-descripcion">
                    {producto.descripcion}
                </p>

                <div className="producto-footer">
                    <div>
                        <span className="precio-label">
                            Precio
                        </span>

                        <strong className="producto-precio">
                            {formatearPrecio(producto.precio)}
                        </strong>
                    </div>

                    <div
                        className={
                            producto.stock > 0
                                ? 'stock disponible'
                                : 'stock agotado'
                        }
                    >
                        <span className="stock-dot" />

                        {producto.stock > 0
                            ? `${producto.stock} disponibles`
                            : 'Agotado'}
                    </div>
                </div>

                <div className="producto-ver-detalle">
                    Ver producto →
                </div>
            </div>
        </article>
    );
}
