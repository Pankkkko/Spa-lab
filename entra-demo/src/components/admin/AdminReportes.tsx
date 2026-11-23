
import { useEffect, useState } from 'react';
import { useMsal } from '@azure/msal-react';

import {
    obtenerKpis,
    obtenerVentasPorHora,
    obtenerProductosTop,
    obtenerPedidosPorEstado,
    obtenerTiemposEntrega,
} from '../../api/reportApi';

import type {
    KpiResponse,
    VentaPorHoraResponse,
    ProductoTopResponse,
    PedidoEstadoResponse,
    TiempoEntregaResponse,
} from '../../api/reportApi';

import '../../styles/admin.css';

function formatearMoneda(valor: number): string {
    return `$${valor.toLocaleString('es-CL')}`;
}

function formatearMinutos(valor: number): string {
    return `${valor.toLocaleString('es-CL', {
        maximumFractionDigits: 1,
    })} min`;
}

function AdminReportes() {
    const { instance, accounts } = useMsal();

    const [kpis, setKpis] = useState<KpiResponse | null>(null);
    const [ventasPorHora, setVentasPorHora] = useState<
        VentaPorHoraResponse[]
    >([]);
    const [productosTop, setProductosTop] = useState<
        ProductoTopResponse[]
    >([]);
    const [pedidosPorEstado, setPedidosPorEstado] = useState<
        PedidoEstadoResponse[]
    >([]);
    const [tiemposEntrega, setTiemposEntrega] =
        useState<TiempoEntregaResponse | null>(null);

    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState('');
    const [intentoCarga, setIntentoCarga] = useState(0);

    useEffect(() => {
        let componenteActivo = true;

        async function cargarReportes() {
            const account = instance.getActiveAccount() ?? accounts[0];

            if (!account) {
                setError('No se encontró una cuenta activa.');
                setCargando(false);
                return;
            }

            try {
                setCargando(true);
                setError('');

                const [
                    kpisResultado,
                    ventasResultado,
                    productosResultado,
                    estadosResultado,
                    tiemposResultado,
                ] = await Promise.all([
                    obtenerKpis(instance, account),
                    obtenerVentasPorHora(instance, account),
                    obtenerProductosTop(instance, account),
                    obtenerPedidosPorEstado(instance, account),
                    obtenerTiemposEntrega(instance, account),
                ]);

                if (!componenteActivo) {
                    return;
                }

                setKpis(kpisResultado);
                setVentasPorHora(ventasResultado);
                setProductosTop(productosResultado);
                setPedidosPorEstado(estadosResultado);
                setTiemposEntrega(tiemposResultado);
            } catch (err) {
                console.error('Error al cargar los reportes:', err);

                if (componenteActivo) {
                    setError(
                        err instanceof Error
                            ? err.message
                            : 'No se pudieron cargar los reportes.',
                    );
                }
            } finally {
                if (componenteActivo) {
                    setCargando(false);
                }
            }
        }

        void cargarReportes();

        return () => {
            componenteActivo = false;
        };
    }, [instance, accounts, intentoCarga]);

    if (cargando) {
        return (
            <section className="admin-message">
                <p>Cargando reportes y estadísticas...</p>
            </section>
        );
    }

    if (error) {
        return (
            <section className="admin-error" role="alert">
                <strong>No se pudieron cargar los reportes.</strong>
                <p>{error}</p>

                <button
                    className="admin-secondary-button"
                    type="button"
                    onClick={() => setIntentoCarga((actual) => actual + 1)}
                >
                    Reintentar
                </button>
            </section>
        );
    }

    if (!kpis || !tiemposEntrega) {
        return (
            <section className="admin-message">
                <p>No hay información de reportes disponible.</p>
            </section>
        );
    }

    const maxVentas = Math.max(
        1,
        ...ventasPorHora.map((venta) => venta.montoTotal),
    );

    const maxProductos = Math.max(
        1,
        ...productosTop.map((producto) => producto.unidadesVendidas),
    );

    const totalPedidosEstados = pedidosPorEstado.reduce(
        (total, item) => total + item.cantidad,
        0,
    );

    return (
        <section className="admin-section">
            <div className="section-heading">
                <div>
                    <span>Analítica</span>
                    <h2>Reportes y estadísticas</h2>
                    <p>
                        Resumen del rendimiento y actividad de Pedidos360.
                    </p>
                </div>

                <button
                    className="admin-secondary-button"
                    type="button"
                    onClick={() => setIntentoCarga((actual) => actual + 1)}
                >
                    Actualizar datos
                </button>
            </div>

            {/* Indicadores principales */}
            <div className="admin-summary">
                <article className="summary-card">
                    <span>Total de pedidos</span>
                    <strong>{kpis.totalPedidos}</strong>
                </article>

                <article className="summary-card">
                    <span>Pedidos activos</span>
                    <strong>{kpis.pedidosActivos}</strong>
                </article>

                <article className="summary-card">
                    <span>Pedidos entregados</span>
                    <strong>{kpis.pedidosEntregados}</strong>
                </article>

                <article className="summary-card">
                    <span>Pedidos cancelados</span>
                    <strong>{kpis.pedidosCancelados}</strong>
                </article>

                <article className="summary-card">
                    <span>Ventas totales</span>
                    <strong>{formatearMoneda(kpis.ventasTotales)}</strong>
                </article>

                <article className="summary-card">
                    <span>Tiempo promedio de entrega</span>
                    <strong>
                        {formatearMinutos(
                            kpis.tiempoPromedioEntregaMinutos,
                        )}
                    </strong>
                </article>
            </div>

            {/* Ventas por hora */}
            <section className="admin-section">
                <div className="section-heading">
                    <div>
                        <span>Actividad comercial</span>
                        <h2>Ventas por hora</h2>
                        <p>
                            Cantidad de pedidos y monto vendido en cada hora.
                        </p>
                    </div>
                </div>

                {ventasPorHora.length === 0 ? (
                    <p className="empty-message">
                        No hay información de ventas por hora.
                    </p>
                ) : (
                    <div
                        style={{
                            display: 'grid',
                            gap: '16px',
                        }}
                    >
                        {ventasPorHora.map((venta, index) => (
                            <div
                                key={`${venta.hora}-${index}`}
                                style={{
                                    display: 'grid',
                                    gap: '6px',
                                }}
                            >
                                <div
                                    style={{
                                        display: 'flex',
                                        justifyContent: 'space-between',
                                        flexWrap: 'wrap',
                                        gap: '8px',
                                    }}
                                >
                                    <strong>{venta.hora}</strong>

                                    <span>
                                        {venta.cantidadPedidos} pedidos ·{' '}
                                        {formatearMoneda(venta.montoTotal)}
                                    </span>
                                </div>

                                <div
                                    style={{
                                        width: '100%',
                                        height: '12px',
                                        background: '#e4eeee',
                                        borderRadius: '20px',
                                        overflow: 'hidden',
                                    }}
                                >
                                    <div
                                        style={{
                                            width: `${
                                                (venta.montoTotal / maxVentas) *
                                                100
                                            }%`,
                                            height: '100%',
                                            background: '#3E9B94',
                                            borderRadius: '20px',
                                            minWidth:
                                                venta.montoTotal > 0
                                                    ? '4px'
                                                    : '0',
                                        }}
                                    />
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </section>

            {/* Productos más vendidos */}
            <section className="admin-section">
                <div className="section-heading">
                    <div>
                        <span>Rendimiento del catálogo</span>
                        <h2>Productos más vendidos</h2>
                        <p>
                            Productos ordenados según sus unidades vendidas.
                        </p>
                    </div>
                </div>

                {productosTop.length === 0 ? (
                    <p className="empty-message">
                        No hay información de productos vendidos.
                    </p>
                ) : (
                    <div className="clients-table-wrapper">
                        <table className="admin-table">
                            <thead>
                                <tr>
                                    <th>Producto</th>
                                    <th>Unidades vendidas</th>
                                    <th>Monto total</th>
                                    <th>Fecha de venta</th>
                                </tr>
                            </thead>

                            <tbody>
                                {productosTop.map((producto) => (
                                    <tr key={producto.productoKey}>
                                        <td className="table-primary">
                                            {producto.productoNombre}
                                        </td>

                                        <td>
                                            <div
                                                style={{
                                                    display: 'grid',
                                                    gap: '5px',
                                                    minWidth: '100px',
                                                }}
                                            >
                                                <strong>
                                                    {producto.unidadesVendidas}
                                                </strong>

                                                <div
                                                    style={{
                                                        height: '6px',
                                                        background: '#e4eeee',
                                                        borderRadius: '10px',
                                                        overflow: 'hidden',
                                                    }}
                                                >
                                                    <div
                                                        style={{
                                                            width: `${
                                                                (producto.unidadesVendidas /
                                                                    maxProductos) *
                                                                100
                                                            }%`,
                                                            height: '100%',
                                                            background: '#3E9B94',
                                                        }}
                                                    />
                                                </div>
                                            </div>
                                        </td>

                                        <td>
                                            {formatearMoneda(
                                                producto.montoTotal,
                                            )}
                                        </td>

                                        <td>{producto.fechaVenta}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </section>

            {/* Pedidos por estado */}
            <section className="admin-section">
                <div className="section-heading">
                    <div>
                        <span>Estado operacional</span>
                        <h2>Pedidos por estado</h2>
                        <p>
                            Distribución de pedidos según su etapa de
                            procesamiento.
                        </p>
                    </div>
                </div>

                {pedidosPorEstado.length === 0 ? (
                    <p className="empty-message">
                        No hay información de estados disponible.
                    </p>
                ) : (
                    <div
                        style={{
                            display: 'grid',
                            gap: '16px',
                        }}
                    >
                        {pedidosPorEstado.map((item) => {
                            const porcentaje =
                                totalPedidosEstados > 0
                                    ? (item.cantidad / totalPedidosEstados) *
                                      100
                                    : 0;

                            return (
                                <div
                                    key={item.estado}
                                    style={{
                                        display: 'grid',
                                        gap: '6px',
                                    }}
                                >
                                    <div
                                        style={{
                                            display: 'flex',
                                            justifyContent: 'space-between',
                                            flexWrap: 'wrap',
                                            gap: '8px',
                                        }}
                                    >
                                        <strong>{item.estado}</strong>

                                        <span>
                                            {item.cantidad} pedidos (
                                            {porcentaje.toFixed(1)}%)
                                        </span>
                                    </div>

                                    <div
                                        style={{
                                            width: '100%',
                                            height: '12px',
                                            background: '#e4eeee',
                                            borderRadius: '20px',
                                            overflow: 'hidden',
                                        }}
                                    >
                                        <div
                                            style={{
                                                width: `${porcentaje}%`,
                                                height: '100%',
                                                background: '#20666B',
                                                borderRadius: '20px',
                                            }}
                                        />
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                )}
            </section>

            {/* Tiempos de entrega */}
            <section className="admin-section">
                <div className="section-heading">
                    <div>
                        <span>Logística</span>
                        <h2>Tiempos de entrega</h2>
                        <p>
                            Indicadores de duración de los pedidos entregados.
                        </p>
                    </div>
                </div>

                <div className="admin-summary">
                    <article className="summary-card">
                        <span>Pedidos entregados</span>
                        <strong>{tiemposEntrega.pedidosEntregados}</strong>
                    </article>

                    <article className="summary-card">
                        <span>Promedio</span>
                        <strong>
                            {formatearMinutos(
                                tiemposEntrega.promedioMinutos,
                            )}
                        </strong>
                    </article>

                    <article className="summary-card">
                        <span>Entrega más rápida</span>
                        <strong>
                            {formatearMinutos(tiemposEntrega.minimoMinutos)}
                        </strong>
                    </article>

                    <article className="summary-card">
                        <span>Entrega más lenta</span>
                        <strong>
                            {formatearMinutos(tiemposEntrega.maximoMinutos)}
                        </strong>
                    </article>
                </div>
            </section>
        </section>
    );
}

export default AdminReportes;
