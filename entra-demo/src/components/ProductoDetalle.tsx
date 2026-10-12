
import type { Producto } from '../api/catalogoApi';

interface ProductoDetalleProps {
    producto: Producto;
    onVolver: () => void;
    onAgregarAlCarrito: (producto: Producto) => void;
}

export default function ProductoDetalle({
    producto,
    onVolver,
    onAgregarAlCarrito,
}: ProductoDetalleProps) {
    const formatearPrecio = (precio: number) =>
        new Intl.NumberFormat('es-CL', {
            style: 'currency',
            currency: 'CLP',
            maximumFractionDigits: 0,
        }).format(precio);

    const agotado = producto.stock <= 0;

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
                    {producto.imagenes?.length > 0 ? (
                        <img
                            src={producto.imagenes[0]}
                            alt={producto.nombre}
                        />
                    ) : (
                        <div className="detalle-imagen-placeholder">
                            AIRSOFT
                        </div>
                    )}

                    <span className="detalle-categoria">
                        {producto.categoria}
                    </span>
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
                            disabled
                            title="Funcionalidad próximamente"
                        >
                            Comprar
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
                        Puedes agregar productos sin iniciar sesión.
                        La compra y el pago aún no están habilitados.
                    </p>
                </div>
            </section>
        </main>
    );
}
