// @vitest-environment jsdom
import React from 'react'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, test, expect } from 'vitest'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import AsistenciaPage from './AsistenciasPage' 

describe('Integración: Registro de Asistencias (Profesor)', () => {

  test('Permite al profesor registrar una asistencia y cerrar el modal', async () => {
    render(
      <MemoryRouter initialEntries={['/asistencias/curso/5']}>
        <Routes>
          <Route path="/asistencias/curso/:id" element={<AsistenciaPage />} />
        </Routes>
      </MemoryRouter>
    )

    // 1. Validamos que la tabla cruzó los datos y dibujó al alumno
    const nombreEstudiante = await screen.findByText('Juan Pérez')
    expect(nombreEstudiante).toBeInTheDocument()

    // 2. Abrimos el Dialog de registro
    const botonAbrirDialog = screen.getByRole('button', { name: /registrar asistencia/i })
    fireEvent.click(botonAbrirDialog)

    // 3. Verificamos que el Dialog está abierto
    const tituloDialog = await screen.findByText('Registrar Asistencia', { selector: 'h2' })
    expect(tituloDialog).toBeInTheDocument()

    // 4. Hacemos clic en Guardar (MSW interceptará el POST y devolverá 201)
    const botonGuardar = screen.getByRole('button', { name: /guardar/i })
    fireEvent.click(botonGuardar)

    // 5. Verificamos que el formulario hizo su trabajo y el Dialog desapareció
    await waitFor(() => {
      expect(screen.queryByRole('dialog')).toBeNull()
    })
  })

})