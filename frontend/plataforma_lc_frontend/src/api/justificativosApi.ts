import { justificativosApi } from './axiosConfig'
import type { Justificativo } from '../types/Justificativo'

export const getJustificativosByCurso = async (cursoId: number): Promise<Justificativo[]> => {
  const res = await justificativosApi.get(`/justificativos/curso/${cursoId}`)
  return res.data
}

export const getJustificativosByEstudiante = async (estudianteId: number): Promise<Justificativo[]> => {
  const res = await justificativosApi.get(`/justificativos/estudiante/${estudianteId}`)
  return res.data
}

export const getJustificativosPendientes = async (): Promise<Justificativo[]> => {
  const res = await justificativosApi.get('/justificativos/pendientes')
  return res.data
}

export const crearJustificativo = async (justificativo: Justificativo): Promise<Justificativo> => {
  const res = await justificativosApi.post('/justificativos', justificativo)
  return res.data
}

export const aprobarJustificativo = async (id: number): Promise<Justificativo> => {
  const res = await justificativosApi.put(`/justificativos/${id}/aprobar`)
  return res.data
}

export const rechazarJustificativo = async (id: number): Promise<Justificativo> => {
  const res = await justificativosApi.put(`/justificativos/${id}/rechazar`)
  return res.data
}