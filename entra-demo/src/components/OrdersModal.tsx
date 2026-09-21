import type { Pedido } from "../api/pedidosApi";

interface OrdersModalProps {
    isOpen: boolean;
    onClose: () => void;
    pedidos: Pedido[];
    cargando: boolean;
    error: string;
}

function OrdersModal({
    isOpen,
    onClose,
    pedidos,
    cargando,
    error,
}: OrdersModalProps) {
    if (!isOpen) {
        return null;
    }

    return (
        <div
            className="orders-modal-overlay"
            onClick={onClose}
        >
            <section
                className="orders-modal"
                role="dialog"
                aria-modal="true"
                aria-labelledby="orders-modal-title"
                onClick={(event) =>
                    event.stopPropagation()
                }
            >
                <header className="orders-modal-header">
                    <div>
                        <span className="orders-modal-label">
                            PEDIDOS360
                        </span>

                        <h2 id="orders-modal-title">
                            Mis pedidos
                        </h2>
                    </div>

                    <button
                        className="orders-modal-close"
                        onClick={onClose}
                        type="button"
                        aria-label="Cerrar"
                    >
                        ×
                    </button>
                </header>

                <div className="orders-modal-content">

                    {cargando && (
                        <p className="orders-message">
                            Cargando pedidos...
                        </p>
                    )}

                    {error && (
                        <p className="orders-error">
                            {error}
                        </p>
                    )}

                    {!cargando &&
                        !error &&
                        pedidos.length === 0 && (
                            <p className="orders-message">
                                No tienes pedidos registrados.
                            </p>
                        )}

                    {!cargando &&
                        !error &&
                        pedidos.length > 0 && (
                            <div className="orders-list">
                                {pedidos.map((pedido) => (
                                    <article
                                        className="order-card"
                                        key={pedido.id}
                                    >
                                        <div className="order-card-header">
                                            <div>
                                                <span className="order-label">
                                                    Pedido
                                                </span>

                                                <h3>
                                                    #{pedido.id}
                                                </h3>
                                            </div>

                                            <span
                                                className={`order-status status-${pedido.estado.toLowerCase()}`}
                                            >
                                                {pedido.estado}
                                            </span>
                                        </div>

                                        <div className="order-info">
                                            <span>
                                                Fecha:{" "}
                                                {pedido.fecha}
                                            </span>

                                            <strong>
                                                $
                                                {pedido.total.toLocaleString(
                                                    "es-CL",
                                                )}
                                            </strong>
                                        </div>

                                        <div className="order-details">
                                            {pedido.detalles.map(
                                                (
                                                    detalle,
                                                    index,
                                                ) => (
                                                    <div
                                                        className="order-detail"
                                                        key={`${pedido.id}-${index}`}
                                                    >
                                                        <span>
                                                            {
                                                                detalle.producto
                                                            }
                                                            {" x"}
                                                            {
                                                                detalle.cantidad
                                                            }
                                                        </span>

                                                        <span>
                                                            $
                                                            {detalle.subtotal.toLocaleString(
                                                                "es-CL",
                                                            )}
                                                        </span>
                                                    </div>
                                                ),
                                            )}
                                        </div>
                                    </article>
                                ))}
                            </div>
                        )}
                </div>

                <footer className="orders-modal-footer">
                    <button
                        className="orders-modal-footer-button"
                        onClick={onClose}
                        type="button"
                    >
                        Cerrar
                    </button>
                </footer>
            </section>
        </div>
    );
}

export default OrdersModal;

