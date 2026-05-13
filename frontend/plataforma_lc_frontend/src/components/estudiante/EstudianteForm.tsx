import { useState } from 'react'
import { Box, Button, TextField, Typography, IconButton } from '@mui/material'
import AddIcon from '@mui/icons-material/Add'
import DeleteIcon from '@mui/icons-material/Delete'
import type { Estudiante } from '../../types/Estudiante'

interface Props {
  onSubmit: (estudiante: Estudiante) => void
  inicial?: Estudiante
}

function EstudianteForm({ onSubmit, inicial }: Props) {
  const [form, setForm] = useState<Estudiante>(
    inicial ?? {
      nombre: '',
      apPaterno: '',
      apMaterno: '',
      direccion: '',
      telefono: '',
      cursos: [{ cursoId: 0 }]
    }
  )

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
  }

  const handleCursoChange = (index: number, value: string) => {
    const nuevosCursos = [...form.cursos]
    nuevosCursos[index] = { cursoId: Number(value) }
    setForm(prev => ({ ...prev, cursos: nuevosCursos }))
  }

  const agregarCurso = () => {
    setForm(prev => ({ ...prev, cursos: [...prev.cursos, { cursoId: 0 }] }))
  }

  const eliminarCurso = (index: number) => {
    const nuevosCursos = form.cursos.filter((_, i) => i !== index)
    setForm(prev => ({ ...prev, cursos: nuevosCursos }))
  }

  const handleSubmit = () => {
    if (!form.nombre || !form.apPaterno || form.cursos.length === 0) return
    onSubmit(form)
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, maxWidth: 400 }}>
      <Typography variant="h6">
        {inicial ? 'Editar Estudiante' : 'Nuevo Estudiante'}
      </Typography>
      <TextField label="Nombre" name="nombre" value={form.nombre} onChange={handleChange} required />
      <TextField label="Apellido Paterno" name="apPaterno" value={form.apPaterno} onChange={handleChange} required />
      <TextField label="Apellido Materno" name="apMaterno" value={form.apMaterno ?? ''} onChange={handleChange} />
      <TextField label="Dirección" name="direccion" value={form.direccion ?? ''} onChange={handleChange} />
      <TextField label="Teléfono" name="telefono" value={form.telefono ?? ''} onChange={handleChange} />

      <Typography variant="subtitle1">Cursos asignados</Typography>
      {form.cursos.map((curso, index) => (
        <Box key={index} sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
          <TextField
            label={`Curso ID ${index + 1}`}
            type="number"
            value={curso.cursoId}
            onChange={e => handleCursoChange(index, e.target.value)}
            fullWidth
          />
          {form.cursos.length > 1 && (
            <IconButton onClick={() => eliminarCurso(index)}>
              <DeleteIcon color="error" />
            </IconButton>
          )}
        </Box>
      ))}
      <Button startIcon={<AddIcon />} onClick={agregarCurso} variant="outlined">
        Agregar curso
      </Button>

      <Button variant="contained" onClick={handleSubmit}>
        {inicial ? 'Guardar cambios' : 'Crear estudiante'}
      </Button>
    </Box>
  )
}

export default EstudianteForm