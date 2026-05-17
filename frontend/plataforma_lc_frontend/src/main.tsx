import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.tsx'
import keycloak from './keycloak'

keycloak.init({
   onLoad: 'login-required',
  checkLoginIframe: false }).then((authenticated) => {
  if (authenticated) {
    ReactDOM.createRoot(document.getElementById('root')!).render(
      <React.StrictMode>
        <App />
      </React.StrictMode>
    )
  } else {
    keycloak.login()
  }
})