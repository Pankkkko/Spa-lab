
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


  /* =========================================================
     NAVEGACIÓN
     ========================================================= */

  function handleLogin() {
    setShowLogin(true);
    setShowCatalogo(false);
  }

  function handleLogout() {
    setUserRole(null);
    setShowLogin(false);
    setShowCatalogo(false);

    instance.logoutRedirect();
  }

  function handleBackToHome() {
    setShowLogin(false);
    setShowCatalogo(false);
  }

  function handleOpenCatalogo() {
    setShowCatalogo(true);
    setShowLogin(false);
  }


  /* =========================================================
     OBTENER ROL DEL USUARIO
     ========================================================= */

  useEffect(() => {
    if (!isAuthenticated) {
      setUserRole(null);
      return;
    }

    const account =
      instance.getActiveAccount() ?? accounts[0];

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


  /* =========================================================
     USUARIO AUTENTICADO
     ========================================================= */

  if (isAuthenticated) {

    if (cargandoRol) {

      return (
        <main>
          <p>
            Verificando permisos...
          </p>
        </main>
      );

    }


    /* =====================================================
       ADMIN
       ===================================================== */

    if (userRole === 'Admin') {

      return (
        <AdminPage
          onLogout={handleLogout}
        />
      );

    }


    /* =====================================================
       CLIENTE
       ===================================================== */

    if (userRole === 'Cliente') {

      /*
       * Si el cliente seleccionó "Catálogo",
       * mostramos CatalogoPage.
       */

      if (showCatalogo) {

        return (
          <CatalogoPage
            onBack={handleBackToHome}
            onLogout={handleLogout}
          />
        );

      }


      /*
       * Home normal del cliente.
       */

      return (
        <HomePage
          isAuthenticated={true}
          onLogin={handleLogin}
          onLogout={handleLogout}
          onOpenCatalogo={handleOpenCatalogo}
        />
      );

    }


    /* =====================================================
       ROL DESCONOCIDO
       ===================================================== */

    return (
      <main>
        <h1>
          Acceso no disponible
        </h1>

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


  /* =========================================================
     USUARIO NO AUTENTICADO
     ========================================================= */

  /*
   * Actualmente el catálogo requiere una cuenta autenticada
   * porque obtiene los productos utilizando el token de MSAL.
   *
   * Por eso no permitimos entrar directamente al catálogo
   * estando deslogueado.
   */

  if (showCatalogo) {
    setShowCatalogo(false);
  }


  /* =========================================================
     LOGIN
     ========================================================= */

  if (showLogin) {

    return (
      <LoginPage
        onBack={handleBackToHome}
      />
    );

  }


  /* =========================================================
     HOME PÚBLICO
     ========================================================= */

  return (
    <HomePage
      isAuthenticated={false}
      onLogin={handleLogin}
      onLogout={handleLogout}
      onOpenCatalogo={handleOpenCatalogo}
    />
  );
}

export default App;
