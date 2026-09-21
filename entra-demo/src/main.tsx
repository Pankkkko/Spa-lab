import { createRoot } from 'react-dom/client';
import { PublicClientApplication } from '@azure/msal-browser';
import { MsalProvider } from '@azure/msal-react';
import { msalConfig } from './authConfig';
import App from './App';

const msalInstance = new PublicClientApplication(msalConfig);

// 👇 EXPONER MSAL GLOBALMENTE (solo para desarrollo)
(window as any).msalInstance = msalInstance;

msalInstance.initialize().then(() => {
  const rootElement = document.getElementById('root');
  if (!rootElement) {
    throw new Error('Root element not found');
  }

  createRoot(rootElement).render(
    <MsalProvider instance={msalInstance}>
      <App />
    </MsalProvider>,
  );
});