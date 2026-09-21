import type { Configuration } from '@azure/msal-browser';

export const msalConfig: Configuration = {
  auth: {
    clientId: import.meta.env.VITE_SPA_CLIENT_ID,
    authority:
      'https://login.microsoftonline.com/' +
      import.meta.env.VITE_ENTRA_TENANT_ID,
    redirectUri: window.location.origin + '/redirect.html',
    postLogoutRedirectUri: window.location.origin,
  },
  cache: {
    cacheLocation: 'sessionStorage',
  },
};

export const tokenRequest = {
  scopes: [
    'api://fa47563a-a68d-4d29-93c1-64bbfab52085/pedidos.read',
    'api://fa47563a-a68d-4d29-93c1-64bbfab52085/pedidos.write',
  ],
};