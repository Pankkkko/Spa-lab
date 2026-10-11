
import { useEffect, useState } from 'react';
import { useIsAuthenticated, useMsal } from '@azure/msal-react';

import LoginPage from './pages/LoginPage';
import HomePage from './pages/HomePage';
import AdminPage from './pages/AdminPage';
import CatalogoPage from './pages/CatalogoPage';

import { obtenerRolUsuario } from './roles';
import type { UserRole } from './roles';

function App() {
    const isAuthenticated = useIsAuthenticated();
    const { instance, accounts } = useMsal();

    const [showLogin, setShowLogin] = useState(false);
    const [showCatalogo, setShowCatalogo] = useState(false);

    const [userRole, setUserRole] = useState<UserRole>(null);
    const [cargandoRol, setCargandoRol] = useState(false);

    function handleLogin() {
        setShowLogin(true);
        setShowCatalogo(false);
    }

    function handleLogout() {
        setUserRole(null);
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

    // Mostrar el catálogo es independiente de la autenticación.
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

    // La pantalla de login solo aparece si el visitante la solicita.
    if (!isAuthenticated && showLogin) {
        return <LoginPage onBack={handleBackToHome} />;
    }

    // Vista pública para visitantes sin sesión.
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

    // Mientras se verifica el rol, no mostramos páginas privadas.
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
