import { anotacionesApi } from './axiosConfig'
import type { Anotacion, TipoAnotacion } from '../types/Anotacion'

export const getAnotacionesByCurso = async (cursoId: number): Promise<Anotacion[]> => {
  const res = await anotacionesApi.get(`/anotaciones/curso/${cursoId}`)
  return res.data
}

export const getAnotacionesByEstudiante = async (estudianteId: number): Promise<Anotacion[]> => {
  const res = await anotacionesApi.get(`/anotaciones/estudiante/${estudianteId}`)
  return res.data
}

export const getAnotacionesByEstudianteYTipo = async (
  estudianteId: number,
  tipo: TipoAnotacion
): Promise<Anotacion[]> => {
  const res = await anotacionesApi.get(`/anotaciones/estudiante/${estudianteId}/tipo/${tipo}`)
  return res.data
}

export const crearAnotacion = async (anotacion: Anotacion): Promise<Anotacion> => {
  const res = await anotacionesApi.post('/anotaciones', anotacion)
  return res.data
}

export const eliminarAnotacion = async (id: number): Promise<void> => {
  await anotacionesApi.delete(`/anotaciones/${id}`)
}