import type { Curso } from '../types/Curso'
import { cursoApi } from './axiosConfig'


export const getCursos = async (): Promise<Curso[]> => {
  const { data } = await cursoApi.get('/curso')
  return data
}

export const getCursoById = async (id: number): Promise<Curso> => {
  const { data } = await cursoApi.get(`/curso/${id}`)
  return data
}

export const createCurso = async (curso: Curso): Promise<Curso> => {
  const { data } = await cursoApi.post('/curso', curso)
  return data
}

export const updateCurso = async (id: number, curso: Curso): Promise<Curso> => {
  const { data } = await cursoApi.put(`/curso/${id}`, curso)
  return data
}

export const deleteCurso = async (id: number): Promise<void> => {
  await cursoApi.delete(`/curso/${id}`)
}

export const getCursosByProfesor = async (profesorId: string): Promise<Curso[]> => {
  const { data } = await cursoApi.get('/curso')
  return data.filter((curso: Curso) => curso.profesorId === profesorId)
}