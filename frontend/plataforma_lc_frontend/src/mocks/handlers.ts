import { http, HttpResponse } from 'msw'

// Asumimos que esta es la URL a la que le pega tu Axios/Fetch
const GATEWAY_URL = 'http://localhost:8085'

export const handlers = [
  // Interceptamos la petición al MS de Cursos a través del Gateway
  http.get(`${GATEWAY_URL}/curso`, () => {
    return HttpResponse.json([
      { id: 1, nombre: 'Matemáticas', codigo: 101, profesorId: 'prof-123' },
      { id: 2, nombre: 'Física', codigo: 202, profesorId: 'prof-99' }
    ])
  }),

  http.get(`${GATEWAY_URL}/clase/curso/:id`, () => {
  return HttpResponse.json([
    {
      id: 1,
      fecha: '2026-06-23',
      descripcion: 'Clase de introducción'
    }
  ])
}),

  // Intercepta estudianteApi.get('/')
  http.get(`${GATEWAY_URL}/estudiante`, () => {
    return HttpResponse.json([
      { 
        id: 10, 
        nombre: 'Juan', 
        apPaterno: 'Pérez', // Agregado para que se concatene correctamente
        rut: '20.123.456-7',
        cursos: [
          { cursoId: 5 } // Ajustado a cursoId para que el .some() haga match
        ] 
      }
    ])
  }),

  // Intercepta asistenciaApi.get('/')
  http.get(`${GATEWAY_URL}/asistencia`, () => {
    return HttpResponse.json([])
  }),

  // Intercepta las peticiones de asistencia por curso
  http.get(`${GATEWAY_URL}/asistencia/curso/:id`, () => {
    return HttpResponse.json([
      { 
        id: 1, 
        id_clase: 5,
        id_estudiante: 10, // Enviamos el número directo, como espera tu React
        estado: 'PRESENT', 
        fecha: '2026-06-23' 
      }
    ])
  }),

  http.get(`${GATEWAY_URL}/evaluaciones/curso/:id`, () => {
    return HttpResponse.json([
      {
        id: 1,
        nombre: 'Control 1', // ¡Agregado para que se vea en la tabla!
        cursoId: 5,
        estudianteId: 10,    // Corregido de id_estudiante a estudianteId
        calificacion: 6.5
      }
    ])
  }),



  // POST listo para probar crear un curso
  http.post(`${GATEWAY_URL}/curso`, () => {
    // Simulamos que el Gateway respondió con un status 201 Created
    return HttpResponse.json(
      { id: 3, nombre: 'Nuevo Curso', codigo: 303, profesorId: 'prof-1' },
      { status: 201 }
    )
  }),

  // Intercepta el guardado de asistencia
  http.post(`${GATEWAY_URL}/asistencia`, () => {
    return HttpResponse.json(
      { id: 99, estado: 'ABSENT', fecha: '2026-06-23' }, 
      { status: 201 }
    )
  }),

  // Intercepta el guardado de evaluaciones
  http.post(`${GATEWAY_URL}/evaluaciones`, () => {
    return HttpResponse.json({}, { status: 201 })
  }),
]