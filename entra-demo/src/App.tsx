
import { useEffect, useState } from 'react';
import { useIsAuthenticated, useMsal } from '@azure/msal-react';

import LoginPage from './pages/LoginPage';
import HomePage from './pages/HomePage';
import AdminPage from './pages/AdminPage';
import CatalogoPage from './pages/CatalogoPage';
import PerfilClientePage from './pages/PerfilClientePage';

import {
    ClienteApiError,
    obtenerMiPerfil,
} from './api/clienteApi';

import { obtenerRolUsuario } from './roles';
import type { UserRole } from './roles';

type EstadoPerfil =
    | 'no-aplica'
    | 'cargando'
    | 'completo'
    | 'requiere-registro'
    | 'error';

function App() {
    const isAuthenticated = useIsAuthenticated();
    const { instance, accounts } = useMsal();

    const [showLogin, setShowLogin] = useState(false);
    const [showCatalogo, setShowCatalogo] = useState(false);

    const [userRole, setUserRole] = useState<UserRole>(null);
    const [cargandoRol, setCargandoRol] = useState(false);

    const [estadoPerfil, setEstadoPerfil] =
        useState<EstadoPerfil>('no-aplica');

    const [intentoPerfil, setIntentoPerfil] = useState(0);

    function handleLogin() {
        setShowLogin(true);
        setShowCatalogo(false);
    }

    function handleLogout() {
        setUserRole(null);
        setEstadoPerfil('no-aplica');
        setShowLogin(false);
        setShowCatalogo(false);

        void instance.logoutRedirect();
    }

    function handleBackToHome() {
        setShowLogin(false);
        setShowCatalogo(false);
    }

    function handleOpenCatalogo() {
        setShowCatalogo(true);
        setShowLogin(false);
    }

    useEffect(() => {
        if (!isAuthenticated) {
            setUserRole(null);
            setCargandoRol(false);
            return;
        }

        const account =
            instance.getActiveAccount() ?? accounts[0];

        if (!account) {
            setUserRole(null);
            setCargandoRol(false);
            return;
        }

        let cancelado = false;

        async function cargarRol() {
            setCargandoRol(true);

            try {
                const rol = await obtenerRolUsuario(
                    instance,
                    account!,
                );

                if (!cancelado) {
                    setUserRole(rol);
                }
            } catch (error) {
                console.error(
                    'Error al obtener el rol del usuario:',
                    error,
                );

                if (!cancelado) {
                    setUserRole(null);
                }
            } finally {
                if (!cancelado) {
                    setCargandoRol(false);
                }
            }
        }

        void cargarRol();

        return () => {
            cancelado = true;
        };
    }, [isAuthenticated, instance, accounts]);

    useEffect(() => {
        if (!isAuthenticated || userRole !== 'Cliente') {
            setEstadoPerfil('no-aplica');
            return;
        }

        const account =
            instance.getActiveAccount() ?? accounts[0];

        if (!account) {
            setEstadoPerfil('error');
            return;
        }

        let cancelado = false;

        async function comprobarPerfil() {
            setEstadoPerfil('cargando');

            try {
                await obtenerMiPerfil(instance, account!);

                if (!cancelado) {
                    setEstadoPerfil('completo');
                }
            } catch (error) {
                if (cancelado) {
                    return;
                }

                if (
                    error instanceof ClienteApiError &&
                    (error.status === 403 || error.status === 404)
                ) {
                    setEstadoPerfil('requiere-registro');
                    return;
                }

                console.error(
                    'Error al comprobar el perfil del cliente:',
                    error,
                );

                setEstadoPerfil('error');
            }
        }

        void comprobarPerfil();

        return () => {
            cancelado = true;
        };
    }, [
        isAuthenticated,
        userRole,
        instance,
        accounts,
        intentoPerfil,
    ]);

    // El catálogo sigue siendo accesible sin iniciar sesión.
    if (showCatalogo) {
        return (
            <CatalogoPage
                isAuthenticated={isAuthenticated}
                onBack={handleBackToHome}
                onLogin={handleLogin}
                onLogout={handleLogout}
            />
        );
    }

    if (!isAuthenticated && showLogin) {
        return <LoginPage onBack={handleBackToHome} />;
    }

    if (!isAuthenticated) {
        return (
            <HomePage
                isAuthenticated={false}
                onLogin={handleLogin}
                onLogout={handleLogout}
                onOpenCatalogo={handleOpenCatalogo}
            />
        );
    }

    if (cargandoRol) {
        return (
            <main>
                <p>Verificando permisos...</p>
            </main>
        );
    }

    if (userRole === 'Admin') {
        return <AdminPage onLogout={handleLogout} />;
    }

    if (userRole === 'Cliente') {
        if (estadoPerfil === 'cargando' ||
            estadoPerfil === 'no-aplica') {
            return (
                <main className="perfil-cliente-page">
                    <section className="perfil-cliente-card">
                        <h1>Verificando tu perfil...</h1>
                        <p>
                            Estamos comprobando los datos de tu cuenta.
                        </p>
                    </section>
                </main>
            );
        }

        if (estadoPerfil === 'requiere-registro') {
            const account =
                instance.getActiveAccount() ?? accounts[0];

            if (!account) {
                return (
                    <main>
                        <h1>No se encontró la cuenta activa</h1>
                        <button
                            type="button"
                            onClick={handleLogout}
                        >
                            Cerrar sesión
                        </button>
                    </main>
                );
            }

            return (
                <PerfilClientePage
                    instance={instance}
                    account={account}
                    onPerfilCreado={() => {
                        setEstadoPerfil('completo');
                    }}
                    onLogout={handleLogout}
                />
            );
        }

        if (estadoPerfil === 'error') {
            return (
                <main className="perfil-cliente-page">
                    <section className="perfil-cliente-card">
                        <h1>No pudimos verificar tu perfil</h1>

                        <p>
                            No pudimos consultar tus datos.
                            Comprueba que el BFF y ms-clientes
                            estén funcionando e inténtalo otra vez.
                        </p>

                        <button
                            className="perfil-cliente-submit"
                            type="button"
                            onClick={() => {
                                setIntentoPerfil((actual) => actual + 1);
                            }}
                        >
                            Reintentar
                        </button>

                        <button
                            className="perfil-cliente-logout"
                            type="button"
                            onClick={handleLogout}
                        >
                            Cerrar sesión
                        </button>
                    </section>
                </main>
            );
        }

        return (
            <HomePage
                isAuthenticated={true}
                onLogin={handleLogin}
                onLogout={handleLogout}
                onOpenCatalogo={handleOpenCatalogo}
            />
        );
    }

    return (
        <main>
            <h1>Acceso no disponible</h1>

            <p>
                No se encontró un rol válido para este usuario.
            </p>

            <button onClick={handleLogout} type="button">
                Cerrar sesión
            </button>
        </main>
    );
}

export default App;
