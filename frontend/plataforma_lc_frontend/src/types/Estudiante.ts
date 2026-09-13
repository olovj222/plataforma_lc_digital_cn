export interface EstudianteCurso {
  id?: number
  cursoId: number
  cursoName?: string
}

export interface Estudiante {
  id?: number
  nombre: string
  apPaterno: string
  apMaterno?: string
  direccion?: string
  telefono?: string
  cursos: EstudianteCurso[]
}