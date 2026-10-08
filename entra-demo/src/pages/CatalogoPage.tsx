
import { useEffect, useMemo, useState } from 'react';
import { useMsal } from '@azure/msal-react';

import {
    obtenerProductos,
    type Producto,
} from '../api/catalogoApi';

import '../styles/catalogo.css';


/* =========================================================
   CATEGORÍAS
   ========================================================= */

const CATEGORIAS = [
    'Todos',
    'Accesorios',
    'Municion',
    'Cargadores',
    'Revolveres',
    'Pistolas',
    'Carabinas',
    'Escopetas',
    'Fusiles',
];


/* =========================================================
   PROPS
   ========================================================= */

interface CatalogoProps {
    onBack: () => void;
    onLogout: () => void;
}


/* =========================================================
   COMPONENTE
   ========================================================= */

export default function Catalogo({
    onBack,
    onLogout,
}: CatalogoProps) {

    const { instance, accounts } = useMsal();


    /* =====================================================
       ESTADO
       ===================================================== */

    const [productos, setProductos] =
        useState<Producto[]>([]);

    const [categoriaSeleccionada, setCategoriaSeleccionada] =
        useState('Todos');

    const [busqueda, setBusqueda] =
        useState('');

    const [cargando, setCargando] =
        useState(true);

    const [error, setError] =
        useState('');


    /* =====================================================
       CARGAR PRODUCTOS
       ===================================================== */

    useEffect(() => {

        async function cargarProductos() {

            if (accounts.length === 0) {
                setCargando(false);
                return;
            }

            try {

                setCargando(true);
                setError('');

                const resultado = await obtenerProductos(
                    instance,
                    accounts[0],
                    0,
                    100,
                );

                setProductos(
                    resultado.content,
                );

            } catch (error) {

                console.error(
                    'Error al cargar catálogo:',
                    error,
                );

                setError(
                    'No fue posible cargar el catálogo.',
                );

            } finally {

                setCargando(false);

            }
        }

        void cargarProductos();

    }, [instance, accounts]);


    /* =====================================================
       FILTRADO
       ===================================================== */

    const productosFiltrados = useMemo(() => {

        return productos.filter((producto) => {

            const coincideCategoria =
                categoriaSeleccionada === 'Todos' ||
                producto.categoria === categoriaSeleccionada;


            const textoBusqueda =
                busqueda
                    .toLowerCase()
                    .trim();


            const coincideBusqueda =
                textoBusqueda === '' ||
                producto.nombre
                    .toLowerCase()
                    .includes(textoBusqueda) ||
                producto.sku
                    .toLowerCase()
                    .includes(textoBusqueda) ||
                producto.descripcion
                    .toLowerCase()
                    .includes(textoBusqueda);


            return (
                coincideCategoria &&
                coincideBusqueda
            );

        });

    }, [
        productos,
        categoriaSeleccionada,
        busqueda,
    ]);


    /* =====================================================
       FORMATEAR PRECIO
       ===================================================== */

    const formatearPrecio = (
        precio: number,
    ) => {

        return new Intl.NumberFormat(
            'es-CL',
            {
                style: 'currency',
                currency: 'CLP',
                maximumFractionDigits: 0,
            },
        ).format(precio);

    };


    /* =====================================================
       LOADING
       ===================================================== */

    if (cargando) {

        return (
            <div className="catalogo-page">

                <nav className="catalogo-navbar">

                    <div className="catalogo-navbar-brand">

                        <div className="catalogo-navbar-logo">
                            P360
                        </div>

                        <span>
                            Pedidos360
                        </span>

                    </div>

                    <div className="catalogo-navbar-actions">

                        <button
                            type="button"
                            onClick={onBack}
                        >
                            Inicio
                        </button>

                        <button
                            type="button"
                            onClick={onLogout}
                        >
                            Cerrar sesión
                        </button>

                    </div>

                </nav>

                <div className="catalogo-loading">

                    <div className="catalogo-spinner"></div>

                    <p>
                        Cargando catálogo...
                    </p>

                </div>

            </div>
        );
    }


    /* =====================================================
       ERROR
       ===================================================== */

    if (error) {

        return (
            <div className="catalogo-page">

                <nav className="catalogo-navbar">

                    <div className="catalogo-navbar-brand">

                        <div className="catalogo-navbar-logo">
                            P360
                        </div>

                        <span>
                            Pedidos360
                        </span>

                    </div>

                    <div className="catalogo-navbar-actions">

                        <button
                            type="button"
                            onClick={onBack}
                        >
                            Inicio
                        </button>

                        <button
                            type="button"
                            onClick={onLogout}
                        >
                            Cerrar sesión
                        </button>

                    </div>

                </nav>


                <div className="catalogo-error">

                    <div className="catalogo-error-icon">
                        !
                    </div>

                    <h2>
                        No pudimos cargar el catálogo
                    </h2>

                    <p>
                        {error}
                    </p>

                    <button
                        type="button"
                        onClick={() =>
                            window.location.reload()
                        }
                    >
                        Reintentar
                    </button>

                </div>

            </div>
        );
    }


    /* =====================================================
       CATÁLOGO
       ===================================================== */

    return (
        <div className="catalogo-page">


            {/* =================================================
                NAVBAR
               ================================================= */}

            <nav className="catalogo-navbar">

                <div className="catalogo-navbar-brand">

                    <div className="catalogo-navbar-logo">
                        P360
                    </div>

                    <span>
                        Pedidos360
                    </span>

                </div>


                <div className="catalogo-navbar-actions">

                    <button
                        type="button"
                        onClick={onBack}
                    >
                        Inicio
                    </button>

                    <button
                        type="button"
                        onClick={onLogout}
                    >
                        Cerrar sesión
                    </button>

                </div>

            </nav>


            {/* =================================================
                HERO
               ================================================= */}

            <section className="catalogo-hero">

                <div className="catalogo-hero-content">

                    <span className="catalogo-eyebrow">
                        PEDIDOS360
                    </span>

                    <h1>
                        Catálogo
                    </h1>

                    <p>
                        Explora nuestro equipamiento
                        de Airsoft.
                        Encuentra tu próximo loadout.
                    </p>

                </div>


                <div className="catalogo-hero-decoration">

                    <span></span>
                    <span></span>
                    <span></span>

                </div>

            </section>


            {/* =================================================
                FILTROS
               ================================================= */}

            <section className="catalogo-filtros">


                {/* BUSCADOR */}

                <div className="catalogo-search">

                    <span className="search-icon">
                        ⌕
                    </span>

                    <input
                        type="text"
                        placeholder="Buscar por nombre, SKU o descripción..."
                        value={busqueda}
                        onChange={(e) =>
                            setBusqueda(
                                e.target.value,
                            )
                        }
                    />

                    {busqueda && (

                        <button
                            className="search-clear"
                            type="button"
                            onClick={() =>
                                setBusqueda('')
                            }
                        >
                            ×
                        </button>

                    )}

                </div>


                {/* CATEGORÍAS */}

                <div className="categorias">

                    {CATEGORIAS.map(
                        (categoria) => (

                            <button
                                key={categoria}
                                type="button"
                                className={
                                    categoriaSeleccionada === categoria
                                        ? 'categoria-btn activa'
                                        : 'categoria-btn'
                                }
                                onClick={() =>
                                    setCategoriaSeleccionada(
                                        categoria,
                                    )
                                }
                            >
                                {categoria}
                            </button>

                        ),
                    )}

                </div>

            </section>


            {/* =================================================
                RESULTADOS
               ================================================= */}

            <section className="catalogo-resultados">


                <div className="resultados-header">

                    <div>

                        <span className="resultados-label">
                            CATÁLOGO
                        </span>

                        <h2>
                            {categoriaSeleccionada === 'Todos'
                                ? 'Todos los productos'
                                : categoriaSeleccionada}
                        </h2>

                    </div>


                    <span className="contador-productos">
                        {productosFiltrados.length}{' '}
                        productos
                    </span>

                </div>


                {/* =================================================
                    PRODUCTOS
                   ================================================= */}

                {productosFiltrados.length === 0 ? (

                    <div className="sin-resultados">

                        <div className="sin-resultados-icon">
                            ◌
                        </div>

                        <h3>
                            No encontramos productos
                        </h3>

                        <p>
                            Prueba con otra categoría
                            o término de búsqueda.
                        </p>

                    </div>

                ) : (

                    <div className="productos-grid">

                        {productosFiltrados.map(
                            (producto) => (

                                <article
                                    className="producto-card"
                                    key={producto.sku}
                                >


                                    {/* IMAGEN */}

                                    <div className="producto-imagen">

                                        {producto.imagenes?.length > 0 ? (

                                            <img
                                                src={
                                                    producto.imagenes[0]
                                                }
                                                alt={
                                                    producto.nombre
                                                }
                                            />

                                        ) : (

                                            <div className="imagen-placeholder">
                                                AIRSOFT
                                            </div>

                                        )}


                                        <span className="producto-categoria">
                                            {
                                                producto.categoria
                                            }
                                        </span>

                                    </div>


                                    {/* INFORMACIÓN */}

                                    <div className="producto-info">

                                        <span className="producto-sku">
                                            {producto.sku}
                                        </span>

                                        <h3>
                                            {producto.nombre}
                                        </h3>

                                        <p className="producto-descripcion">
                                            {
                                                producto.descripcion
                                            }
                                        </p>


                                        {/* FOOTER PRODUCTO */}

                                        <div className="producto-footer">

                                            <div>

                                                <span className="precio-label">
                                                    Precio
                                                </span>

                                                <strong className="producto-precio">
                                                    {
                                                        formatearPrecio(
                                                            producto.precio,
                                                        )
                                                    }
                                                </strong>

                                            </div>


                                            <div
                                                className={
                                                    producto.stock > 0
                                                        ? 'stock disponible'
                                                        : 'stock agotado'
                                                }
                                            >

                                                <span className="stock-dot"></span>

                                                {producto.stock > 0
                                                    ? `${producto.stock} disponibles`
                                                    : 'Agotado'}

                                            </div>

                                        </div>

                                    </div>

                                </article>

                            ),
                        )}

                    </div>

                )}

            </section>

        </div>
    );
}
