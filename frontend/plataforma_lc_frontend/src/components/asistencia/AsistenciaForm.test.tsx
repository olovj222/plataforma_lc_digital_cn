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
    
    expect(screen.getByRole('button', { name: /Registrar Asistencia/i })).toBeInTheDocument()
    expect(screen.getByText('PRESENT')).toBeInTheDocument()
  })

  test('Renderiza con datos iniciales en modo "Guardar Cambios"', () => {
    const asistenciaInicial = { 
      id: 1, 
      id_estudiante: 99, 
      id_clase: 101,  // corregido: era id_curso
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
    const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {})

    render(<AsistenciaForm onSubmit={mockOnSubmit} />)

    fireEvent.click(screen.getByRole('button', { name: /Registrar Asistencia/i }))

    expect(alertSpy).toHaveBeenCalledWith('Por favor, ingresa el ID del estudiante.')
    expect(mockOnSubmit).not.toHaveBeenCalled()
  })

  test('Llama a onSubmit con los datos correctos al llenar el formulario', () => {
    const mockOnSubmit = vi.fn()
    render(<AsistenciaForm onSubmit={mockOnSubmit} />)

    fireEvent.change(screen.getByLabelText(/ID del Estudiante/i), { target: { value: '10' } })
    fireEvent.change(screen.getByLabelText(/ID del Curso/i), { target: { value: '5' } })
    fireEvent.change(screen.getByLabelText(/Fecha/i), { target: { value: '2026-06-15' } })

    const combobox = screen.getByRole('combobox', { name: /Estado/i })
    fireEvent.mouseDown(combobox)
    
    const opcionAbsent = screen.getByRole('option', { name: 'ABSENT' })
    fireEvent.click(opcionAbsent)

    fireEvent.click(screen.getByRole('button', { name: /Registrar Asistencia/i }))

    expect(mockOnSubmit).toHaveBeenCalledTimes(1)
    expect(mockOnSubmit).toHaveBeenCalledWith(expect.objectContaining({
      id_estudiante: 10,
      id_clase: 5,  // corregido: era id_curso
      fecha: '2026-06-15',
      estado: 'ABSENT'
    }))
  })
})
