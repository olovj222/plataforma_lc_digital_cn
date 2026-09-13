// @vitest-environment jsdom
import React from 'react'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, test, expect } from 'vitest'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import EvaluacionesPage from './EvaluacionesPage'

describe('Integración: Registro de Evaluaciones (Profesor)', () => {

  test('Permite registrar una calificación válida y cerrar el modal', async () => {
    render(
      <MemoryRouter initialEntries={['/evaluaciones/curso/5']}>
        <Routes>
          <Route path="/evaluaciones/curso/:id" element={<EvaluacionesPage />} />
        </Routes>
      </MemoryRouter>
    )

    // 1. Esperamos a que la tabla cruce los datos y muestre "Control 1" y "Juan Pérez"
    expect(await screen.findByText('Control 1')).toBeInTheDocument()
    expect(await screen.findByText('Juan Pérez')).toBeInTheDocument()

    // 2. Abrimos el Dialog
    const botonNueva = screen.getByRole('button', { name: /nueva evaluación/i })
    fireEvent.click(botonNueva)

    // 3. Verificamos que se abrió el formulario
    expect(await screen.findByText('Nueva Evaluación', { selector: 'h2' })).toBeInTheDocument()

    // 4. Llenamos los TextFields según los labels que tienes en tu código
    const inputNombre = screen.getByLabelText(/nombre/i)
    fireEvent.change(inputNombre, { target: { value: 'Examen Final' } })

    const inputEstudiante = screen.getByLabelText(/id estudiante/i)
    fireEvent.change(inputEstudiante, { target: { value: '10' } })

    const inputNota = screen.getByLabelText(/calificación/i)
    fireEvent.change(inputNota, { target: { value: '7.0' } })

    // 5. Guardamos
    const botonGuardar = screen.getByRole('button', { name: /guardar/i })
    fireEvent.click(botonGuardar)

    // 6. Verificamos que el modal se cerró exitosamente tras recibir la respuesta de MSW
    await waitFor(() => {
      expect(screen.queryByRole('dialog')).toBeNull()
    })
  })

})