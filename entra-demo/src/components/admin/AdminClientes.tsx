
import { useEffect, useState } from 'react';
import { useMsal } from '@azure/msal-react';
import type { AccountInfo } from '@azure/msal-browser';

import {
    obtenerClientes,
    obtenerTodosLosPedidos,
} from '../../api/adminApi';

import type {
    Cliente,
    Pedido,
} from '../../api/adminApi';

import '../../styles/admin.css';

function AdminClientes() {
    const { instance, accounts } = useMsal();

    const [clientes, setClientes] = useState<Cliente[]>([]);
    const [pedidos, setPedidos] = useState<Pedido[]>([]);

    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState('');

    async function cargarClientes() {
        const account = instance.getActiveAccount() ?? accounts[0];

        if (!account) {
            setError('No se encontró una cuenta activa.');
            setCargando(false);
            return;
        }

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
        } catch (err) {
            console.error('Error al cargar los clientes:', err);

            setError(
                err instanceof Error
                    ? err.message
                    : 'No se pudieron cargar los clientes.',
            );
        } finally {
            setCargando(false);
        }
    }

    useEffect(() => {
        void cargarClientes();

        // La carga inicial depende de MSAL y sus cuentas.
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [instance, accounts]);

    function obtenerCantidadPedidos(clienteId: number): number {
        return pedidos.filter(
            (pedido) => pedido.clienteId === clienteId,
        ).length;
    }

    if (cargando) {
        return (
            <section className="admin-message">
                <p>Cargando clientes...</p>
            </section>
        );
    }

    if (error) {
        return (
            <section className="admin-error" role="alert">
                <strong>No se pudieron cargar los clientes.</strong>
                <p>{error}</p>

                <button
                    className="admin-secondary-button"
                    type="button"
                    onClick={() => void cargarClientes()}
                >
                    Reintentar
                </button>
            </section>
        );
    }

    return (
        <section className="admin-section">
            <div className="section-heading">
                <div>
                    <span>Gestión</span>
                    <h2>Clientes registrados</h2>
                    <p>
                        {clientes.length} clientes registrados en el sistema
                    </p>
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
                                <th>ID</th>
                                <th>Nombre</th>
                                <th>Email</th>
                                <th>Teléfono</th>
                                <th>Pedidos</th>
                            </tr>
                        </thead>

                        <tbody>
                            {clientes.map((cliente) => (
                                <tr key={cliente.id}>
                                    <td>#{cliente.id}</td>

                                    <td className="table-primary">
                                        {cliente.nombre}
                                    </td>

                                    <td>{cliente.email}</td>

                                    <td>{cliente.telefono}</td>

                                    <td>
                                        {obtenerCantidadPedidos(cliente.id)}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </section>
    );
}

export default AdminClientes;
