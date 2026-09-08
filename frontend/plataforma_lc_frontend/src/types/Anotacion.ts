export type TipoAnotacion = 'POSITIVA' | 'NEGATIVA'

export interface Anotacion {
  id?: number
  estudianteId: number
  cursoId: number
  tipo: TipoAnotacion
  descripcion: string
  fecha?: string
  autorId?: string
}