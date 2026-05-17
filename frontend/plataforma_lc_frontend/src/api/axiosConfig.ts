import axios from 'axios'

export const asistenciaApi = axios.create({
  baseURL: 'http://localhost:8082',
})

export const cursoApi = axios.create({
  baseURL: 'http://localhost:8081',
})

export const estudianteApi = axios.create({
  baseURL: 'http://localhost:8080',
})