import axios, { InternalAxiosRequestConfig } from 'axios'
import keycloak from '../keycloak'

const BASE_URL = 'http://localhost:8085'

// Interceptor asíncrono que auto-renueva el token antes de cada petición
const authInterceptor = async (config: InternalAxiosRequestConfig) => {
  if (keycloak.authenticated) {
    try {
      await keycloak.updateToken(30)
      config.headers.Authorization = `Bearer ${keycloak.token}`
    } catch (error) {
      console.error('Error al renovar el token de sesión:', error)
      keycloak.login()
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