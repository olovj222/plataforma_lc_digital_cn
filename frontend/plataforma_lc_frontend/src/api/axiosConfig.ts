import axios from 'axios'

export const cursoApi = axios.create({
  baseURL: 'http://localhost:8081',
})

export const estudianteApi = axios.create({
  baseURL: 'http://localhost:8080',
})