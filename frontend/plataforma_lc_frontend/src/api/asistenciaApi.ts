import type { Asistencia } from '../types/Asistencia'
import { asistenciaApi } from './axiosConfig'


export const getAsistencia = async (): Promise<Asistencia[]> => {
    const { data } = await asistenciaApi.get('/asistencia')
    return data
}

export const getAsistenciaById = async (id: number): Promise<Asistencia> => {
    const { data } = await asistenciaApi.get(`/asistencia/${id}`)
    return data
}

export const createAsistencia = async (asistencia: Asistencia): Promise<Asistencia> => {
    const { data } = await asistenciaApi.post('/asistencia', asistencia)
    return data
}

export const updateAsistencia = async (id: number, asistencia: Asistencia): Promise<Asistencia> => {
    const { data } = await asistenciaApi.put(`/asistencia/${id}`, asistencia)
    return data
}

export const deleteAsistencia = async (id: number): Promise<void> => {
    await asistenciaApi.delete(`/asistencia/${id}`)
}
export const getAsistenciasPorCurso = async (cursoId: number): Promise<Asistencia[]> => {
  const { data } = await asistenciaApi.get(`/asistencia/curso/${cursoId}`)
  return data
}