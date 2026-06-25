import type { Clase } from '../types/Clase';
import { claseApi } from './axiosConfig'; // Ajusta el import según tu proyecto


export const getClases = async (): Promise<Clase[]> => {
  const response = await claseApi.get('/clase');
  return response.data;
};

export const createClase = async (clase: Clase): Promise<Clase> => {
  const response = await claseApi.post('/clase', clase);
  return response.data;
};

export const updateClase = async (id: number, clase: Clase): Promise<Clase> => {
  const response = await claseApi.put(`/clase/${id}`, clase);
  return response.data;
};

export const deleteClase = async (id: number): Promise<void> => {
  await claseApi.delete(`/clase/${id}`);
};

// Por si necesitas usar el endpoint @GetMapping("/curso/{cursoId}") en el futuro
export const getClasesPorCurso = async (cursoId: number): Promise<Clase[]> => {
  const response = await claseApi.get(`/clase/curso/${cursoId}`);
  return response.data;
};