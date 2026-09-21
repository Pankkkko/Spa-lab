import { useEffect, useState } from 'react';
import { useMsal } from '@azure/msal-react';

import {
    obtenerClientes,
    obtenerTodosLosPedidos,
} from '../api/adminApi';

import type {
    Cliente,
    Pedido,
} from '../api/adminApi';

import '../styles/admin.css';

interface AdminPageProps {
    onLogout: () => void;
}

function AdminPage({ onLogout }: AdminPageProps) {
    const { instance, accounts } = useMsal();

    const [clientes, setClientes] = useState<Cliente[]>([]);
    const [pedidos, setPedidos] = useState<Pedido[]>([]);

    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const account =
            instance.getActiveAccount() ?? accounts[0];

        if (!account) {
            setError('No se encontró una cuenta activa.');
            setCargando(false);
            return;
        }

        async function cargarDatos() {
            setCargando(true);
            setError('');

            try {
                const [clientesResultado, pedidosResultado] =
                    await Promise.all([
                        obtenerClientes(instance, account),
                        obtenerTodosLosPedidos(instance, account),
                    ]);

                setClientes(clientesResultado);
                setPedidos(pedidosResultado);
            } catch (error) {
                console.error(
                    'Error al cargar información administrativa:',
                    error,
                );

                setError(
                    error instanceof Error
                        ? error.message
                        : 'No se pudieron cargar los datos.',
                );
            } finally {
                setCargando(false);
            }
        }

        void cargarDatos();
    }, [instance, accounts]);

    function obtenerNombreCliente(clienteId: number) {
        const cliente = clientes.find(
            (item) => item.id === clienteId,
        );

        return cliente?.nombre ?? 'Cliente no encontrado';
    }

    function obtenerCantidadPedidos(clienteId: number) {
        return pedidos.filter(
            (pedido) => pedido.clienteId === clienteId,
        ).length;
    }

    return (
        <main className="admin-page">
            <header className="admin-header">
                <div>
                    <span className="admin-label">
                        PEDIDOS360
                    </span>

                    <h1>
                        Panel de Administración
                    </h1>

                    <p>
                        Gestión de clientes y pedidos.
                    </p>
                </div>

                <button
                    className="admin-logout-button"
                    onClick={onLogout}
                    type="button"
                >
                    Cerrar sesión
                </button>
            </header>

            {cargando && (
                <section className="admin-message">
                    <p>
                        Cargando información...
                    </p>
                </section>
            )}

            {error && (
                <section className="admin-error">
                    <strong>
                        No se pudo cargar el panel.
                    </strong>

                    <p>
                        {error}
                    </p>
                </section>
            )}

            {!cargando && !error && (
                <>
                    <section className="admin-summary">
                        <article className="summary-card">
                            <span>
                                Clientes
                            </span>

                            <strong>
                                {clientes.length}
                            </strong>
                        </article>

                        <article className="summary-card">
                            <span>
                                Pedidos
                            </span>

                            <strong>
                                {pedidos.length}
                            </strong>
                        </article>

                        <article className="summary-card">
                            <span>
                                Pedidos pendientes
                            </span>

                            <strong>
                                {
                                    pedidos.filter(
                                        (pedido) =>
                                            pedido.estado.toUpperCase() ===
                                            'PENDIENTE',
                                    ).length
                                }
                            </strong>
                        </article>
                    </section>

                    <section className="admin-section">
                        <div className="section-heading">
                            <div>
                                <span>
                                    Gestión
                                </span>

                                <h2>
                                    Clientes registrados
                                </h2>
                            </div>
                        </div>

                        {clientes.length === 0 ? (
                            <p className="empty-message">
                                No hay clientes registrados.
                            </p>
                        ) : (
                            <div className="clients-table-wrapper">
                                <table className="admin-table">
                                    <thead>
                                        <tr>
                                            <th>
                                                ID
                                            </th>

                                            <th>
                                                Nombre
                                            </th>

                                            <th>
                                                Email
                                            </th>

                                            <th>
                                                Teléfono
                                            </th>

                                            <th>
                                                Pedidos
                                            </th>
                                        </tr>
                                    </thead>

                                    <tbody>
                                        {clientes.map((cliente) => (
                                            <tr key={cliente.id}>
                                                <td>
                                                    #{cliente.id}
                                                </td>

                                                <td className="table-primary">
                                                    {cliente.nombre}
                                                </td>

                                                <td>
                                                    {cliente.email}
                                                </td>

                                                <td>
                                                    {cliente.telefono}
                                                </td>

                                                <td>
                                                    {
                                                        obtenerCantidadPedidos(
                                                            cliente.id,
                                                        )
                                                    }
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </section>

                    <section className="admin-section">
                        <div className="section-heading">
                            <div>
                                <span>
                                    Gestión
                                </span>

                                <h2>
                                    Todos los pedidos
                                </h2>
                            </div>
                        </div>

                        {pedidos.length === 0 ? (
                            <p className="empty-message">
                                No hay pedidos registrados.
                            </p>
                        ) : (
                            <div className="orders-admin-list">
                                {pedidos.map((pedido) => (
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
                                                    {
                                                        obtenerNombreCliente(
                                                            pedido.clienteId,
                                                        )
                                                    }
                                                </h3>

                                                <p>
                                                    Cliente ID:{' '}
                                                    {pedido.clienteId}
                                                </p>
                                            </div>

                                            <span
                                                className={`admin-order-status status-${pedido.estado.toLowerCase()}`}
                                            >
                                                {pedido.estado}
                                            </span>
                                        </div>

                                        <div className="admin-order-info">
                                            <div>
                                                <span>
                                                    Fecha
                                                </span>

                                                <strong>
                                                    {pedido.fecha}
                                                </strong>
                                            </div>

                                            <div>
                                                <span>
                                                    Productos
                                                </span>

                                                <strong>
                                                    {pedido.detalles.reduce(
                                                        (
                                                            total,
                                                            detalle,
                                                        ) =>
                                                            total +
                                                            detalle.cantidad,
                                                        0,
                                                    )}
                                                </strong>
                                            </div>

                                            <div>
                                                <span>
                                                    Total
                                                </span>

                                                <strong>
                                                    $
                                                    {pedido.total.toLocaleString(
                                                        'es-CL',
                                                    )}
                                                </strong>
                                            </div>
                                        </div>

                                        <div className="admin-order-details">
                                            {pedido.detalles.map(
                                                (
                                                    detalle,
                                                    index,
                                                ) => (
                                                    <div
                                                        className="admin-order-detail"
                                                        key={`${pedido.id}-${index}`}
                                                    >
                                                        <span>
                                                            {
                                                                detalle.producto
                                                            }{' '}
                                                            ×{' '}
                                                            {
                                                                detalle.cantidad
                                                            }
                                                        </span>

                                                        <span>
                                                            $
                                                            {detalle.subtotal.toLocaleString(
                                                                'es-CL',
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
                    </section>
                </>
            )}

            <footer className="admin-footer">
                <span>
                    Pedidos360
                </span>

                <span>
                    •
                </span>

                <span>
                    Panel de Administración
                </span>
            </footer>
        </main>
    );
}

export default AdminPage;

