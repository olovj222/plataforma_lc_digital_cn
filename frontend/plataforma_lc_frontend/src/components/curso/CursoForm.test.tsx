// @vitest-environment jsdom
import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import { vi, describe, test, expect, afterEach } from 'vitest'
import CursoForm from './CursoForm'

describe('Componente CursoForm', () => {

  afterEach(() => {
    vi.restoreAllMocks()
  })

  test('Renderiza en modo "Nuevo Curso" por defecto', () => {
    render(<CursoForm onSubmit={vi.fn()} />)
    
    expect(screen.getByText('Nuevo Curso')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Crear curso/i })).toBeInTheDocument()
  })

  test('Renderiza con datos iniciales en modo "Editar Curso"', () => {
    // Aquí está la corrección: Agregamos el "codigo" para que cumpla con el tipo Curso
    const cursoInicial = { nombre: 'Matemáticas', codigo: 101, profesorId: 'prof-123' }
    
    render(<CursoForm onSubmit={vi.fn()} inicial={cursoInicial} />)
    
    expect(screen.getByText('Editar Curso')).toBeInTheDocument()
    expect(screen.getByLabelText(/Nombre/i)).toHaveValue('Matemáticas')
    // Verificamos que el código también se haya llenado
    expect(screen.getByLabelText(/Código/i)).toHaveValue(101)
  })

  test('No llama a onSubmit si faltan campos obligatorios', () => {
    const mockOnSubmit = vi.fn()
    render(<CursoForm onSubmit={mockOnSubmit} />)
    
    // Intentamos guardar sin llenar nada
    fireEvent.click(screen.getByRole('button', { name: /Crear curso/i }))
    
    expect(mockOnSubmit).not.toHaveBeenCalled()
  })

  test('Llama a onSubmit con los datos correctos al llenar el formulario', () => {
    const mockOnSubmit = vi.fn()
    render(<CursoForm onSubmit={mockOnSubmit} />)

    // Simulamos que el usuario escribe en todos los inputs
    fireEvent.change(screen.getByLabelText(/Nombre/i), { target: { value: 'Física' } })
    fireEvent.change(screen.getByLabelText(/Código/i), { target: { value: '202' } })
    fireEvent.change(screen.getByLabelText(/ID Profesor/i), { target: { value: 'prof-99' } })

    fireEvent.click(screen.getByRole('button', { name: /Crear curso/i }))

    expect(mockOnSubmit).toHaveBeenCalledTimes(1)
    expect(mockOnSubmit).toHaveBeenCalledWith(expect.objectContaining({
      nombre: 'Física',
      codigo: 202, // Tu código maneja maravillosamente la conversión a Number, aquí lo validamos
      profesorId: 'prof-99'
    }))
  })
})