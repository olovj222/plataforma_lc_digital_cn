import { useState } from 'react'
import { Box, Button, TextField, Typography, Paper } from '@mui/material'
import { useNavigate } from 'react-router-dom'

function ProfesorLoginPage() {
  const [profesorId, setProfesorId] = useState('')
  const navigate = useNavigate()

  const handleIngresar = () => {
    if (!profesorId) return
    localStorage.setItem('profesorId', profesorId)
    navigate('/profesor/mis-cursos')
  }

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 8 }}>
      <Paper sx={{ p: 4, maxWidth: 400, width: '100%' }}>
        <Typography variant="h5" sx={{ mb: 3 }}>
          Acceso Profesor
        </Typography>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <TextField
            label="ID de Profesor"
            type="number"
            value={profesorId}
            onChange={e => setProfesorId(e.target.value)}
            required
          />
          <Button variant="contained" onClick={handleIngresar}>
            Ingresar
          </Button>
        </Box>
      </Paper>
    </Box>
  )
}

export default ProfesorLoginPage