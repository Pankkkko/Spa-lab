import { useEffect, useState } from "react";
import { useMsal } from "@azure/msal-react";

import HomeWelcome from "../components/HomeWelcome";
import HomeAction from "../components/HomeAction";
import TokenDebugButton from "../components/TokenDebugButton";
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

    useEffect(() => {
        if (!isAuthenticated) {
            setPedidos([]);
            setErrorPedidos("");
            return;
        }

        const account = instance.getActiveAccount() ?? accounts[0];

        if (!account) {
            return;
        }

        async function cargarPedidos() {
            setCargandoPedidos(true);
            setErrorPedidos("");

            try {
                const resultado = await obtenerPedidos(
                    instance,
                    account,
                    1,
                );

                setPedidos(resultado);
            } catch (error) {
                console.error("Error al cargar pedidos:", error);

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

    return (
        <main className="home-page">
            <section className="home-card">
                <HomeWelcome />

                <HomeAction
                    isAuthenticated={isAuthenticated}
                    onLogin={onLogin}
                    onLogout={onLogout}
                />

                {/* 🔧 Botón temporal para copiar el token al portapapeles */}
                {isAuthenticated && <TokenDebugButton />}

                {isAuthenticated && (
                    <section className="orders-section">
                        <div className="orders-header">
                            <h2>Mis pedidos</h2>

                            <p>
                                Estos son los pedidos asociados a tu cuenta.
                            </p>
                        </div>

                        {cargandoPedidos && (
                            <p className="orders-message">
                                Cargando pedidos...
                            </p>
                        )}

                        {errorPedidos && (
                            <p className="orders-error">
                                {errorPedidos}
                            </p>
                        )}

                        {!cargandoPedidos &&
                            !errorPedidos &&
                            pedidos.length === 0 && (
                                <p className="orders-message">
                                    No tienes pedidos registrados.
                                </p>
                            )}

                        {!cargandoPedidos &&
                            !errorPedidos &&
                            pedidos.length > 0 && (
                                <div className="orders-list">
                                    {pedidos.map((pedido) => (
                                        <article
                                            className="order-card"
                                            key={pedido.id}
                                        >
                                            <div className="order-card-header">
                                                <div>
                                                    <span className="order-label">
                                                        Pedido
                                                    </span>

                                                    <h3>
                                                        #{pedido.id}
                                                    </h3>
                                                </div>

                                                <span
                                                    className={`order-status status-${pedido.estado.toLowerCase()}`}
                                                >
                                                    {pedido.estado}
                                                </span>
                                            </div>

                                            <div className="order-info">
                                                <span>
                                                    Fecha: {pedido.fecha}
                                                </span>

                                                <strong>
                                                    ${pedido.total.toLocaleString(
                                                        "es-CL",
                                                    )}
                                                </strong>
                                            </div>

                                            <div className="order-details">
                                                {pedido.detalles.map(
                                                    (detalle, index) => (
                                                        <div
                                                            className="order-detail"
                                                            key={`${pedido.id}-${index}`}
                                                        >
                                                            <span>
                                                                {detalle.producto}
                                                                {" x"}
                                                                {detalle.cantidad}
                                                            </span>

                                                            <span>
                                                                $
                                                                {detalle.subtotal.toLocaleString(
                                                                    "es-CL",
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
                )}
            </section>
        </main>
    );
}

export default HomePage;