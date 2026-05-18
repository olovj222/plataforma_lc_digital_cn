import axios from 'axios'
import keycloak from '../keycloak'

export const cursoApi = axios.create({
  baseURL: 'http://localhost:8085',
})

export const estudianteApi = axios.create({
  baseURL: 'http://localhost:8085',
})

export const asistenciaApi = axios.create({
  baseURL: 'http://localhost:8085',
})

export const evaluacionesApi = axios.create({
  baseURL: 'http://localhost:8085',
})

// Interceptor que agrega el token JWT en cada request
const authInterceptor = async (config: any) => {
  try {
    await keycloak.updateToken(30) // refresca si expira en menos de 30 segundos
  } catch (e) {
    keycloak.login() // si no puede refrescar, redirige al login
  }
  if (keycloak.token) {
    config.headers.Authorization = `Bearer ${keycloak.token}`
  }
  return config
}

cursoApi.interceptors.request.use(authInterceptor)
estudianteApi.interceptors.request.use(authInterceptor)
asistenciaApi.interceptors.request.use(authInterceptor)
evaluacionesApi.interceptors.request.use(authInterceptor)