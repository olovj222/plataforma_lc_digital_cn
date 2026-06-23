// @vitest-environment jsdom
import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import { vi, describe, test, expect, afterEach } from 'vitest'
import AsistenciaForm from './AsistenciaForm'

describe('Componente AsistenciaForm', () => {

  afterEach(() => {
    vi.restoreAllMocks()
  })

  test('Renderiza el formulario en modo "Registrar Asistencia" por defecto', () => {
    render(<AsistenciaForm onSubmit={vi.fn()} />)
    
    // Verifica que el botón por defecto exista
    expect(screen.getByRole('button', { name: /Registrar Asistencia/i })).toBeInTheDocument()
    
    // Verifica que el select muestre "PRESENT" por defecto
    expect(screen.getByText('PRESENT')).toBeInTheDocument()
  })

  test('Renderiza con datos iniciales en modo "Guardar Cambios"', () => {
    const asistenciaInicial = { 
      id: 1, 
      id_estudiante: 99, 
      id_curso: 101, 
      fecha: '2026-06-14', 
      estado: 'ABSENT' 
    }
    
    render(<AsistenciaForm onSubmit={vi.fn()} inicial={asistenciaInicial as any} />)

    expect(screen.getByRole('button', { name: /Guardar Cambios/i })).toBeInTheDocument()
    expect(screen.getByLabelText(/ID del Estudiante/i)).toHaveValue(99)
    expect(screen.getByLabelText(/ID del Curso/i)).toHaveValue(101)
    expect(screen.getByLabelText(/Fecha/i)).toHaveValue('2026-06-14')
    expect(screen.getByText('ABSENT')).toBeInTheDocument()
  })

  test('Muestra una alerta y no llama a onSubmit si falta el ID del estudiante', () => {
    const mockOnSubmit = vi.fn()
    // Espiamos window.alert para que la prueba verifique que se llamó y evitar que manche la consola
    const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {})

    render(<AsistenciaForm onSubmit={mockOnSubmit} />)

    // Hacemos clic sin llenar el formulario
    fireEvent.click(screen.getByRole('button', { name: /Registrar Asistencia/i }))

    // Verificamos la alerta exacta que programaste
    expect(alertSpy).toHaveBeenCalledWith('Por favor, ingresa el ID del estudiante.')
    expect(mockOnSubmit).not.toHaveBeenCalled()
  })

  test('Llama a onSubmit con los datos correctos al llenar el formulario', () => {
    const mockOnSubmit = vi.fn()
    render(<AsistenciaForm onSubmit={mockOnSubmit} />)

    // 1. Llenamos los inputs de texto/número
    fireEvent.change(screen.getByLabelText(/ID del Estudiante/i), { target: { value: '10' } })
    fireEvent.change(screen.getByLabelText(/ID del Curso/i), { target: { value: '5' } })
    fireEvent.change(screen.getByLabelText(/Fecha/i), { target: { value: '2026-06-15' } })

    // 2. Interactuamos con el Select de MUI (Es un div con role="combobox")
    const combobox = screen.getByRole('combobox', { name: /Estado/i })
    fireEvent.mouseDown(combobox) // Abre el menú
    
    // 3. Hacemos clic en la opción "ABSENT" dentro de la lista desplegada
    const opcionAbsent = screen.getByRole('option', { name: 'ABSENT' })
    fireEvent.click(opcionAbsent)

    // 4. Enviamos el formulario
    fireEvent.click(screen.getByRole('button', { name: /Registrar Asistencia/i }))

    expect(mockOnSubmit).toHaveBeenCalledTimes(1)
    expect(mockOnSubmit).toHaveBeenCalledWith(expect.objectContaining({
      id_estudiante: 10,
      id_curso: 5,
      fecha: '2026-06-15',
      estado: 'ABSENT'
    }))
  })
})