
import { useEffect, useState } from 'react';
import { useMsal } from '@azure/msal-react';

import {
    cambiarEstadoPedido,
    ESTADOS_PEDIDO,
    obtenerClientes,
    obtenerTodosLosPedidos,
} from '../api/adminApi';

import type {
    Cliente,
    Pedido,
    EstadoPedido,
} from '../api/adminApi';

import {
    actualizarProducto,
    crearProducto,
    obtenerProductos,
} from '../api/catalogoApi';

import type {
    Producto,
    ProductoRequest,
} from '../api/catalogoApi';

import '../styles/admin.css';

interface AdminPageProps {
    onLogout: () => void;
}

interface FormularioProducto {
    sku: string;
    nombre: string;
    descripcion: string;
    categoria: string;
    precio: string;
    imagenes: string;
    stock: string;
}

const FORMULARIO_VACIO: FormularioProducto = {
    sku: '',
    nombre: '',
    descripcion: '',
    categoria: '',
    precio: '',
    imagenes: '',
    stock: '0',
};

const CATEGORIAS = [
    'Accesorios',
    'Municion',
    'Cargadores',
    'Revolveres',
    'Pistolas',
    'Carabinas',
    'Escopetas',
    'Fusiles',
];

// Transiciones permitidas por las reglas del microservicio de Pedidos.
// CANCELADO solo es posible desde CREADO; los estados finales no avanzan.
const TRANSICIONES_ESTADO: Record<EstadoPedido, readonly EstadoPedido[]> = {
    CREADO: ['ACEPTADO', 'CANCELADO'],
    ACEPTADO: ['EN_PREPARACION'],
    EN_PREPARACION: ['DESPACHADO'],
    DESPACHADO: ['ENTREGADO'],
    ENTREGADO: [],
    CANCELADO: [],
};

function esEstadoPedido(estado: string): estado is EstadoPedido {
    return ESTADOS_PEDIDO.some((estadoValido) => estadoValido === estado);
}

function estadosDisponibles(estadoActual: string): EstadoPedido[] {
    if (!esEstadoPedido(estadoActual)) {
        return [];
    }

    const permitidos = TRANSICIONES_ESTADO[estadoActual];
    // Mantener el estado actual visible en el selector controlado.
    return ESTADOS_PEDIDO.filter(
        (estado) => estado === estadoActual || permitidos.includes(estado),
    );
}

function AdminPage({ onLogout }: AdminPageProps) {
    const { instance, accounts } = useMsal();

    const [clientes, setClientes] = useState<Cliente[]>([]);
    const [pedidos, setPedidos] = useState<Pedido[]>([]);
    const [productos, setProductos] = useState<Producto[]>([]);

    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState('');
    const [mensaje, setMensaje] = useState('');

    const [pedidoEnActualizacion, setPedidoEnActualizacion] =
        useState<number | null>(null);

    const [productoEnGuardado, setProductoEnGuardado] = useState(false);
    const [productoEnEdicion, setProductoEnEdicion] =
        useState<string | null>(null);

    const [busquedaProducto, setBusquedaProducto] = useState('');
    const [formulario, setFormulario] =
        useState<FormularioProducto>(FORMULARIO_VACIO);

    const [mostrarFormulario, setMostrarFormulario] = useState(false);

    async function cargarTodosLosProductos(
        account: NonNullable<ReturnType<typeof instance.getActiveAccount>>,
    ): Promise<Producto[]> {
        const tamanoPagina = 20;
        const primeraPagina = await obtenerProductos(
            instance,
            account,
            0,
            tamanoPagina,
        );

        const resultado = [...primeraPagina.content];

        for (
            let pagina = 1;
            pagina < primeraPagina.totalPages;
            pagina++
        ) {
            const respuesta = await obtenerProductos(
                instance,
                account,
                pagina,
                tamanoPagina,
            );

            resultado.push(...respuesta.content);
        }

        return resultado;
    }

    async function cargarDatos() {
        const account = instance.getActiveAccount() ?? accounts[0];

        if (!account) {
            setError('No se encontró una cuenta activa.');
            setCargando(false);
            return;
        }

        setCargando(true);
        setError('');

        try {
            const [clientesResultado, pedidosResultado, productosResultado] =
                await Promise.all([
                    obtenerClientes(instance, account),
                    obtenerTodosLosPedidos(instance, account),
                    cargarTodosLosProductos(account),
                ]);

            setClientes(clientesResultado);
            setPedidos(pedidosResultado);
            setProductos(productosResultado);
        } catch (err) {
            console.error('Error al cargar el panel administrativo:', err);

            setError(
                err instanceof Error
                    ? err.message
                    : 'No se pudieron cargar los datos.',
            );
        } finally {
            setCargando(false);
        }
    }

    useEffect(() => {
        // La función gestiona la carga, los errores y los resultados de la solicitud.
        // eslint-disable-next-line react-hooks/set-state-in-effect
        void cargarDatos();
        // La carga inicial depende de la instancia MSAL y sus cuentas.
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [instance, accounts]);

    function obtenerNombreCliente(clienteId: number) {
        const cliente = clientes.find((item) => item.id === clienteId);
        return cliente?.nombre ?? 'Cliente no encontrado';
    }

    function obtenerCantidadPedidos(clienteId: number) {
        return pedidos.filter(
            (pedido) => pedido.clienteId === clienteId,
        ).length;
    }

    async function actualizarEstado(
        pedidoId: number,
        estado: EstadoPedido,
    ) {
        const pedidoActual = pedidos.find((pedido) => pedido.id === pedidoId);

        if (!pedidoActual) {
            setError(`No se encontró el pedido #${pedidoId}.`);
            return;
        }

        if (!esEstadoPedido(pedidoActual.estado)) {
            setMensaje('');
            setError(
                `El pedido #${pedidoId} tiene un estado no reconocido: ${pedidoActual.estado}.`,
            );
            return;
        }

        if (pedidoActual.estado === estado) {
            return;
        }

        if (!TRANSICIONES_ESTADO[pedidoActual.estado]?.includes(estado)) {
            setMensaje('');
            setError(
                `No se permite cambiar el pedido #${pedidoId} de ${pedidoActual.estado} a ${estado}.`,
            );
            return;
        }

        if (pedidoEnActualizacion !== null) {
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
                estado,
            );

            setPedidos((actuales) =>
                actuales.map((pedido) =>
                    pedido.id === pedidoId ? pedidoActualizado : pedido,
                ),
            );

            setMensaje(`Pedido #${pedidoId} actualizado a ${estado}.`);
        } catch (err) {
            setError(
                err instanceof Error
                    ? err.message
                    : 'No se pudo actualizar el estado del pedido.',
            );
        } finally {
            setPedidoEnActualizacion(null);
        }
    }

    function actualizarCampoFormulario(
        campo: keyof FormularioProducto,
        valor: string,
    ) {
        setFormulario((actual) => ({
            ...actual,
            [campo]: valor,
        }));
    }

    function iniciarCreacion() {
        setProductoEnEdicion(null);
        setFormulario(FORMULARIO_VACIO);
        setMostrarFormulario(true);
        setError('');
        setMensaje('');
    }

    function iniciarEdicion(producto: Producto) {
        setProductoEnEdicion(producto.sku);

        setFormulario({
            sku: producto.sku,
            nombre: producto.nombre,
            descripcion: producto.descripcion,
            categoria: producto.categoria,
            precio: String(producto.precio),
            imagenes: producto.imagenes.join('\n'),
            stock: String(producto.stock),
        });

        setMostrarFormulario(true);
        setError('');
        setMensaje('');
    }

    function cancelarFormulario() {
        setMostrarFormulario(false);
        setProductoEnEdicion(null);
        setFormulario(FORMULARIO_VACIO);
    }

    async function guardarProducto(
        event: React.FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault();

        const account = instance.getActiveAccount() ?? accounts[0];

        if (!account) {
            setError('No se encontró una cuenta activa.');
            return;
        }

        const precio = Number(formulario.precio);
        const stock = Number(formulario.stock);

        const imagenes = formulario.imagenes
            .split('\n')
            .map((imagen) => imagen.trim())
            .filter(Boolean);

        if (!Number.isFinite(precio) || precio <= 0) {
            setError('El precio debe ser un número mayor que cero.');
            return;
        }

        if (!Number.isInteger(stock) || stock < 0) {
            setError('El stock debe ser un número entero igual o mayor que cero.');
            return;
        }

        if (imagenes.length === 0) {
            setError('Debes agregar al menos una URL de imagen.');
            return;
        }

        const producto: ProductoRequest = {
            sku: formulario.sku.trim(),
            nombre: formulario.nombre.trim(),
            descripcion: formulario.descripcion.trim(),
            categoria: formulario.categoria.trim(),
            precio,
            imagenes,
            stock,
        };

        if (
            !producto.sku ||
            !producto.nombre ||
            !producto.descripcion ||
            !producto.categoria
        ) {
            setError('Completa todos los campos obligatorios.');
            return;
        }

        setProductoEnGuardado(true);
        setError('');
        setMensaje('');

        try {
            if (productoEnEdicion) {
                await actualizarProducto(
                    instance,
                    account,
                    productoEnEdicion,
                    producto,
                );

                setMensaje(`Producto "${producto.nombre}" actualizado correctamente.`);
            } else {
                await crearProducto(instance, account, producto);
                setMensaje(`Producto "${producto.nombre}" creado correctamente.`);
            }

            const productosActualizados = await cargarTodosLosProductos(account);
            setProductos(productosActualizados);
            cancelarFormulario();
        } catch (err) {
            console.error('Error al guardar producto:', err);

            setError(
                err instanceof Error
                    ? err.message
                    : 'No se pudo guardar el producto.',
            );
        } finally {
            setProductoEnGuardado(false);
        }
    }

    const productosFiltrados = productos.filter((producto) => {
        const consulta = busquedaProducto.trim().toLowerCase();

        return (
            producto.nombre.toLowerCase().includes(consulta) ||
            producto.sku.toLowerCase().includes(consulta) ||
            producto.categoria.toLowerCase().includes(consulta)
        );
    });

    return (
        <main className="admin-page">
            <header className="admin-header">
                <div>
                    <span className="admin-label">PEDIDOS360</span>
                    <h1>Panel de Administración</h1>
                    <p>Gestión de clientes, productos y pedidos.</p>
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
                    <p>Cargando información...</p>
                </section>
            )}

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

            {!cargando && !error && (
                <>
                    <section className="admin-summary">
                        <article className="summary-card">
                            <span>Clientes</span>
                            <strong>{clientes.length}</strong>
                        </article>

                        <article className="summary-card">
                            <span>Productos</span>
                            <strong>{productos.length}</strong>
                        </article>

                        <article className="summary-card">
                            <span>Pedidos</span>
                            <strong>{pedidos.length}</strong>
                        </article>

                        <article className="summary-card">
                            <span>Pedidos pendientes</span>
                            <strong>
                                {
                                    pedidos.filter(
                                        (pedido) =>
                                            pedido.estado.toUpperCase() === 'CREADO',
                                    ).length
                                }
                            </strong>
                        </article>
                    </section>

                    <section className="admin-section">
                        <div className="section-heading admin-product-heading">
                            <div>
                                <span>Catálogo</span>
                                <h2>Gestión de productos</h2>
                                <p>
                                    {productos.length} productos registrados
                                </p>
                            </div>

                            <button
                                className="admin-primary-button"
                                type="button"
                                onClick={iniciarCreacion}
                            >
                                + Nuevo producto
                            </button>
                        </div>

                        <div className="admin-product-search">
                            <label htmlFor="buscar-producto">
                                Buscar por nombre, SKU o categoría
                            </label>
                            <input
                                id="buscar-producto"
                                type="search"
                                placeholder="Ej. M4A1, SKU-001..."
                                value={busquedaProducto}
                                onChange={(event) =>
                                    setBusquedaProducto(event.target.value)
                                }
                            />
                        </div>

                        {mostrarFormulario && (
                            <form
                                className="admin-product-form"
                                onSubmit={(event) => void guardarProducto(event)}
                            >
                                <div className="admin-product-form-heading">
                                    <div>
                                        <span>
                                            {productoEnEdicion
                                                ? 'EDITAR CATÁLOGO'
                                                : 'NUEVO REGISTRO'}
                                        </span>
                                        <h3>
                                            {productoEnEdicion
                                                ? `Editar ${productoEnEdicion}`
                                                : 'Crear producto'}
                                        </h3>
                                    </div>

                                    <button
                                        className="admin-icon-button"
                                        type="button"
                                        onClick={cancelarFormulario}
                                        aria-label="Cerrar formulario"
                                    >
                                        ×
                                    </button>
                                </div>

                                <div className="admin-product-form-grid">
                                    <label>
                                        SKU *
                                        <input
                                            value={formulario.sku}
                                            onChange={(event) =>
                                                actualizarCampoFormulario(
                                                    'sku',
                                                    event.target.value,
                                                )
                                            }
                                            required
                                            disabled={Boolean(productoEnEdicion)}
                                            maxLength={100}
                                        />
                                    </label>

                                    <label>
                                        Nombre *
                                        <input
                                            value={formulario.nombre}
                                            onChange={(event) =>
                                                actualizarCampoFormulario(
                                                    'nombre',
                                                    event.target.value,
                                                )
                                            }
                                            required
                                            maxLength={150}
                                        />
                                    </label>

                                    <label>
                                        Categoría *
                                        <input
                                            list="categorias-productos"
                                            value={formulario.categoria}
                                            onChange={(event) =>
                                                actualizarCampoFormulario(
                                                    'categoria',
                                                    event.target.value,
                                                )
                                            }
                                            required
                                        />
                                        <datalist id="categorias-productos">
                                            {CATEGORIAS.map((categoria) => (
                                                <option
                                                    key={categoria}
                                                    value={categoria}
                                                />
                                            ))}
                                        </datalist>
                                    </label>

                                    <label>
                                        Precio (CLP) *
                                        <input
                                            type="number"
                                            min="1"
                                            step="1"
                                            value={formulario.precio}
                                            onChange={(event) =>
                                                actualizarCampoFormulario(
                                                    'precio',
                                                    event.target.value,
                                                )
                                            }
                                            required
                                        />
                                    </label>

                                    <label>
                                        Stock *
                                        <input
                                            type="number"
                                            min="0"
                                            step="1"
                                            value={formulario.stock}
                                            onChange={(event) =>
                                                actualizarCampoFormulario(
                                                    'stock',
                                                    event.target.value,
                                                )
                                            }
                                            required
                                        />
                                    </label>

                                    <label className="admin-product-description">
                                        Descripción *
                                        <textarea
                                            value={formulario.descripcion}
                                            onChange={(event) =>
                                                actualizarCampoFormulario(
                                                    'descripcion',
                                                    event.target.value,
                                                )
                                            }
                                            required
                                            rows={3}
                                        />
                                    </label>

                                    <label className="admin-product-description">
                                        URLs de imágenes *
                                        <textarea
                                            value={formulario.imagenes}
                                            onChange={(event) =>
                                                actualizarCampoFormulario(
                                                    'imagenes',
                                                    event.target.value,
                                                )
                                            }
                                            placeholder={'https://.../imagen1.jpg\nhttps://.../imagen2.jpg'}
                                            required
                                            rows={3}
                                        />
                                        <small>
                                            Una URL por línea. Debes agregar al menos una.
                                        </small>
                                    </label>
                                </div>

                                <div className="admin-product-form-actions">
                                    <button
                                        className="admin-secondary-button"
                                        type="button"
                                        onClick={cancelarFormulario}
                                        disabled={productoEnGuardado}
                                    >
                                        Cancelar
                                    </button>

                                    <button
                                        className="admin-primary-button"
                                        type="submit"
                                        disabled={productoEnGuardado}
                                    >
                                        {productoEnGuardado
                                            ? 'Guardando...'
                                            : productoEnEdicion
                                              ? 'Guardar cambios'
                                              : 'Crear producto'}
                                    </button>
                                </div>
                            </form>
                        )}

                        {productosFiltrados.length === 0 ? (
                            <p className="empty-message">
                                {productos.length === 0
                                    ? 'No hay productos registrados.'
                                    : 'No se encontraron productos para esa búsqueda.'}
                            </p>
                        ) : (
                            <div className="clients-table-wrapper">
                                <table className="admin-table admin-products-table">
                                    <thead>
                                        <tr>
                                            <th>SKU</th>
                                            <th>Producto</th>
                                            <th>Categoría</th>
                                            <th>Precio</th>
                                            <th>Stock</th>
                                            <th>Acciones</th>
                                        </tr>
                                    </thead>

                                    <tbody>
                                        {productosFiltrados.map((producto) => (
                                            <tr key={producto.sku}>
                                                <td>{producto.sku}</td>
                                                <td className="table-primary">
                                                    <div className="admin-product-name">
                                                        {producto.imagenes?.[0] && (
                                                            <img
                                                                src={producto.imagenes[0]}
                                                                alt=""
                                                                loading="lazy"
                                                            />
                                                        )}
                                                        <span>{producto.nombre}</span>
                                                    </div>
                                                </td>
                                                <td>{producto.categoria}</td>
                                                <td>
                                                    $
                                                    {producto.precio.toLocaleString(
                                                        'es-CL',
                                                    )}
                                                </td>
                                                <td>
                                                    <span
                                                        className={
                                                            producto.stock === 0
                                                                ? 'admin-stock-empty'
                                                                : 'admin-stock-available'
                                                        }
                                                    >
                                                        {producto.stock}
                                                    </span>
                                                </td>
                                                <td>
                                                    <button
                                                        className="admin-edit-button"
                                                        type="button"
                                                        onClick={() =>
                                                            iniciarEdicion(producto)
                                                        }
                                                    >
                                                        Editar
                                                    </button>
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
                                <span>Gestión</span>
                                <h2>Clientes registrados</h2>
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
                                                    {obtenerCantidadPedidos(
                                                        cliente.id,
                                                    )}
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
                                <span>Gestión</span>
                                <h2>Todos los pedidos</h2>
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
                                                        pedidoEnActualizacion !== null ||
                                                        estadosDisponibles(pedido.estado).length <= 1
                                                    }
                                                    onChange={(event) => {
                                                        const nuevoEstado =
                                                            ESTADOS_PEDIDO.find(
                                                                (estado) =>
                                                                    estado ===
                                                                    event.currentTarget.value,
                                                            );

                                                        if (nuevoEstado) {
                                                            void actualizarEstado(
                                                                pedido.id,
                                                                nuevoEstado,
                                                            );
                                                        }
                                                    }}
                                                >
                                                    {estadosDisponibles(pedido.estado).map((estado) => (
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
                                                <strong>
                                                    {pedido.detalles.reduce(
                                                        (total, detalle) =>
                                                            total + detalle.cantidad,
                                                        0,
                                                    )}
                                                </strong>
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
                                            {pedido.detalles.map(
                                                (detalle, index) => (
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
                <span>Pedidos360</span>
                <span>•</span>
                <span>Panel de Administración</span>
            </footer>
        </main>
    );
}

export default AdminPage;
