import React, { useEffect, useState } from 'react'
import { Box, Button, FormControl, InputLabel, MenuItem, Select, TextField } from '@mui/material'
import type { Asistencia } from '../../types/Asistencia'

interface AsistenciaFormProps {
  onSubmit: (asistencia: Asistencia) => Promise<void> | void
  inicial?: Asistencia
}

function AsistenciaForm({ onSubmit, inicial }: AsistenciaFormProps) {
  // ─── CORRECCIÓN 1: Unificamos el estado inicial a "PRESENT" (Match con backend) ───
  const [id_estudiante, setEstudianteId] = useState<number | ''>('')
  const [id_clase, setClaseId] = useState<string>('')
  const [fecha, setFecha] = useState(new Date().toISOString().split('T')[0])
  const [estado, setEstado] = useState('PRESENT')

  useEffect(() => {
    if (inicial) {
      setEstudianteId(inicial.id_estudiante)
      setClaseId(String(inicial.id_clase))
      const fechaObj = new Date(inicial.fecha)
      setFecha(fechaObj.toISOString().split('T')[0])
      setEstado(inicial.estado)
    } else {
      setEstudianteId('')
      setClaseId('')
      setFecha(new Date().toISOString().split('T')[0])
      setEstado('PRESENT')
    }
  }, [inicial])

  const handleFormSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (id_estudiante === '' || id_clase === '') {
      alert('Por favor, ingresa todos los campos obligatorios.')
      return
    }

    const datosAsistencia: any = {
        ...(inicial?.id && { id: inicial.id }),
        id_estudiante: Number(id_estudiante),
        id_clase: Number(id_clase),
        fecha: fecha, 
        estado
    }

    // Esperamos a que la petición termine antes de que el flujo continúe
    await onSubmit(datosAsistencia)
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
        label="ID del Estudiante"
        type="number"
        variant="outlined"
        fullWidth
        required
        value={id_estudiante}
        onChange={(e) => setEstudianteId(e.target.value !== '' ? Number(e.target.value) : '')}
      />

      {/* ─── CORRECCIÓN 2: Cambiado "ID del Curso" a "ID de la Clase" para alinearse con id_clase ─── */}
      <TextField
        label="ID de la Clase"
        type="number"
        variant="outlined"
        fullWidth
        required
        value={id_clase}
        onChange={(e) => setClaseId(e.target.value)}
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