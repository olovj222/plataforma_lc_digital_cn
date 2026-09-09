import axios, { InternalAxiosRequestConfig } from 'axios'
import keycloak from '../keycloak'

const BASE_URL = 'http://localhost:8085'

// Interceptor asíncrono que auto-renueva el token antes de cada petición
const authInterceptor = async (config: InternalAxiosRequestConfig) => {
  if (keycloak.authenticated) {
    try {
      // Si el token vence en menos de 30 segundos, se refresca automáticamente
      await keycloak.updateToken(30)
      config.headers.Authorization = `Bearer ${keycloak.token}`
    } catch (error) {
      console.error('Error al renovar el token de sesión:', error)
      keycloak.login() // Redirige al login si la sesión caducó por completo
    }
  }
  return config
}

// Función helper para instanciar Axios con el interceptor aplicado
const createApiInstance = () => {
  const instance = axios.create({ baseURL: BASE_URL })
  instance.interceptors.request.use(authInterceptor)
  return instance
}

// Exportación de instancias (mantienen compatibilidad con tus imports actuales)
export const cursoApi = createApiInstance()
export const claseApi = createApiInstance()
export const estudianteApi = createApiInstance()
export const asistenciaApi = createApiInstance()
export const evaluacionesApi = createApiInstance()
export const justificativosApi = createApiInstance()
export const anotacionesApi = createApiInstance()