import { useEffect, useState } from "react";
import { useMsal } from "@azure/msal-react";

import HomeWelcome from "../components/HomeWelcome";
import HomeAction from "../components/HomeAction";
import OrdersModal from "../components/OrdersModal";

// 🔧 Debug de evaluación.
// Se mantiene importado para poder reactivarlo rápidamente más adelante.
// import TokenDebugButton from "../components/TokenDebugButton";

import { obtenerPedidos } from "../api/pedidosApi";
import type { Pedido } from "../api/pedidosApi";

import "../styles/home.css";

interface HomePageProps {
    isAuthenticated: boolean;
    onLogin: () => void;
    onLogout: () => void;
}

function HomePage({
    isAuthenticated,
    onLogin,
    onLogout,
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

    const nombreUsuario =
        account?.name ??
        account?.username ??
        "Cliente";

    const username =
        account?.username ?? "";

    return (
        <main className="home-page">
            <section className="home-card">

                <HomeWelcome />

                {isAuthenticated && account && (
                    <section className="user-welcome">
                        <span className="user-welcome-label">
                            Bienvenido/a
                        </span>

                        <h2>
                            {nombreUsuario}
                        </h2>

                        {username && (
                            <p>
                                {username}
                            </p>
                        )}
                    </section>
                )}

                <HomeAction
                    isAuthenticated={isAuthenticated}
                    onLogin={onLogin}
                    onLogout={onLogout}
                />

                {/*
                    🔧 Botón temporal de debug.

                    NO ELIMINAR.
                    Para la evaluación 2, descomentar:

                    import TokenDebugButton from "../components/TokenDebugButton";

                    y luego:

                    {isAuthenticated && <TokenDebugButton />}
                */}

                {isAuthenticated && (
                    <section className="orders-preview">
                        <div className="orders-preview-content">
                            <span className="orders-preview-label">
                                Tu cuenta
                            </span>

                            <h2>
                                Mis pedidos
                            </h2>

                            <p>
                                Consulta el estado y los detalles
                                de tus compras.
                            </p>
                        </div>

                        <button
                            className="orders-open-button"
                            onClick={abrirPedidos}
                            type="button"
                        >
                            Ver mis pedidos
                        </button>
                    </section>
                )}
            </section>

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

