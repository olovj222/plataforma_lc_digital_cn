import { describe, test, expect, vi, afterEach } from 'vitest'
import { getAsistencia, createAsistencia } from './asistenciaApi'
import { asistenciaApi } from './axiosConfig'

vi.mock('./axiosConfig', () => ({
  asistenciaApi: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() }
}))

describe('Pruebas de asistenciaApi', () => {
  afterEach(() => { vi.clearAllMocks() })

  test('getAsistencia obtiene los registros', async () => {
    const mockAsistencia = [{ id: 1, estudianteId: 10, estado: 'PRESENTE' }]
    vi.mocked(asistenciaApi.get).mockResolvedValueOnce({ data: mockAsistencia })
    
    const resultado = await getAsistencia()
    
    expect(asistenciaApi.get).toHaveBeenCalledWith('/asistencia')
    expect(resultado).toEqual(mockAsistencia)
  })

  test('createAsistencia envía datos por POST', async () => {
    const nuevaAsistencia = { estudianteId: 10, estado: 'PRESENTE' }
    vi.mocked(asistenciaApi.post).mockResolvedValueOnce({ data: { id: 1, ...nuevaAsistencia } })
    
    const resultado = await createAsistencia(nuevaAsistencia as any)
    
    expect(asistenciaApi.post).toHaveBeenCalledWith('/asistencia', nuevaAsistencia)
    expect(resultado.id).toBe(1)
  })
})