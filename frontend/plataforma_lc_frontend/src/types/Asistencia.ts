export interface Asistencia {
    id?: number
    id_clase: number
    id_estudiante: number
    estado?: string
    fecha?: Date
}

export interface EstudianteResponse {
    id?: number
    nombre: string
}