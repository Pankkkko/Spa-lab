import { useEffect, useState } from 'react';
import { useMsal } from '@azure/msal-react';

import {
    obtenerClientes,
    obtenerTodosLosPedidos,
} from '../../api/adminApi';

import { obtenerProductos } from '../../api/catalogoApi';

interface EstadisticasAdmin {
    totalClientes: number;
    totalProductos: number;
    totalPedidos: number;
    pedidosPendientes: number;
}

const ESTADISTICAS_INICIALES: EstadisticasAdmin = {
    totalClientes: 0,
    totalProductos: 0,
    totalPedidos: 0,
    pedidosPendientes: 0,
};

function AdminPrincipal() {
    const { instance, accounts } = useMsal();

    const [estadisticas, setEstadisticas] = useState<EstadisticasAdmin>(
        ESTADISTICAS_INICIALES,
    );
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        let componenteActivo = true;

        async function cargarEstadisticas() {
            const account = instance.getActiveAccount() ?? accounts[0];

            if (!account) {
                if (componenteActivo) {
                    setError('No se encontró una cuenta activa.');
                    setCargando(false);
                }
                return;
            }

            try {
                setCargando(true);
                setError('');

                const tamanoPagina = 20;

                const [
                    clientesResultado,
                    pedidosResultado,
                    primeraPaginaProductos,
                ] = await Promise.all([
                    obtenerClientes(instance, account),
                    obtenerTodosLosPedidos(instance, account),
                    obtenerProductos(instance, account, 0, tamanoPagina),
                ]);

                let totalProductos = primeraPaginaProductos.content.length;

                for (
                    let pagina = 1;
                    pagina < primeraPaginaProductos.totalPages;
                    pagina++
                ) {
                    const respuesta = await obtenerProductos(
                        instance,
                        account,
                        pagina,
                        tamanoPagina,
                    );

                    totalProductos += respuesta.content.length;
                }

                if (!componenteActivo) {
                    return;
                }

                setEstadisticas({
                    totalClientes: clientesResultado.length,
                    totalProductos,
                    totalPedidos: pedidosResultado.length,
                    pedidosPendientes: pedidosResultado.filter(
                        (pedido) => pedido.estado.toUpperCase() === 'CREADO',
                    ).length,
                });
            } catch (err) {
                console.error(
                    'Error al cargar las estadísticas administrativas:',
                    err,
                );

                if (componenteActivo) {
                    setError(
                        err instanceof Error
                            ? err.message
                            : 'No se pudieron cargar las estadísticas.',
                    );
                }
            } finally {
                if (componenteActivo) {
                    setCargando(false);
                }
            }
        }

        void cargarEstadisticas();

        return () => {
            componenteActivo = false;
        };
    }, [instance, accounts]);

    if (cargando) {
        return (
            <section className="admin-message">
                <p>Cargando estadísticas...</p>
            </section>
        );
    }

    if (error) {
        return (
            <section className="admin-error" role="alert">
                <strong>No se pudieron cargar las estadísticas.</strong>
                <p>{error}</p>
            </section>
        );
    }

    return (
        <section className="admin-summary">
            <article className="summary-card">
                <span>Clientes</span>
                <strong>{estadisticas.totalClientes}</strong>
            </article>

            <article className="summary-card">
                <span>Productos</span>
                <strong>{estadisticas.totalProductos}</strong>
            </article>

            <article className="summary-card">
                <span>Pedidos</span>
                <strong>{estadisticas.totalPedidos}</strong>
            </article>

            <article className="summary-card">
                <span>Pedidos pendientes</span>
                <strong>{estadisticas.pedidosPendientes}</strong>
            </article>
        </section>
    );
}

export default AdminPrincipal;
