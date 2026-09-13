import { describe, test, expect, vi, afterEach } from 'vitest'
import { getCursos, createCurso, getCursosByProfesor } from './cursoApi'
import { cursoApi } from './axiosConfig'

vi.mock('./axiosConfig', () => ({
  cursoApi: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() }
}))

describe('Pruebas de cursoApi', () => {
  afterEach(() => { vi.clearAllMocks() })

  test('getCursos obtiene la lista de cursos', async () => {
    const mockCursos = [{ id: 1, nombre: 'Matemáticas' }]
    vi.mocked(cursoApi.get).mockResolvedValueOnce({ data: mockCursos })
    
    const resultado = await getCursos()
    
    expect(cursoApi.get).toHaveBeenCalledWith('/curso')
    expect(resultado).toEqual(mockCursos)
  })

  test('getCursosByProfesor filtra correctamente por profesorId', async () => {
    const mockCursos = [
      { id: 1, nombre: 'Matemáticas', profesorId: 'prof-1' },
      { id: 2, nombre: 'Historia', profesorId: 'prof-2' }
    ]
    // Simulamos que el backend devuelve TODOS los cursos
    vi.mocked(cursoApi.get).mockResolvedValueOnce({ data: mockCursos })
    
    // Ejecutamos la función pidiendo solo los del profesor 1
    const resultado = await getCursosByProfesor('prof-1')
    
    // Verificamos que el filter haya hecho su trabajo
    expect(resultado).toEqual([{ id: 1, nombre: 'Matemáticas', profesorId: 'prof-1' }])
  })

  test('createCurso envía los datos por POST', async () => {
    const nuevoCurso = { nombre: 'Ciencias', profesorId: 'prof-1' }
    vi.mocked(cursoApi.post).mockResolvedValueOnce({ data: { id: 3, ...nuevoCurso } })
    
    const resultado = await createCurso(nuevoCurso as any)
    
    expect(cursoApi.post).toHaveBeenCalledWith('/curso', nuevoCurso)
    expect(resultado.id).toBe(3)
  })
})