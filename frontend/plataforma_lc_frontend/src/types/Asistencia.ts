export interface Asistencia {
    id?: number
    id_curso: number
    id_estudiante: number
    estado?: string
    fecha?: Date
}

export interface EstudianteResponse {
    id?: number
    nombre: string
}