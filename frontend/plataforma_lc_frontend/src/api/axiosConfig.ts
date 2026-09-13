import axios, { InternalAxiosRequestConfig } from 'axios'
import { InteractionRequiredAuthError } from '@azure/msal-browser'
import { msalInstance } from '../msalConfig'

const BASE_URL = 'http://localhost:8085'

// Scopes mínimos solo para mantener la sesión renovable.
// Si más adelante expones un scope propio de API en Entra ID, agrégalo aquí.
const tokenRequest = {
  scopes: ['70af68d7-f0b7-4897-9d64-4a0b0791ca70/Curso.Create'],
}

// Interceptor asíncrono: obtiene el token vigente (o lo renueva) antes de cada petición
const authInterceptor = async (config: InternalAxiosRequestConfig) => {
  const account = msalInstance.getActiveAccount() ?? msalInstance.getAllAccounts()[0]

  if (account) {
    try {
      const response = await msalInstance.acquireTokenSilent({
        ...tokenRequest,
        account,
      })
      // Usamos el ID Token: es el mismo que ya usa App.tsx para leer los roles
        config.headers.Authorization = `Bearer ${response.accessToken}`
    } catch (error) {
      if (error instanceof InteractionRequiredAuthError) {
        // La sesión requiere reautenticación interactiva (ej. token expirado del todo)
        await msalInstance.acquireTokenRedirect(tokenRequest)
      } else {
        console.error('Error al renovar el token de sesión:', error)
      }
    }
  }

  return config
}

const createApiInstance = () => {
  const instance = axios.create({ baseURL: BASE_URL })
  instance.interceptors.request.use(authInterceptor)
  return instance
}

export const cursoApi = createApiInstance()
export const claseApi = createApiInstance()
export const estudianteApi = createApiInstance()
export const asistenciaApi = createApiInstance()
export const evaluacionesApi = createApiInstance()
export const justificativosApi = createApiInstance()
export const anotacionesApi = createApiInstance()