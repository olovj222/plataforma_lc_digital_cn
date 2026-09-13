import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.tsx'
import { MsalProvider } from '@azure/msal-react';
import { msalInstance } from './msalConfig.ts';

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <MsalProvider instance={msalInstance}>
      <App />
    </MsalProvider>
  </React.StrictMode>
);