import { evaluacionesApi } from './axiosConfig'

export interface Evaluacion {
  id?: number
  nombre: string
  cursoId: number
  estudianteId: number
  calificacion: number
  estudianteNombre?: string
}

export const getEvaluacionesPorCurso = async (cursoId: number): Promise<Evaluacion[]> => {
  const { data } = await evaluacionesApi.get(`/evaluaciones/curso/${cursoId}`)
  return data
}

export const crearEvaluacion = async (evaluacion: Evaluacion): Promise<Evaluacion> => {
  const { data } = await evaluacionesApi.post('/evaluaciones', evaluacion)
  return data
}

export const actualizarNota = async (id: number, nota: number): Promise<Evaluacion> => {
  const { data } = await evaluacionesApi.post(`/evaluaciones/${id}/nota?nota=${nota}`)
  return data
}

export const eliminarEvaluacion = async (id: number): Promise<void> => {
  await evaluacionesApi.delete(`/evaluaciones/${id}`)
}