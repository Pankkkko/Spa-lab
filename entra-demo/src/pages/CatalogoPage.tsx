
import { useEffect, useMemo, useState } from 'react';
import { useMsal } from '@azure/msal-react';

import {
    obtenerProductos,
    type Producto,
} from '../api/catalogoApi';

import CatalogoNavbar from '../components/CatalogoNavbar';
import CatalogoHero from '../components/CatalogoHero';
import CatalogoFiltros from '../components/CatalogoFiltros';
import CatalogoResultados from '../components/CatalogoResultados';
import ProductoCard from '../components/ProductoCard';
import CatalogoEstado from '../components/CatalogoEstado';
import ProductoDetalle from '../components/ProductoDetalle';
import CarritoModal from '../components/CarritoModal';
import { useCarrito } from '../hooks/useCarrito';

import '../styles/catalogo.css';

interface CatalogoProps {
    isAuthenticated: boolean;
    onBack: () => void;
    onLogin: () => void;
    onLogout: () => void;
}

export default function CatalogoPage({
    isAuthenticated,
    onBack,
    onLogin,
    onLogout,
}: CatalogoProps) {
    const { instance, accounts } = useMsal();

    const [productos, setProductos] = useState<Producto[]>([]);
    const [categoriaSeleccionada, setCategoriaSeleccionada] =
        useState('Todos');
    const [busqueda, setBusqueda] = useState('');
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState('');
    const [productoSeleccionado, setProductoSeleccionado] =
        useState<Producto | null>(null);
    const [carritoAbierto, setCarritoAbierto] = useState(false);

    const {
        items,
        cantidadTotal,
        agregarProducto,
        cambiarCantidad,
        eliminarProducto,
        vaciarCarrito,
        reconciliarStock,
    } = useCarrito();

    useEffect(() => {
        let cancelado = false;

        async function cargarProductos() {
            try {
                setCargando(true);
                setError('');

                const account =
                    instance.getActiveAccount() ?? accounts[0];

                const resultado = await obtenerProductos(
                    account ? instance : undefined,
                    account ?? undefined,
                    0,
                    100,
                );

                if (!cancelado) {
                    setProductos(resultado.content);
                }
            } catch (error) {
                console.error('Error al cargar catálogo:', error);

                if (!cancelado) {
                    setError('No fue posible cargar el catálogo.');
                }
            } finally {
                if (!cancelado) {
                    setCargando(false);
                }
            }
        }

        void cargarProductos();

        return () => {
            cancelado = true;
        };
    }, [instance, accounts]);

    useEffect(() => {
        if (!cargando && !error) {
            reconciliarStock(productos);
        }
    }, [productos, cargando, error, reconciliarStock]);

    const productosFiltrados = useMemo(() => {
        const textoBusqueda = busqueda.toLowerCase().trim();

        return productos.filter((producto) => {
            const coincideCategoria =
                categoriaSeleccionada === 'Todos' ||
                producto.categoria === categoriaSeleccionada;

            const coincideBusqueda =
                textoBusqueda === '' ||
                producto.nombre.toLowerCase().includes(textoBusqueda) ||
                producto.sku.toLowerCase().includes(textoBusqueda) ||
                producto.descripcion.toLowerCase().includes(textoBusqueda);

            return coincideCategoria && coincideBusqueda;
        });
    }, [productos, categoriaSeleccionada, busqueda]);

    const manejarAgregarAlCarrito = (producto: Producto) => {
        const agregado = agregarProducto(producto);

        if (agregado) {
            setCarritoAbierto(true);
        }
    };

    return (
        <div className="catalogo-page">

            <CatalogoNavbar
                isAuthenticated={isAuthenticated}
                nombreUsuario={
                    (instance.getActiveAccount() ?? accounts[0])?.name ??
                    (instance.getActiveAccount() ?? accounts[0])?.username
                }
                cantidadCarrito={cantidadTotal}
                paginaActual="catalogo"
                onInicio={onBack}
                onCatalogo={() => { }}
                onAbrirPedidos={onBack}
                onLogin={onLogin}
                onLogout={onLogout}
                onAbrirCarrito={() => setCarritoAbierto(true)}
            />


            {cargando ? (
                <CatalogoEstado tipo="cargando" />
            ) : error ? (
                <CatalogoEstado
                    tipo="error"
                    mensaje={error}
                    onReintentar={() => window.location.reload()}
                />
            ) : productoSeleccionado ? (
                <ProductoDetalle
                    producto={
                        productos.find(
                            (producto) =>
                                producto.sku === productoSeleccionado.sku,
                        ) ?? productoSeleccionado
                    }
                    onVolver={() => setProductoSeleccionado(null)}
                    onAgregarAlCarrito={manejarAgregarAlCarrito}
                />
            ) : (
                <>
                    <CatalogoHero />

                    <CatalogoFiltros
                        busqueda={busqueda}
                        onBusquedaChange={setBusqueda}
                        categoriaSeleccionada={categoriaSeleccionada}
                        onCategoriaChange={setCategoriaSeleccionada}
                    />

                    <section className="catalogo-resultados">
                        <CatalogoResultados
                            categoriaSeleccionada={categoriaSeleccionada}
                            cantidadProductos={productosFiltrados.length}
                        />

                        {productosFiltrados.length === 0 ? (
                            <CatalogoEstado tipo="vacio" />
                        ) : (
                            <div className="productos-grid">
                                {productosFiltrados.map((producto) => (
                                    <ProductoCard
                                        key={producto.sku}
                                        producto={producto}
                                        onClick={setProductoSeleccionado}
                                    />
                                ))}
                            </div>
                        )}
                    </section>
                </>
            )}

            <CarritoModal
                abierto={carritoAbierto}
                items={items}
                onCerrar={() => setCarritoAbierto(false)}
                onCambiarCantidad={cambiarCantidad}
                onEliminar={eliminarProducto}
                onVaciar={vaciarCarrito}
            />
        </div>
    );
}
