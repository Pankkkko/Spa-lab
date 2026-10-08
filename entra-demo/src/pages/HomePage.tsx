
import { useEffect, useState } from "react";
import { useMsal } from "@azure/msal-react";

import OrdersModal from "../components/OrdersModal";

// 🔧 DEBUG DE EVALUACIÓN
// Se mantiene comentado para poder activarlo rápidamente.
//
// import TokenDebugButton from "../components/TokenDebugButton";

import { obtenerPedidos } from "../api/pedidosApi";
import type { Pedido } from "../api/pedidosApi";

import "../styles/home.css";

interface HomePageProps {
    isAuthenticated: boolean;
    onLogin: () => void;
    onLogout: () => void;
    onOpenCatalogo: () => void;
}

function HomePage({
    isAuthenticated,
    onLogin,
    onLogout,
    onOpenCatalogo,
}: HomePageProps) {
    const { instance, accounts } = useMsal();

    const [pedidos, setPedidos] = useState<Pedido[]>([]);
    const [cargandoPedidos, setCargandoPedidos] = useState(false);
    const [errorPedidos, setErrorPedidos] = useState("");

    const [mostrarPedidos, setMostrarPedidos] = useState(false);

    const account =
        instance.getActiveAccount() ?? accounts[0];

    useEffect(() => {
        if (!isAuthenticated) {
            setPedidos([]);
            setErrorPedidos("");
            return;
        }

        const cuentaActual =
            instance.getActiveAccount() ?? accounts[0];

        if (!cuentaActual) {
            return;
        }

        async function cargarPedidos() {
            setCargandoPedidos(true);
            setErrorPedidos("");

            try {
                const resultado = await obtenerPedidos(
                    instance,
                    cuentaActual,
                    1,
                );

                setPedidos(resultado);
            } catch (error) {
                console.error(
                    "Error al cargar pedidos:",
                    error,
                );

                setErrorPedidos(
                    error instanceof Error
                        ? error.message
                        : "No se pudieron cargar los pedidos.",
                );
            } finally {
                setCargandoPedidos(false);
            }
        }

        void cargarPedidos();
    }, [isAuthenticated, instance, accounts]);

    function abrirPedidos() {
        setMostrarPedidos(true);
    }

    function cerrarPedidos() {
        setMostrarPedidos(false);
    }

    function irAlCatalogo() {
        window.location.href = "/catalogo";
    }

    const nombreUsuario =
        account?.name ??
        account?.username ??
        "Cliente";

    return (
        <main className="home-page">

            {/* =====================================================
                NAVBAR
               ===================================================== */}

            <nav className="home-navbar">

                <div className="navbar-brand">
                    <div className="navbar-logo">
                        P360
                    </div>

                    <span>
                        Pedidos360
                    </span>
                </div>

                <div className="navbar-links">

                    <button
                        className="navbar-link navbar-link-active"
                        type="button"
                    >
                        Inicio
                    </button>

                    <button
                        className="navbar-link"
                        type="button"
                        onClick={onOpenCatalogo}
                    >
                        Catálogo
                    </button>

                    {isAuthenticated && (
                        <button
                            className="navbar-link"
                            type="button"
                            onClick={abrirPedidos}
                        >
                            Mis pedidos
                        </button>
                    )}

                </div>

                <div className="navbar-account">

                    {isAuthenticated ? (
                        <>
                            <span className="navbar-user">
                                {nombreUsuario}
                            </span>

                            <button
                                className="navbar-logout"
                                type="button"
                                onClick={onLogout}
                            >
                                Cerrar sesión
                            </button>
                        </>
                    ) : (
                        <button
                            className="navbar-login"
                            type="button"
                            onClick={onLogin}
                        >
                            Iniciar sesión
                        </button>
                    )}

                </div>

            </nav>


            {/* =====================================================
                HERO
               ===================================================== */}

            <section className="home-hero">

                <div className="hero-content">

                    <span className="hero-eyebrow">
                        PEDIDOS360
                    </span>

                    <h1>
                        Todo lo que necesitas,
                        <br />
                        en un solo lugar.
                    </h1>

                    <p>
                        Explora nuestro catálogo, descubre
                        nuevos productos y gestiona tus pedidos
                        de manera rápida y sencilla.
                    </p>

                    <div className="hero-actions">

                        <button
                            className="hero-primary-button"
                            type="button"
                            onClick={onOpenCatalogo}
                        >
                            Explorar catálogo
                        </button>

                        {isAuthenticated ? (
                            <button
                                className="hero-secondary-button"
                                type="button"
                                onClick={abrirPedidos}
                            >
                                Ver mis pedidos
                            </button>
                        ) : (
                            <button
                                className="hero-secondary-button"
                                type="button"
                                onClick={onLogin}
                            >
                                Iniciar sesión
                            </button>
                        )}

                    </div>

                </div>


                {/* =================================================
                    ILUSTRACIÓN / DECORACIÓN
                   ================================================= */}

                <div className="hero-decoration">

                    <div className="hero-circle hero-circle-large">
                        <span>✦</span>
                    </div>

                    <div className="hero-circle hero-circle-small">
                        <span>+</span>
                    </div>

                    <div className="hero-product-card">

                        <div className="hero-product-icon">
                            ★
                        </div>

                        <div>
                            <span>
                                PEDIDOS360
                            </span>

                            <strong>
                                Compra fácil
                            </strong>
                        </div>

                    </div>

                </div>

            </section>


            {/* =====================================================
                BIENVENIDA DEL USUARIO
               ===================================================== */}

            {isAuthenticated && account && (
                <section className="home-user-section">

                    <div>
                        <span className="section-label">
                            TU CUENTA
                        </span>

                        <h2>
                            Bienvenido/a, {nombreUsuario}
                        </h2>

                        <p>
                            Revisa tus pedidos o continúa
                            explorando nuestro catálogo.
                        </p>
                    </div>

                    <button
                        className="section-button"
                        type="button"
                        onClick={abrirPedidos}
                    >
                        Mis pedidos
                    </button>

                </section>
            )}


            {/* =====================================================
                TARJETAS DE ACCESO
               ===================================================== */}

            <section className="home-features">

                <article className="feature-card">

                    <div className="feature-icon">
                        ◈
                    </div>

                    <span className="section-label">
                        CATÁLOGO
                    </span>

                    <h2>
                        Encuentra lo que buscas
                    </h2>

                    <p>
                        Explora nuestros productos y descubre
                        nuevas opciones disponibles.
                    </p>

                    <button
                        type="button"
                        onClick={irAlCatalogo}
                    >
                        Ver catálogo →
                    </button>

                </article>


                <article className="feature-card">

                    <div className="feature-icon">
                        ✓
                    </div>

                    <span className="section-label">
                        PEDIDOS
                    </span>

                    <h2>
                        Sigue tus compras
                    </h2>

                    <p>
                        Consulta el estado y los detalles
                        de tus pedidos realizados.
                    </p>

                    <button
                        type="button"
                        onClick={
                            isAuthenticated
                                ? abrirPedidos
                                : onLogin
                        }
                    >
                        {isAuthenticated
                            ? "Ver mis pedidos →"
                            : "Iniciar sesión →"}
                    </button>

                </article>


                <article className="feature-card">

                    <div className="feature-icon">
                        ★
                    </div>

                    <span className="section-label">
                        PEDIDOS360
                    </span>

                    <h2>
                        Una experiencia simple
                    </h2>

                    <p>
                        Una plataforma pensada para que
                        comprar y administrar tus pedidos
                        sea rápido y sencillo.
                    </p>

                </article>

            </section>


            {/* =====================================================
                DEBUG DE TOKEN
               =====================================================

                🔧 NO ELIMINAR.

                Para activar el botón durante la evaluación:

                1. Descomentar el import:

                   import TokenDebugButton
                   from "../components/TokenDebugButton";

                2. Descomentar:

                   {isAuthenticated && <TokenDebugButton />}

               ===================================================== */}

            {/*
                {isAuthenticated && <TokenDebugButton />}
            */}


            {/* =====================================================
                MODAL DE PEDIDOS
               ===================================================== */}

            {isAuthenticated && (
                <OrdersModal
                    isOpen={mostrarPedidos}
                    onClose={cerrarPedidos}
                    pedidos={pedidos}
                    cargando={cargandoPedidos}
                    error={errorPedidos}
                />
            )}

        </main>
    );
}

export default HomePage;
