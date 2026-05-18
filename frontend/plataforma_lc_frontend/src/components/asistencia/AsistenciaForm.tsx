import React, { useEffect, useState } from 'react'
import { Box, Button, FormControl, InputLabel, MenuItem, Select, TextField } from '@mui/material'
import type { Asistencia } from '../../types/Asistencia'

interface AsistenciaFormProps {
  onSubmit: (asistencia: Asistencia) => Promise<void> | void
  inicial?: Asistencia
}

function AsistenciaForm({ onSubmit, inicial }: AsistenciaFormProps) {
  // Inicializamos los estados. Si es una nueva asistencia, la fecha por defecto será el día de hoy.
  const [id_estudiante, setEstudianteId] = useState<number | ''>('')
  const [id_curso, setCursoId] = useState<string>('')
  const [fecha, setFecha] = useState(new Date().toISOString().split('T')[0])
  const [estado, setEstado] = useState('PRESENTE')

  // Efecto para cargar los datos del estudiante si se entra en modo edición
  useEffect(() => {
    if (inicial) {
      setEstudianteId(inicial.id_estudiante)
      setCursoId(String(inicial.id_curso))
      const fechaObj = new Date(inicial.fecha ?? new Date())
      setFecha(fechaObj.toISOString().split('T')[0])
      setEstado(inicial.estado ?? 'PRESENT')
    } else {
      // Resetear al estado inicial si pasa de editar a crear nuevo
      setEstudianteId('')
      setFecha(new Date().toISOString().split('T')[0])
      setEstado('PRESENT')
    }
  }, [inicial])

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault()

    if (id_estudiante === '') {
      alert('Por favor, ingresa el ID del estudiante.')
      return
    }

    const datosAsistencia: any = {
        ...(inicial?.id && { id: inicial.id }),
        id_estudiante: Number(id_estudiante),
        id_curso: Number(id_curso),
        fecha: fecha, 
        estado
    }

    onSubmit(datosAsistencia)
  }

  return (
    <Box
      component="form"
      onSubmit={handleFormSubmit}
      sx={{
        display: 'flex',
        flexDirection: 'column',
        gap: 3,
        pt: 1, // Espacio para que no choque con el título del Dialog
        minWidth: { sm: 400 }
      }}
    >
      <TextField
        label="ID del Estudiante"
        type="number"
        variant="outlined"
        fullWidth
        required
        value={id_estudiante}
        onChange={(e) => setEstudianteId(e.target.value !== '' ? Number(e.target.value) : '')}
      />

    <TextField
            label="ID del Curso"
            type="number"
            variant="outlined"
            fullWidth
            required
            value={id_curso}
            onChange={(e) => setCursoId(e.target.value)}
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

      <FormControl fullWidth required>
        <InputLabel id="estado-asistencia-label">Estado</InputLabel>
        <Select
          labelId="estado-asistencia-label"
          id="estado-asistencia"
          label="Estado"
          value={estado}
          onChange={(e) => setEstado(e.target.value)}
        >
          <MenuItem value="PRESENT">PRESENT</MenuItem>
          <MenuItem value="ABSENT">ABSENT</MenuItem>
          <MenuItem value="JUSTIFIED">JUSTIFIED</MenuItem>
          <MenuItem value="ATRASO">Atraso</MenuItem>
        </Select>
      </FormControl>

      <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1, mt: 1 }}>
        <Button type="submit" variant="contained" color="primary" fullWidth>
          {inicial ? 'Guardar Cambios' : 'Registrar Asistencia'}
        </Button>
      </Box>
    </Box>
  )
}

export default AsistenciaForm