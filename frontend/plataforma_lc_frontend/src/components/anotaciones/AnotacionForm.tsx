import { useEffect, useState } from 'react'
import { Box, Button, MenuItem, TextField, Typography } from '@mui/material'
import type { Anotacion, TipoAnotacion } from '../../types/Anotacion'

interface Props {
  cursoId: number
  onSubmit: (anotacion: Anotacion) => void
  inicial?: Anotacion
}

function AnotacionForm({ cursoId, onSubmit, inicial }: Props) {
  const [form, setForm] = useState<Anotacion>(
    inicial ?? {
      estudianteId: 0,
      cursoId,
      tipo: 'POSITIVA',
      descripcion: '',
    }
  )

  useEffect(() => {
    if (inicial) {
      setForm(inicial)
    } else {
      setForm({ estudianteId: 0, cursoId, tipo: 'POSITIVA', descripcion: '' })
    }
  }, [inicial, cursoId])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
  }

  const handleTipoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm(prev => ({ ...prev, tipo: e.target.value as TipoAnotacion }))
  }

  const handleSubmit = () => {
    if (!form.estudianteId || !form.descripcion) return
    onSubmit(form)
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, maxWidth: 400 }}>
      <Typography variant="h6">Nueva Anotación</Typography>
      <TextField
        label="ID Estudiante"
        name="estudianteId"
        type="number"
        value={form.estudianteId}
        onChange={handleChange}
        required
      />
      <TextField
        select
        label="Tipo"
        name="tipo"
        value={form.tipo}
        onChange={handleTipoChange}
        required
      >
        <MenuItem value="POSITIVA">Positiva</MenuItem>
        <MenuItem value="NEGATIVA">Negativa</MenuItem>
      </TextField>
      <TextField
        label="Descripción"
        name="descripcion"
        value={form.descripcion}
        onChange={handleChange}
        multiline
        rows={3}
        required
      />
      <Button variant="contained" onClick={handleSubmit}>
        Crear anotación
      </Button>
    </Box>
  )
}

export default AnotacionForm