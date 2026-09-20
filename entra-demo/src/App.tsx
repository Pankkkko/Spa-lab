import { useState } from 'react';
import { useIsAuthenticated, useMsal } from '@azure/msal-react';

import LoginPage from './pages/LoginPage';
import HomePage from './pages/HomePage';

function App() {
  const isAuthenticated = useIsAuthenticated();
  const { instance } = useMsal();

  const [showLogin, setShowLogin] = useState(false);

  function handleLogin() {
    setShowLogin(true);
  }

  function handleLogout() {
    instance.logoutRedirect();
  }

  function handleBackToHome() {
    setShowLogin(false);
  }

  // Usuario autenticado → siempre Home
  if (isAuthenticated) {
    return (
      <HomePage
        isAuthenticated={true}
        onLogin={handleLogin}
        onLogout={handleLogout}
      />
    );
  }

  // Usuario no autenticado + eligió Login
  if (showLogin) {
    return (
      <LoginPage
        onBack={handleBackToHome}
      />
    );
  }

  // Primera pantalla
  return (
    <HomePage
      isAuthenticated={false}
      onLogin={handleLogin}
      onLogout={handleLogout}
    />
  );
}

export default App;

