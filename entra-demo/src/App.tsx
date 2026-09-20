import { useMsal } from '@azure/msal-react';

import LoginPage from './pages/LoginPage';

export default function App() {
  const { instance, accounts } = useMsal();

  const account = instance.getActiveAccount() ?? accounts[0];

  async function cerrarSesion() {
    await instance.logoutPopup({
      account: account ?? undefined,
    });
  }

  if (!account) {
    return <LoginPage />;
  }

  return (
    <main
      style={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        gap: '20px',
        background: '#06131c',
        color: '#f4fffd',
        fontFamily: 'Arial, sans-serif',
      }}
    >
      <h1>Bienvenido a Pedidos360 🌊</h1>

      <p>
        Sesión iniciada como: <strong>{account.username}</strong>
      </p>

      <button
        onClick={() => void cerrarSesion()}
        style={{
          padding: '12px 24px',
          borderRadius: '7px',
          border: '1px solid #20666B',
          background: '#0C3B45',
          color: '#CFF0EA',
          cursor: 'pointer',
          fontSize: '15px',
          fontWeight: 600,
        }}
      >
        Cerrar sesión
      </button>
    </main>
  );
}