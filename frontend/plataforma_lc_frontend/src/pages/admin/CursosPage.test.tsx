// @vitest-environment jsdom
import React from 'react'
import { render, screen } from '@testing-library/react'
import { describe, test, expect } from 'vitest'
import CursoPage from './CursosPage' // Ajusta la ruta a tu componente real
import { BrowserRouter } from 'react-router-dom'

describe('Página de Cursos (Integración)', () => {

  test('Renderiza la lista de cursos obtenida desde la API', async () => {
    // 1. Renderizamos el componente padre. 
    // Internamente, este componente disparará su Axios/Fetch.
    // MSW interceptará esa llamada y devolverá Matemáticas y Física en milisegundos.
    render(
      <BrowserRouter>
        <CursoPage />
      </BrowserRouter>
    )

    // 2. Como la llamada a la red es asíncrona, la pantalla no se dibuja de inmediato.
    // Usamos `findByText` en lugar de `getByText`. 
    // `findByText` espera inteligentemente hasta que el texto aparezca en el DOM.
    
    const cursoMatematicas = await screen.findByText('Matemáticas')
    const cursoFisica = await screen.findByText('Física')

    // 3. Verificamos que los datos se renderizaron correctamente
    expect(cursoMatematicas).toBeInTheDocument()
    expect(cursoFisica).toBeInTheDocument()
    
    // Si tienes los códigos visibles en tu tabla, también los podemos buscar
    expect(screen.getByText('101')).toBeInTheDocument()
  })

})