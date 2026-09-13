
import type { Estudiante } from '../types/Estudiante'
import { estudianteApi } from './axiosConfig'


export const getEstudiantes = async (): Promise<Estudiante[]> => {
  const { data } = await estudianteApi.get('/estudiante')
  return data
}

export const getEstudianteById = async (id: number): Promise<Estudiante> => {
  const { data } = await estudianteApi.get(`/estudiante/${id}`)
  return data
}

export const createEstudiante = async (estudiante: Estudiante): Promise<Estudiante> => {
  const { data } = await estudianteApi.post('/estudiante', estudiante)
  return data
}

export const updateEstudiante = async (id: number, estudiante: Estudiante): Promise<Estudiante> => {
  const { data } = await estudianteApi.put(`/estudiante/${id}`, estudiante)
  return data
}

export const deleteEstudiante = async (id: number): Promise<void> => {
  await estudianteApi.delete(`/estudiante/${id}`)
}