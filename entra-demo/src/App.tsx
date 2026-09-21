import { useEffect, useState } from 'react';
import { useIsAuthenticated, useMsal } from '@azure/msal-react';

import LoginPage from './pages/LoginPage';
import HomePage from './pages/HomePage';
import AdminPage from './pages/AdminPage';

import { obtenerRolUsuario } from './roles';
import type { UserRole } from './roles';

function App() {
  const isAuthenticated = useIsAuthenticated();
  const { instance, accounts } = useMsal();

  const [showLogin, setShowLogin] = useState(false);
  const [userRole, setUserRole] = useState<UserRole>(null);
  const [cargandoRol, setCargandoRol] = useState(false);

  function handleLogin() {
    setShowLogin(true);
  }

  function handleLogout() {
    setUserRole(null);
    instance.logoutRedirect();
  }

  function handleBackToHome() {
    setShowLogin(false);
  }

  useEffect(() => {
    if (!isAuthenticated) {
      setUserRole(null);
      return;
    }

    const account = instance.getActiveAccount() ?? accounts[0];

    if (!account) {
      setUserRole(null);
      return;
    }

    async function cargarRol() {
      setCargandoRol(true);

      try {
        const rol = await obtenerRolUsuario(
          instance,
          account,
        );

        setUserRole(rol);
      } catch (error) {
        console.error(
          'Error al obtener el rol del usuario:',
          error,
        );

        setUserRole(null);
      } finally {
        setCargandoRol(false);
      }
    }

    void cargarRol();
  }, [isAuthenticated, instance, accounts]);

  if (isAuthenticated) {
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
        />
      );
    }

    return (
      <main>
        <h1>Acceso no disponible</h1>

        <p>
          No se encontró un rol válido para este usuario.
        </p>

        <button
          onClick={handleLogout}
          type="button"
        >
          Cerrar sesión
        </button>
      </main>
    );
  }

  if (showLogin) {
    return (
      <LoginPage
        onBack={handleBackToHome}
      />
    );
  }

  return (
    <HomePage
      isAuthenticated={false}
      onLogin={handleLogin}
      onLogout={handleLogout}
    />
  );
}

export default App;

