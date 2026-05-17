import axios from 'axios'
import keycloak from '../keycloak'

export const cursoApi = axios.create({
  baseURL: 'http://localhost:8081',
})

export const estudianteApi = axios.create({
  baseURL: 'http://localhost:8080',
})

// Interceptor que agrega el token JWT en cada request
const authInterceptor = (config: any) => {
  if (keycloak.token) {
    config.headers.Authorization = `Bearer ${keycloak.token}`
  }
  return config
}

cursoApi.interceptors.request.use(authInterceptor)
estudianteApi.interceptors.request.use(authInterceptor)