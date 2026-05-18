import { useState, useEffect } from 'react'
import { Box, Button, TextField, Typography } from '@mui/material'
import type { Curso } from '../../types/Curso'

interface Props {
  onSubmit: (curso: Curso) => void
  inicial?: Curso
}

function CursoForm({ onSubmit, inicial }: Props) {
  const [form, setForm] = useState<Curso>(
    inicial ?? { nombre: '', codigo: 0, profesorId: '' }
  ) 
  useEffect(() => {
  if (inicial) {
    setForm(inicial)
  } else {
    setForm({ nombre: '', codigo: 0, profesorId: '' })
  }
}, [inicial])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setForm(prev => ({
      ...prev,
      [name]: name === 'codigo' ? Number(value) : value
    }))
  }

  const handleSubmit = () => {
    if (!form.nombre || !form.codigo) return
    onSubmit(form)
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, maxWidth: 400 }}>
      <Typography variant="h6">
        {inicial ? 'Editar Curso' : 'Nuevo Curso'}
      </Typography>
      <TextField
        label="Nombre"
        name="nombre"
        value={form.nombre}
        onChange={handleChange}
        required
      />
      <TextField
        label="Código"
        name="codigo"
        type="number"
        value={form.codigo}
        onChange={handleChange}
        required
      />
      <TextField
        label="ID Profesor"
        name="profesorId"
<<<<<<< HEAD
        type="text"
=======
        type="string"
>>>>>>> develop
        value={form.profesorId}
        onChange={handleChange}
      />
      <Button variant="contained" onClick={handleSubmit}>
        {inicial ? 'Guardar cambios' : 'Crear curso'}
      </Button>
    </Box>
  )
}

export default CursoForm