export type EstadoJustificativo = 'PENDIENTE' | 'APROBADO' | 'RECHAZADO'

export interface Justificativo {
  id?: number
  estudianteId: number
  cursoId: number
  fechaInasistencia: string // formato yyyy-MM-dd
  motivo: string
  estado?: EstadoJustificativo
  autorId?: string
  resueltoPor?: string
  fechaCreacion?: string
  fechaResolucion?: string
}