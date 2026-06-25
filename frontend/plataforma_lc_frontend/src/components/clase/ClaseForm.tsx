import React, { useEffect, useState } from 'react'
import { Box, Button, TextField } from '@mui/material'
import type { Clase } from '../../types/Clase'


interface ClaseFormProps {
  onSubmit: (clase: Clase) => Promise<void> | void
  inicial?: Clase
}

function ClaseForm({ onSubmit, inicial }: ClaseFormProps) {
  const [cursoId, setCursoId] = useState<number | ''>('')
  const [fecha, setFecha] = useState(new Date().toISOString().split('T')[0])
  const [descripcion, setDescripcion] = useState('')

  useEffect(() => {
    if (inicial) {
      setCursoId(inicial.cursoId)
      // Ajuste para formatear la fecha correctamente al formato YYYY-MM-DD
      const fechaObj = new Date(inicial.fecha)
      setFecha(fechaObj.toISOString().split('T')[0])
      setDescripcion(inicial.descripcion || '')
    } else {
      setCursoId('')
      setFecha(new Date().toISOString().split('T')[0])
      setDescripcion('')
    }
  }, [inicial])

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault()

    if (cursoId === '') {
      alert('Por favor, ingresa el ID del curso.')
      return
    }

    const datosClase: Clase = {
        ...(inicial?.id && { id: inicial.id }),
        cursoId: Number(cursoId),
        fecha: fecha,
        descripcion: descripcion
    }

    onSubmit(datosClase)
  }

  return (
    <Box
      component="form"
      onSubmit={handleFormSubmit}
      noValidate
      sx={{
        display: 'flex',
        flexDirection: 'column',
        gap: 3,
        pt: 1,
        minWidth: { sm: 400 }
      }}
    >
      <TextField
        label="ID del Curso"
        type="number"
        variant="outlined"
        fullWidth
        required
        value={cursoId}
        onChange={(e) => setCursoId(e.target.value !== '' ? Number(e.target.value) : '')}
        helperText="Debe coincidir con un curso existente en el sistema."
      />

      <TextField
        label="Fecha"
        type="date"
        variant="outlined"
        fullWidth
        required
        value={fecha}
        onChange={(e) => setFecha(e.target.value)}
      />

      <TextField
        label="Descripción de la clase"
        type="text"
        variant="outlined"
        fullWidth
        multiline
        rows={3}
        value={descripcion}
        onChange={(e) => setDescripcion(e.target.value)}
        placeholder="Ej: Unidad 1 - Introducción a la materia"
      />

      <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1, mt: 1 }}>
        <Button type="submit" variant="contained" color="primary" fullWidth>
          {inicial ? 'Guardar Cambios' : 'Registrar Clase'}
        </Button>
      </Box>
    </Box>
  )
}

export default ClaseForm