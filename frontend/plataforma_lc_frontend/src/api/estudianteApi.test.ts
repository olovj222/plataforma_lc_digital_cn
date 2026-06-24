import { describe, test, expect, vi, afterEach } from 'vitest'
import { getEstudiantes, createEstudiante } from './estudianteApi'
import { estudianteApi } from './axiosConfig'

// 1. EL MOCK: Le decimos a Vitest que secuestre este archivo.
// Todo lo que salga de 'axiosConfig' ahora será un espía controlado por nosotros.
vi.mock('./axiosConfig', () => {
  return {
    estudianteApi: {
      get: vi.fn(),
      post: vi.fn(),
      put: vi.fn(),
      delete: vi.fn(),
    }
  }
})

describe('Pruebas de estudianteApi', () => {
  
  // Limpiamos los espías después de cada test para que no se mezclen
  afterEach(() => {
    vi.clearAllMocks()
  })

  test('getEstudiantes llama a la ruta correcta y devuelve los datos', async () => {
    // 2. Preparamos los datos falsos que queremos que el "servidor" devuelva
    const mockEstudiantes = [
      { id: 1, nombre: 'Juan', apPaterno: 'Pérez' },
      { id: 2, nombre: 'Ana', apPaterno: 'Gómez' }
    ]
    
    // 3. LA INTERCEPCIÓN: Le decimos al espía que cuando llamen a .get(),
    // devuelva exitosamente un objeto. (Recuerda que Axios SIEMPRE envuelve tu respuesta en una propiedad "data")
    vi.mocked(estudianteApi.get).mockResolvedValueOnce({ data: mockEstudiantes })

    // 4. Ejecutamos la función real
    const resultado = await getEstudiantes()

    // 5. Verificamos que la función hizo exactamente lo que debía
    expect(estudianteApi.get).toHaveBeenCalledTimes(1)
    expect(estudianteApi.get).toHaveBeenCalledWith('/estudiante')
    
    // Verificamos que la función peló el { data: ... } de Axios y devolvió tu arreglo limpio
    expect(resultado).toEqual(mockEstudiantes)
  })

  test('createEstudiante envía los datos correctos por POST', async () => {
    // Preparamos lo que vamos a enviar y lo que nos van a responder
    const nuevoEstudiante = { nombre: 'Carlos', apPaterno: 'Ruiz', cursos: [] }
    const estudianteCreado = { id: 3, ...nuevoEstudiante }

    // Interceptamos el POST
    vi.mocked(estudianteApi.post).mockResolvedValueOnce({ data: estudianteCreado })

    // Ejecutamos la función asumiendo el tipo (para evitar quejidos de TS en el mock)
    const resultado = await createEstudiante(nuevoEstudiante as any)

    // Verificamos que le hayamos pegado a la URL correcta CON el cuerpo correcto
    expect(estudianteApi.post).toHaveBeenCalledWith('/estudiante', nuevoEstudiante)
    expect(resultado).toEqual(estudianteCreado)
  })
})