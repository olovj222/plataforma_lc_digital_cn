import { describe, test, expect, vi, afterEach } from 'vitest'
import { getEvaluacionesPorCurso, actualizarNota } from './evaluacionesApi'
import { evaluacionesApi } from './axiosConfig'

vi.mock('./axiosConfig', () => ({
  evaluacionesApi: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() }
}))

describe('Pruebas de evaluacionesApi', () => {
  afterEach(() => { vi.clearAllMocks() })

  test('getEvaluacionesPorCurso usa el cursoId en la URL', async () => {
    const mockEvaluaciones = [{ id: 1, calificacion: 95 }]
    vi.mocked(evaluacionesApi.get).mockResolvedValueOnce({ data: mockEvaluaciones })
    
    const resultado = await getEvaluacionesPorCurso(101)
    
    // Verificamos la interpolación de la variable en la URL
    expect(evaluacionesApi.get).toHaveBeenCalledWith('/evaluaciones/curso/101')
    expect(resultado).toEqual(mockEvaluaciones)
  })

  test('actualizarNota envía la nota como query param', async () => {
    const mockRespuesta = { id: 1, calificacion: 100 }
    vi.mocked(evaluacionesApi.post).mockResolvedValueOnce({ data: mockRespuesta })
    
    const resultado = await actualizarNota(1, 100)
    
    // Verificamos que armes bien la URL con el query parameter (?nota=...)
    expect(evaluacionesApi.post).toHaveBeenCalledWith('/evaluaciones/1/nota?nota=100')
    expect(resultado).toEqual(mockRespuesta)
  })
})