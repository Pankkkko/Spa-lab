import type { AccountInfo, IPublicClientApplication } from '@azure/msal-browser';
import { obtenerToken } from './token';

export async function consultarApi(
  instance: IPublicClientApplication,
  account: AccountInfo,
): Promise<string> {
  const base = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '');
  if (!base) {
    throw new Error('Complete VITE_API_BASE_URL y reinicie Vite');
  }

  const result = await obtenerToken(instance, account);

  const response = await fetch(`${base}/api/data`, {
    headers: {
      Authorization: `Bearer ${result.accessToken}`,
    },
  });

  const body = await response.text();
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}: ${body}`);
  }

  return JSON.stringify(JSON.parse(body), null, 2);
}

export async function consultarBff(
  instance: IPublicClientApplication,
  account: AccountInfo,
): Promise<string> {
  const base = import.meta.env.VITE_BFF_BASE_URL?.replace(/\/$/, '');
  if (!base) {
    throw new Error('Complete VITE_BFF_BASE_URL y reinicie Vite');
  }

  const result = await obtenerToken(instance, account);

  const response = await fetch(`${base}/api/data`, {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${result.accessToken}`,
    },
  });

  const texto = await response.text();
  if (!response.ok) {
    throw new Error(`Error BFF ${response.status}: ${texto}`);
  }

  const data = JSON.parse(texto);
  return JSON.stringify(data, null, 2);
}