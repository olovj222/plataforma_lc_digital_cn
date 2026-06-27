import { render, screen, fireEvent } from '@testing-library/react'
import { describe, test, expect, vi } from 'vitest'
import AsistenciaForm from './AsistenciaForm'
import type { Asistencia } from '../../types/Asistencia'

describe('Componente AsistenciaForm', () => {
  const mockOnSubmit = vi.fn()

  test('Renderiza con datos iniciales en modo "Guardar Cambios"', () => {
    const datosIniciales: Asistencia = {
      id: 1,
      id_estudiante: 99,
      id_clase: 101,
      fecha: new Date('2026-06-14'), // ✨ CORRECCIÓN: Convertido a objeto Date para cumplir con el tipo Asistencia
      estado: 'ABSENT'
    }

    render(<AsistenciaForm onSubmit={mockOnSubmit} inicial={datosIniciales} />)

    expect(screen.getByRole('button', { name: /Guardar Cambios/i })).toBeInTheDocument()
    expect(screen.getByLabelText(/ID del Estudiante/i)).toHaveValue(99)
    expect(screen.getByLabelText(/ID de la Clase/i)).toHaveValue(101)
    expect(screen.getByLabelText(/Fecha/i)).toHaveValue('2026-06-14')
    expect(screen.getByText('ABSENT')).toBeInTheDocument()
  })

  test('Muestra una alerta y no llama a onSubmit si falta el ID del estudiante', () => {
    const alertSpy = vi.spyOn(window, 'alert').mockImplementation(() => {})
    render(<AsistenciaForm onSubmit={mockOnSubmit} />)

    fireEvent.click(screen.getByRole('button', { name: /Registrar Asistencia/i }))

    // ─── CORRECCIÓN: Validar el nuevo mensaje unificado de campos obligatorios ───
    expect(alertSpy).toHaveBeenCalledWith('Por favor, ingresa todos los campos obligatorios.')
    expect(mockOnSubmit).not.toHaveBeenCalled()
    alertSpy.mockRestore()
  })

  test('Llama a onSubmit con los datos correctos al llenar el formulario', () => {
    render(<AsistenciaForm onSubmit={mockOnSubmit} />)

    // Simular el ingreso de datos del estudiante
    fireEvent.change(screen.getByLabelText(/ID del Estudiante/i), { target: { value: '10' } })
    
    // Simular el ingreso de datos de la clase
    fireEvent.change(screen.getByLabelText(/ID de la Clase/i), { target: { value: '5' } })
    
    // Simular el cambio de fecha
    fireEvent.change(screen.getByLabelText(/Fecha/i), { target: { value: '2026-06-26' } })

    // Hacer clic en enviar
    fireEvent.click(screen.getByRole('button', { name: /Registrar Asistencia/i }))

    // Comprobar que los datos enviados coinciden con el esquema exacto de tu tipo "Asistencia"
    expect(mockOnSubmit).toHaveBeenCalledWith({
      id_estudiante: 10,
      id_clase: 5,
      fecha: '2026-06-26',
      estado: 'PRESENT'
    })
  })
})
