
import { useCallback, useEffect, useState } from 'react';
import { useMsal } from '@azure/msal-react';

import {
    obtenerRegistrosAudit,
    obtenerDetalleAudit,
} from '../../api/auditApi';

import type {
    AuditRegistroResponse,
} from '../../api/auditApi';

import {
    obtenerClientes,
} from '../../api/adminApi';

import type {
    Cliente,
} from '../../api/adminApi';

import '../../styles/admin.css';

const TAMANO_PAGINA = 10;

function formatearFecha(fecha: string): string {
    if (!fecha) {
        return 'Fecha no disponible';
    }

    const fechaConvertida = new Date(fecha);

    if (Number.isNaN(fechaConvertida.getTime())) {
        return fecha;
    }

    return fechaConvertida.toLocaleString('es-CL', {
        dateStyle: 'short',
        timeStyle: 'medium',
    });
}

function AdminAuditoria() {
    const { instance, accounts } = useMsal();

    const [registros, setRegistros] = useState<AuditRegistroResponse[]>([]);
    const [clientes, setClientes] = useState<Cliente[]>([]);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState('');
    const [errorClientes, setErrorClientes] = useState('');

    const [paginaActual, setPaginaActual] = useState(0);
    const [totalPaginas, setTotalPaginas] = useState(0);
    const [totalRegistros, setTotalRegistros] = useState(0);

    const [busqueda, setBusqueda] = useState('');
    const [registroSeleccionado, setRegistroSeleccionado] =
        useState<AuditRegistroResponse | null>(null);

    const [detalle, setDetalle] =
        useState<Awaited<ReturnType<typeof obtenerDetalleAudit>> | null>(null);

    const [cargandoDetalle, setCargandoDetalle] = useState(false);
    const [errorDetalle, setErrorDetalle] = useState('');

    // Cargar clientes para resolver el nombre asociado a cada clienteId.
    const cargarClientes = useCallback(async () => {
        const account = instance.getActiveAccount() ?? accounts[0];

        if (!account) {
            setErrorClientes('No se encontró una cuenta activa.');
            return;
        }

        setErrorClientes('');

        try {
            const resultado = await obtenerClientes(instance, account);
            setClientes(resultado);
        } catch (err) {
            console.error('Error al cargar los clientes:', err);

            setErrorClientes(
                err instanceof Error
                    ? err.message
                    : 'No se pudieron cargar los clientes.',
            );
        }
    }, [instance, accounts]);

    // Resolver nombre del cliente mediante su ID.
    function obtenerNombreCliente(clienteId: number | null): string {
        if (clienteId === null || clienteId === undefined) {
            return 'No asociado';
        }

        const cliente = clientes.find(
            (item) => item.id === clienteId,
        );

        return cliente?.nombre ?? 'Cliente no encontrado';
    }

    useEffect(() => {
        void cargarClientes();
    }, [cargarClientes]);

    const cargarRegistros = useCallback(async () => {
        const account = instance.getActiveAccount() ?? accounts[0];

        if (!account) {
            setError('No se encontró una cuenta activa.');
            setCargando(false);
            return;
        }

        setCargando(true);
        setError('');

        try {
            const resultado = await obtenerRegistrosAudit(
                instance,
                account,
                {
                    page: paginaActual,
                    size: TAMANO_PAGINA,
                },
            );

            setRegistros(resultado.content);
            setTotalPaginas(resultado.totalPages);
            setTotalRegistros(resultado.totalElements);
        } catch (err) {
            console.error('Error al cargar los registros de auditoría:', err);

            setError(
                err instanceof Error
                    ? err.message
                    : 'No se pudieron cargar los registros de auditoría.',
            );
        } finally {
            setCargando(false);
        }
    }, [instance, accounts, paginaActual]);

    useEffect(() => {
        const timeoutId = window.setTimeout(() => {
            void cargarRegistros();
        }, 0);

        return () => window.clearTimeout(timeoutId);
    }, [cargarRegistros]);

    async function abrirDetalle(registro: AuditRegistroResponse) {
        const account = instance.getActiveAccount() ?? accounts[0];

        if (!account) {
            setErrorDetalle('No se encontró una cuenta activa.');
            return;
        }

        setRegistroSeleccionado(registro);
        setDetalle(null);
        setErrorDetalle('');
        setCargandoDetalle(true);

        try {
            const resultado = await obtenerDetalleAudit(
                instance,
                account,
                registro.id,
            );

            setDetalle(resultado);
        } catch (err) {
            console.error('Error al obtener el detalle de auditoría:', err);

            setErrorDetalle(
                err instanceof Error
                    ? err.message
                    : 'No se pudo obtener el detalle del evento.',
            );
        } finally {
            setCargandoDetalle(false);
        }
    }

    function cerrarDetalle() {
        setRegistroSeleccionado(null);
        setDetalle(null);
        setErrorDetalle('');
    }

    const registrosFiltrados = registros.filter((registro) => {
        const termino = busqueda.trim().toLowerCase();

        if (!termino) {
            return true;
        }

        const nombreCliente = obtenerNombreCliente(registro.clienteId);

        return [
            registro.eventId,
            registro.usuarioNombre,
            registro.usuarioId,
            registro.clienteId,
            nombreCliente,
            registro.tipoEvento,
            registro.entidad,
            registro.entidadId,
            registro.descripcion,
            registro.origen,
            registro.traceId,
            registro.correlationId,
        ].some((valor) =>
            String(valor ?? '').toLowerCase().includes(termino),
        );
    });

    if (cargando) {
        return (
            <section className="admin-message">
                <p>Cargando registros de auditoría...</p>
            </section>
        );
    }

    if (error) {
        return (
            <section className="admin-error" role="alert">
                <strong>No se pudo cargar la auditoría.</strong>
                <p>{error}</p>

                <button
                    className="admin-secondary-button"
                    type="button"
                    onClick={() => void cargarRegistros()}
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
                    <span>Seguridad y trazabilidad</span>
                    <h2>Registro de auditoría</h2>
                    <p>
                        Historial de eventos procesados por Pedidos360.
                    </p>
                </div>

                <button
                    className="admin-secondary-button"
                    type="button"
                    onClick={() => {
                        void cargarRegistros();
                        void cargarClientes();
                    }}
                >
                    Actualizar registros
                </button>
            </div>

            {/* Resumen */}
            <div className="admin-summary">
                <article className="summary-card">
                    <span>Eventos registrados</span>
                    <strong>{totalRegistros}</strong>
                </article>

                <article className="summary-card">
                    <span>Página actual</span>
                    <strong>
                        {totalPaginas === 0 ? 0 : paginaActual + 1}
                        {' / '}
                        {totalPaginas}
                    </strong>
                </article>

                <article className="summary-card">
                    <span>Eventos visibles</span>
                    <strong>{registrosFiltrados.length}</strong>
                </article>
            </div>

            {/* Búsqueda */}
            <div className="admin-product-search">
                <label htmlFor="buscar-auditoria">
                    Buscar en los registros de esta página
                </label>

                <input
                    id="buscar-auditoria"
                    type="search"
                    placeholder="Evento, cliente, usuario, pedido, traceId..."
                    value={busqueda}
                    onChange={(event) => setBusqueda(event.target.value)}
                />
            </div>

            {errorClientes && (
                <p className="empty-message">
                    No fue posible cargar los nombres de los clientes.
                    Se mostrarán sus identificadores cuando estén disponibles.
                </p>
            )}

            {/* Tabla de eventos */}
            {registrosFiltrados.length === 0 ? (
                <p className="empty-message">
                    {registros.length === 0
                        ? 'No hay eventos de auditoría registrados.'
                        : 'No se encontraron eventos para esa búsqueda.'}
                </p>
            ) : (
                <div className="clients-table-wrapper">
                    <table className="admin-table">
                        <thead>
                            <tr>
                                <th>Fecha</th>
                                <th>Evento</th>
                                <th>Cliente del pedido</th>
                                <th>Usuario</th>
                                <th>Entidad</th>
                                <th>ID entidad</th>
                                <th>Origen</th>
                                <th>Detalle</th>
                            </tr>
                        </thead>

                        <tbody>
                            {registrosFiltrados.map((registro) => (
                                <tr key={registro.id}>
                                    <td>
                                        {formatearFecha(registro.fechaEvento)}
                                    </td>

                                    <td className="table-primary">
                                        {registro.tipoEvento}
                                    </td>

                                    <td>
                                        {registro.clienteId !== null ? (
                                            <>
                                                <strong>
                                                    {obtenerNombreCliente(
                                                        registro.clienteId,
                                                    )}
                                                </strong>
                                                <br />
                                                <small>
                                                    ID: {registro.clienteId}
                                                </small>
                                            </>
                                        ) : (
                                            'No asociado'
                                        )}
                                    </td>

                                    <td>
                                        {registro.usuarioNombre ??
                                            'Administrador del sistema'}
                                    </td>

                                    <td>{registro.entidad}</td>

                                    <td>
                                        {registro.entidadId ?? '-'}
                                    </td>

                                    <td>{registro.origen ?? '-'}</td>

                                    <td>
                                        <button
                                            className="admin-secondary-button"
                                            type="button"
                                            onClick={() =>
                                                void abrirDetalle(registro)
                                            }
                                        >
                                            Inspeccionar
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            {/* Paginación */}
            <div
                style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    flexWrap: 'wrap',
                    gap: '12px',
                    marginTop: '20px',
                }}
            >
                <span>
                    Página {totalPaginas === 0 ? 0 : paginaActual + 1} de{' '}
                    {totalPaginas}
                </span>

                <div style={{ display: 'flex', gap: '8px' }}>
                    <button
                        className="admin-secondary-button"
                        type="button"
                        disabled={paginaActual === 0 || cargando}
                        onClick={() =>
                            setPaginaActual((actual) => actual - 1)
                        }
                    >
                        Anterior
                    </button>

                    <button
                        className="admin-secondary-button"
                        type="button"
                        disabled={
                            paginaActual + 1 >= totalPaginas || cargando
                        }
                        onClick={() =>
                            setPaginaActual((actual) => actual + 1)
                        }
                    >
                        Siguiente
                    </button>
                </div>
            </div>

            {/* Panel de detalle */}
            {registroSeleccionado && (
                <div
                    style={{
                        position: 'fixed',
                        inset: 0,
                        zIndex: 1000,
                        background: 'rgba(0, 0, 0, 0.55)',
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                        padding: '20px',
                    }}
                    onMouseDown={(event) => {
                        if (event.target === event.currentTarget) {
                            cerrarDetalle();
                        }
                    }}
                >
                    <section
                        className="admin-product-form"
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby="auditoria-detalle-titulo"
                        style={{
                            width: '100%',
                            maxWidth: '850px',
                            maxHeight: '85vh',
                            overflowY: 'auto',
                        }}
                    >
                        <div className="admin-product-form-heading">
                            <div>
                                <span>INSPECCIÓN DE EVENTO</span>

                                <h3 id="auditoria-detalle-titulo">
                                    {registroSeleccionado.tipoEvento}
                                </h3>
                            </div>

                            <button
                                className="admin-icon-button"
                                type="button"
                                onClick={cerrarDetalle}
                                aria-label="Cerrar detalle"
                            >
                                ×
                            </button>
                        </div>

                        {cargandoDetalle ? (
                            <p className="admin-message">
                                Cargando información del evento...
                            </p>
                        ) : errorDetalle ? (
                            <section className="admin-error" role="alert">
                                {errorDetalle}
                            </section>
                        ) : detalle ? (
                            <div
                                style={{
                                    display: 'grid',
                                    gap: '18px',
                                }}
                            >
                                <div className="admin-product-form-grid">
                                    <label>
                                        ID del registro
                                        <input
                                            readOnly
                                            value={String(detalle.id)}
                                        />
                                    </label>

                                    <label>
                                        Event ID
                                        <input
                                            readOnly
                                            value={detalle.eventId ?? '-'}
                                        />
                                    </label>

                                    <label>
                                        Cliente del pedido
                                        <input
                                            readOnly
                                            value={obtenerNombreCliente(
                                                detalle.clienteId,
                                            )}
                                        />
                                    </label>

                                    <label>
                                        Cliente ID
                                        <input
                                            readOnly
                                            value={
                                                detalle.clienteId !== null
                                                    ? String(detalle.clienteId)
                                                    : 'No asociado'
                                            }
                                        />
                                    </label>

                                    <label>
                                        Usuario que ejecutó la acción
                                        <input
                                            readOnly
                                            value={
                                                detalle.usuarioNombre ??
                                                'Administrador del sistema'
                                            }
                                        />
                                    </label>

                                    <label>
                                        Usuario ID
                                        <input
                                            readOnly
                                            value={detalle.usuarioId ?? '-'}
                                        />
                                    </label>

                                    <label>
                                        Fecha del evento
                                        <input
                                            readOnly
                                            value={formatearFecha(
                                                detalle.fechaEvento,
                                            )}
                                        />
                                    </label>

                                    <label>
                                        Entidad
                                        <input
                                            readOnly
                                            value={detalle.entidad ?? '-'}
                                        />
                                    </label>

                                    <label>
                                        ID de entidad
                                        <input
                                            readOnly
                                            value={detalle.entidadId ?? '-'}
                                        />
                                    </label>

                                    <label>
                                        Origen
                                        <input
                                            readOnly
                                            value={detalle.origen ?? '-'}
                                        />
                                    </label>

                                    <label>
                                        Fecha de recepción
                                        <input
                                            readOnly
                                            value={
                                                detalle.fechaRecepcion
                                                    ? formatearFecha(
                                                          detalle.fechaRecepcion,
                                                      )
                                                    : '-'
                                            }
                                        />
                                    </label>
                                </div>

                                <div>
                                    <strong>Descripción</strong>
                                    <p>
                                        {detalle.descripcion ??
                                            'Sin descripción disponible.'}
                                    </p>
                                </div>

                                <div>
                                    <strong>Trace ID</strong>
                                    <p style={{ overflowWrap: 'anywhere' }}>
                                        {detalle.traceId ?? '-'}
                                    </p>
                                </div>

                                <div>
                                    <strong>Correlation ID</strong>
                                    <p style={{ overflowWrap: 'anywhere' }}>
                                        {detalle.correlationId ?? '-'}
                                    </p>
                                </div>

                                <div>
                                    <strong>Payload del evento</strong>

                                    <pre
                                        style={{
                                            marginTop: '10px',
                                            padding: '16px',
                                            borderRadius: '10px',
                                            background: '#102f36',
                                            color: '#d8f3ed',
                                            overflowX: 'auto',
                                            whiteSpace: 'pre-wrap',
                                            overflowWrap: 'anywhere',
                                            fontSize: '12px',
                                            lineHeight: 1.6,
                                        }}
                                    >
                                        {JSON.stringify(
                                            detalle.payload,
                                            null,
                                            2,
                                        )}
                                    </pre>
                                </div>
                            </div>
                        ) : (
                            <p className="empty-message">
                                No hay información adicional disponible.
                            </p>
                        )}

                        <div
                            style={{
                                display: 'flex',
                                justifyContent: 'flex-end',
                                marginTop: '20px',
                            }}
                        >
                            <button
                                className="admin-secondary-button"
                                type="button"
                                onClick={cerrarDetalle}
                            >
                                Cerrar
                            </button>
                        </div>
                    </section>
                </div>
            )}
        </section>
    );
}

export default AdminAuditoria;
