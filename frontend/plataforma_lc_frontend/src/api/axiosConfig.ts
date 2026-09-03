import axios from 'axios'
import keycloak from '../keycloak'

export const cursoApi = axios.create({
  baseURL: 'http://localhost:8085',
})

export const claseApi = axios.create({
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

export const justificativosApi = axios.create({
  baseURL: 'http://localhost:8085',
})

export const anotacionesApi = axios.create({
  baseURL: 'http://localhost:8085',
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
asistenciaApi.interceptors.request.use(authInterceptor)
claseApi.interceptors.request.use(authInterceptor)
evaluacionesApi.interceptors.request.use(authInterceptor)
justificativosApi.interceptors.request.use(authInterceptor)
anotacionesApi.interceptors.request.use(authInterceptor)