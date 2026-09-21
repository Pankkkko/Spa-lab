import { useState } from 'react';
import { useMsal } from '@azure/msal-react';
import { tokenRequest } from '../authConfig';

export default function TokenDebugButton() {
  const { instance, accounts } = useMsal();
  const [mensaje, setMensaje] = useState('');

  async function copiarToken() {
    try {
      const account = instance.getActiveAccount() ?? accounts[0];
      if (!account) {
        setMensaje('❌ No hay cuenta activa. Inicia sesión primero.');
        return;
      }

      const result = await instance.acquireTokenSilent({
        ...tokenRequest,
        account,
      });

      await navigator.clipboard.writeText(result.accessToken);
      setMensaje(`✅ Token copiado (${result.accessToken.length} caracteres)`);
    } catch (error) {
      console.error('Error al obtener token:', error);
      setMensaje(
        '❌ ' + (error instanceof Error ? error.message : String(error)),
      );
    }
  }

  return (
    <div style={{ marginTop: 20, padding: 10, border: '1px dashed #888' }}>
      <button onClick={copiarToken} style={{ padding: '8px 16px', cursor: 'pointer' }}>
        📋 Copiar token al portapapeles
      </button>
      {mensaje && <p style={{ marginTop: 8 }}>{mensaje}</p>}
    </div>
  );
}