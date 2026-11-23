
import { useEffect, useState } from 'react';
import { useMsal } from '@azure/msal-react';

import {
    cambiarEstadoPedido,
    ESTADOS_PEDIDO,
    obtenerClientes,
    obtenerTodosLosPedidos,
} from '../../api/adminApi';

import type {
    Cliente,
    Pedido,
    EstadoPedido,
} from '../../api/adminApi';

import '../../styles/admin.css';

// Transiciones permitidas por las reglas del microservicio de Pedidos.
const TRANSICIONES_ESTADO: Record<EstadoPedido, readonly EstadoPedido[]> = {
    CREADO: ['ACEPTADO', 'CANCELADO'],
    ACEPTADO: ['EN_PREPARACION'],
    EN_PREPARACION: ['DESPACHADO'],
    DESPACHADO: ['ENTREGADO'],
    ENTREGADO: [],
    CANCELADO: [],
};

function obtenerEstadosDisponibles(estadoActual: EstadoPedido): EstadoPedido[] {
    const permitidos = TRANSICIONES_ESTADO[estadoActual] ?? [];

    return ESTADOS_PEDIDO.filter(
        (estado) => estado === estadoActual || permitidos.includes(estado),
    );
}

function AdminPedidos() {
    const { instance, accounts } = useMsal();

    const [clientes, setClientes] = useState<Cliente[]>([]);
    const [pedidos, setPedidos] = useState<Pedido[]>([]);

    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState('');
    const [mensaje, setMensaje] = useState('');

    const [pedidoEnActualizacion, setPedidoEnActualizacion] =
        useState<number | null>(null);

    async function cargarPedidos() {
        const account = instance.getActiveAccount() ?? accounts[0];

        if (!account) {
            setError('No se encontró una cuenta activa.');
            setCargando(false);
            return;
        }

        setCargando(true);
        setError('');

        try {
            const [clientesResultado, pedidosResultado] = await Promise.all([
                obtenerClientes(instance, account),
                obtenerTodosLosPedidos(instance, account),
            ]);

            setClientes(clientesResultado);
            setPedidos(pedidosResultado);
        } catch (err) {
            console.error('Error al cargar los pedidos:', err);

            setError(
                err instanceof Error
                    ? err.message
                    : 'No se pudieron cargar los pedidos.',
            );
        } finally {
            setCargando(false);
        }
    }

    useEffect(() => {
        const timeoutId = window.setTimeout(() => {
            void cargarPedidos();
        }, 0);

        return () => window.clearTimeout(timeoutId);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [instance, accounts]);

    function obtenerNombreCliente(clienteId: number): string {
        const cliente = clientes.find((item) => item.id === clienteId);

        return cliente?.nombre ?? 'Cliente no encontrado';
    }

    async function actualizarEstado(
        pedidoId: number,
        nuevoEstado: EstadoPedido,
    ) {
        const pedidoActual = pedidos.find(
            (pedido) => pedido.id === pedidoId,
        );

        if (!pedidoActual) {
            setError(`No se encontró el pedido #${pedidoId}.`);
            return;
        }

        const estadosPermitidos =
            TRANSICIONES_ESTADO[pedidoActual.estado] ?? [];

        if (!estadosPermitidos.includes(nuevoEstado)) {
            setError(
                `No se permite cambiar el pedido #${pedidoId} de ${pedidoActual.estado} a ${nuevoEstado}.`,
            );
            return;
        }

        const account = instance.getActiveAccount() ?? accounts[0];

        if (!account) {
            setError('No se encontró una cuenta activa.');
            return;
        }

        setPedidoEnActualizacion(pedidoId);
        setError('');
        setMensaje('');

        try {
            const pedidoActualizado = await cambiarEstadoPedido(
                instance,
                account,
                pedidoId,
                nuevoEstado,
            );

            setPedidos((actuales) =>
                actuales.map((pedido) =>
                    pedido.id === pedidoId ? pedidoActualizado : pedido,
                ),
            );

            setMensaje(
                `Pedido #${pedidoId} actualizado a ${nuevoEstado}.`,
            );
        } catch (err) {
            console.error('Error al actualizar el pedido:', err);

            setError(
                err instanceof Error
                    ? err.message
                    : 'No se pudo actualizar el estado del pedido.',
            );
        } finally {
            setPedidoEnActualizacion(null);
        }
    }

    const pedidosPendientes = pedidos.filter(
        (pedido) => pedido.estado === 'CREADO',
    ).length;

    const pedidosEnProceso = pedidos.filter(
        (pedido) =>
            pedido.estado === 'ACEPTADO' ||
            pedido.estado === 'EN_PREPARACION' ||
            pedido.estado === 'DESPACHADO',
    ).length;

    const pedidosFinalizados = pedidos.filter(
        (pedido) =>
            pedido.estado === 'ENTREGADO' ||
            pedido.estado === 'CANCELADO',
    ).length;

    if (cargando) {
        return (
            <section className="admin-message">
                <p>Cargando pedidos...</p>
            </section>
        );
    }

    return (
        <section className="admin-section">
            <div className="section-heading">
                <div>
                    <span>Gestión</span>
                    <h2>Todos los pedidos</h2>
                    <p>
                        Administra los pedidos y actualiza su estado de
                        procesamiento.
                    </p>
                </div>
            </div>

            {error && (
                <section className="admin-error" role="alert">
                    <strong>No se pudo completar la operación.</strong>
                    <p>{error}</p>

                    <button
                        className="admin-secondary-button"
                        type="button"
                        onClick={() => setError('')}
                    >
                        Cerrar mensaje
                    </button>
                </section>
            )}

            {mensaje && !error && (
                <section className="admin-success" role="status">
                    {mensaje}

                    <button
                        type="button"
                        onClick={() => setMensaje('')}
                        aria-label="Cerrar mensaje"
                    >
                        ×
                    </button>
                </section>
            )}

            <div className="admin-summary">
                <article className="summary-card">
                    <span>Total de pedidos</span>
                    <strong>{pedidos.length}</strong>
                </article>

                <article className="summary-card">
                    <span>Pendientes</span>
                    <strong>{pedidosPendientes}</strong>
                </article>

                <article className="summary-card">
                    <span>En proceso</span>
                    <strong>{pedidosEnProceso}</strong>
                </article>

                <article className="summary-card">
                    <span>Finalizados</span>
                    <strong>{pedidosFinalizados}</strong>
                </article>
            </div>

            {pedidos.length === 0 ? (
                <p className="empty-message">
                    No hay pedidos registrados.
                </p>
            ) : (
                <div className="orders-admin-list">
                    {pedidos.map((pedido) => {
                        const estadosDisponibles =
                            obtenerEstadosDisponibles(pedido.estado);

                        const estaActualizando =
                            pedidoEnActualizacion === pedido.id;

                        const esEstadoFinal =
                            (TRANSICIONES_ESTADO[pedido.estado] ?? []).length === 0;

                        const cantidadProductos = pedido.detalles.reduce(
                            (total, detalle) => total + detalle.cantidad,
                            0,
                        );

                        return (
                            <article
                                className="admin-order-card"
                                key={pedido.id}
                            >
                                <div className="admin-order-main">
                                    <div>
                                        <span className="order-id">
                                            Pedido #{pedido.id}
                                        </span>

                                        <h3>
                                            {obtenerNombreCliente(
                                                pedido.clienteId,
                                            )}
                                        </h3>

                                        <p>
                                            Cliente ID: {pedido.clienteId}
                                        </p>
                                    </div>

                                    <label className="admin-order-status-control">
                                        <span className="sr-only">
                                            Estado del pedido #{pedido.id}
                                        </span>

                                        <select
                                            className={`admin-order-status status-${pedido.estado.toLowerCase()}`}
                                            value={pedido.estado}
                                            disabled={
                                                estaActualizando ||
                                                esEstadoFinal
                                            }
                                            onChange={(event) =>
                                                void actualizarEstado(
                                                    pedido.id,
                                                    event.target.value as EstadoPedido,
                                                )
                                            }
                                        >
                                            {estadosDisponibles.map((estado) => (
                                                <option
                                                    key={estado}
                                                    value={estado}
                                                >
                                                    {estado}
                                                </option>
                                            ))}
                                        </select>
                                    </label>
                                </div>

                                <div className="admin-order-info">
                                    <div>
                                        <span>Fecha</span>
                                        <strong>{pedido.fecha}</strong>
                                    </div>

                                    <div>
                                        <span>Productos</span>
                                        <strong>{cantidadProductos}</strong>
                                    </div>

                                    <div>
                                        <span>Total</span>
                                        <strong>
                                            $
                                            {pedido.total.toLocaleString(
                                                'es-CL',
                                            )}
                                        </strong>
                                    </div>
                                </div>

                                <div className="admin-order-details">
                                    {pedido.detalles.map((detalle, index) => (
                                        <div
                                            className="admin-order-detail"
                                            key={`${pedido.id}-${index}`}
                                        >
                                            <span>
                                                {detalle.producto} ×{' '}
                                                {detalle.cantidad}
                                            </span>

                                            <span>
                                                $
                                                {detalle.subtotal.toLocaleString(
                                                    'es-CL',
                                                )}
                                            </span>
                                        </div>
                                    ))}
                                </div>
                            </article>
                        );
                    })}
                </div>
            )}
        </section>
    );
}

export default AdminPedidos;
