import { useState } from 'react';
import { useMsal } from '@azure/msal-react';
import { InteractionStatus } from '@azure/msal-browser';
import { tokenRequest } from './authConfig';
import { obtenerToken } from './token';
import { consultarApi, consultarBff } from './api';

export default function App() {
  const { instance, accounts, inProgress } = useMsal();
  const [salida, setSalida] = useState('');
  const [salidaParte2, setSalidaParte2] = useState('');
  const [ocupado, setOcupado] = useState(false);

  const account = instance.getActiveAccount() ?? accounts[0];
  const bloqueado = ocupado || inProgress !== InteractionStatus.None;
  const apiLista = Boolean(import.meta.env.VITE_API_BASE_URL);
  const bffListo = Boolean(import.meta.env.VITE_BFF_BASE_URL);

  async function ejecutar(action: () => Promise<void>) {
    setOcupado(true);
    setSalida('');
    try {
      await action();
    } catch (error) {
      setSalida(error instanceof Error ? error.message : String(error));
    } finally {
      setOcupado(false);
    }
  }

  async function entrar() {
    const result = await instance.loginPopup({
      ...tokenRequest,
      prompt: 'select_account',
    });
    instance.setActiveAccount(result.account);
  }

  async function probarToken() {
    if (!account) return;
    const token = await obtenerToken(instance, account);
    if (!token.accessToken) {
      throw new Error('No se obtuvo access token');
    }
    setSalida(
      'Token de API obtenido. Vence: ' +
        (token.expiresOn?.toLocaleString() ?? 'Consultar metadatos'),
    );
  }

  async function consultar() {
    if (account) {
      setSalida(await consultarApi(instance, account));
    }
  }

  async function consultarBffHandler() {
    if (!account) return;
    setOcupado(true);
    setSalidaParte2('');
    try {
      const resultado = await consultarBff(instance, account);
      setSalidaParte2(resultado);
    } catch (error) {
      setSalidaParte2(error instanceof Error ? error.message : String(error));
    } finally {
      setOcupado(false);
    }
  }

  async function salir() {
    if (account) {
      await instance.logoutPopup({ account });
    }
  }

  return (
    <main style={{ maxWidth: 850, margin: '40px auto', padding: 20, fontFamily: 'Arial' }}>
      <h1>Demo Entra ID y API Gateway</h1>

      <p>Estado MSAL: {inProgress}</p>
      <p>Ocupado: {String(ocupado)}</p>

      {!account ? (
        <button disabled={bloqueado} onClick={() => void ejecutar(entrar)}>
          Iniciar sesión
        </button>
      ) : (
        <>
          <p>Sesión: {account.username}</p>

          <hr />
          <h2>Comprobación Parte 1</h2>
          <p>Autenticación con Microsoft Entra ID y acceso a la API protegida.</p>

          <button disabled={bloqueado} onClick={() => void ejecutar(probarToken)}>
            Obtener token API
          </button>{' '}
          <button disabled={bloqueado || !apiLista} onClick={() => void ejecutar(consultar)}>
            Consultar API
          </button>

          <pre
            style={{
              whiteSpace: 'pre-wrap',
              overflowWrap: 'anywhere',
              backgroundColor: '#f5f5f5',
              padding: 15,
              marginTop: 15,
            }}
          >
            {salida}
          </pre>

          <hr />
          <h2>Comprobación Parte 2</h2>
          <p>Flujo completo React → BFF → ms-clientes.</p>

          <button disabled={bloqueado || !bffListo} onClick={() => void consultarBffHandler()}>
            Consultar BFF + ms-clientes
          </button>

          <pre
            style={{
              whiteSpace: 'pre-wrap',
              overflowWrap: 'anywhere',
              backgroundColor: '#f5f5f5',
              padding: 15,
              marginTop: 15,
            }}
          >
            {salidaParte2}
          </pre>

          <hr />
          <button disabled={bloqueado} onClick={() => void ejecutar(salir)}>
            Cerrar sesión
          </button>
        </>
      )}
    </main>
  );
}