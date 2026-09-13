// @vitest-environment jsdom
import React from 'react' // <-- ESTO ES CLAVE para el test runner
import { render, screen, fireEvent } from '@testing-library/react'
import { vi, describe, test, expect, afterEach } from 'vitest'
import EstudianteForm from './EstudianteForm'

describe('Componente EstudianteForm', () => {

  afterEach(() => {
    vi.restoreAllMocks()
  })

  test('Renderiza en modo "Nuevo Estudiante" por defecto', () => {
    render(<EstudianteForm onSubmit={vi.fn()} />)
    expect(screen.getByText('Nuevo Estudiante')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Crear estudiante/i })).toBeInTheDocument()
  })

  test('Renderiza en modo "Editar Estudiante" si recibe datos iniciales', () => {
    const estudianteInicial = {
      nombre: 'Juan',
      apPaterno: 'Pérez',
      apMaterno: 'López',
      direccion: 'Calle Falsa 123',
      telefono: '123456',
      cursos: [{ cursoId: 101 }]
    }

    render(<EstudianteForm onSubmit={vi.fn()} inicial={estudianteInicial} />)
    expect(screen.getByText('Editar Estudiante')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Guardar cambios/i })).toBeInTheDocument()
  })

  test('No llama a onSubmit si faltan campos obligatorios', () => {
    const mockOnSubmit = vi.fn()
    render(<EstudianteForm onSubmit={mockOnSubmit} />)
    
    const botonGuardar = screen.getByRole('button', { name: /Crear estudiante/i })
    fireEvent.click(botonGuardar)

    expect(mockOnSubmit).not.toHaveBeenCalled()
  })

  test('Llama a onSubmit con los datos correctos al llenar el formulario', () => {
    const mockOnSubmit = vi.fn()
    render(<EstudianteForm onSubmit={mockOnSubmit} />)

    // Usamos getAllByRole o busquedas más genéricas para evitar problemas con MUI
    const inputs = screen.getAllByRole('textbox')
    // El primer textbox debería ser el nombre, el segundo apPaterno
    fireEvent.change(inputs[0], { target: { value: 'Ana' } })
    fireEvent.change(inputs[1], { target: { value: 'Gómez' } })
    
    // El input numérico de curso
    const inputCurso = screen.getByRole('spinbutton')
    fireEvent.change(inputCurso, { target: { value: '99' } })

    fireEvent.click(screen.getByRole('button', { name: /Crear estudiante/i }))

    expect(mockOnSubmit).toHaveBeenCalledTimes(1)
    expect(mockOnSubmit).toHaveBeenCalledWith(expect.objectContaining({
      nombre: 'Ana',
      apPaterno: 'Gómez',
      cursos: [{ cursoId: 99 }]
    }))
  })

  test('Permite agregar nuevos campos de cursos', () => {
    render(<EstudianteForm onSubmit={vi.fn()} />)
    
    // Verificamos que solo hay un input numérico al inicio
    expect(screen.getAllByRole('spinbutton')).toHaveLength(1)

    const botonAgregar = screen.getByRole('button', { name: /Agregar curso/i })
    fireEvent.click(botonAgregar)

    // Ahora debería haber dos
    expect(screen.getAllByRole('spinbutton')).toHaveLength(2)
  })
})