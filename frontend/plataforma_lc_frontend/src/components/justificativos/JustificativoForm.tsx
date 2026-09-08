import { useEffect, useState } from 'react'
import { Box, Button, TextField, Typography } from '@mui/material'
import type { Justificativo } from '../../types/Justificativo'

interface Props {
  cursoId: number
  onSubmit: (justificativo: Justificativo) => void
  inicial?: Justificativo
}

function JustificativoForm({ cursoId, onSubmit, inicial }: Props) {
  const [form, setForm] = useState<Justificativo>(
    inicial ?? {
      estudianteId: 0,
      cursoId,
      fechaInasistencia: '',
      motivo: '',
    }
  )

  useEffect(() => {
    if (inicial) {
      setForm(inicial)
    } else {
      setForm({ estudianteId: 0, cursoId, fechaInasistencia: '', motivo: '' })
    }
  }, [inicial, cursoId])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
  }

  const handleSubmit = () => {
    if (!form.estudianteId || !form.fechaInasistencia || !form.motivo) return
    onSubmit(form)
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, maxWidth: 400 }}>
      <Typography variant="h6">Registrar Justificativo</Typography>
      <TextField
        label="ID Estudiante"
        name="estudianteId"
        type="number"
        value={form.estudianteId}
        onChange={handleChange}
        required
      />
      <TextField
        label="Fecha de inasistencia"
        name="fechaInasistencia"
        type="date"
        value={form.fechaInasistencia}
        onChange={handleChange}
        slotProps={{ inputLabel: { shrink: true } }}
        required
      />
      <TextField
        label="Motivo"
        name="motivo"
        value={form.motivo}
        onChange={handleChange}
        multiline
        rows={3}
        required
      />
      <Button variant="contained" onClick={handleSubmit}>
        Crear justificativo
      </Button>
    </Box>
  )
}

export default JustificativoForm