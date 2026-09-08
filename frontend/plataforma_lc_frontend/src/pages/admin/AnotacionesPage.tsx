import { useState } from 'react'
import { Box, Button, Chip, IconButton, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography } from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import { getAnotacionesByEstudiante, eliminarAnotacion } from '../../api/anotacionesApi'
import type { Anotacion } from '../../types/Anotacion'

function AnotacionesPage() {
  const [estudianteId, setEstudianteId] = useState('')
  const [anotaciones, setAnotaciones] = useState<Anotacion[]>([])

  const buscar = async () => {
    if (!estudianteId) return
    const data = await getAnotacionesByEstudiante(Number(estudianteId))
    setAnotaciones(data)
  }

  const eliminar = async (id?: number) => {
    if (!id) return
    await eliminarAnotacion(id)
    buscar()
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Typography variant="h5">Anotaciones por Estudiante</Typography>

      <Box sx={{ display: 'flex', gap: 2, maxWidth: 400 }}>
        <TextField
          label="ID Estudiante"
          value={estudianteId}
          onChange={e => setEstudianteId(e.target.value)}
        />
        <Button variant="contained" onClick={buscar}>Buscar</Button>
      </Box>

      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Tipo</TableCell>
            <TableCell>Descripción</TableCell>
            <TableCell>Fecha</TableCell>
            <TableCell>Acciones</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {anotaciones.map(a => (
            <TableRow key={a.id}>
              <TableCell>
                <Chip label={a.tipo} color={a.tipo === 'POSITIVA' ? 'success' : 'error'} size="small" />
              </TableCell>
              <TableCell>{a.descripcion}</TableCell>
              <TableCell>{a.fecha}</TableCell>
              <TableCell>
                <IconButton onClick={() => eliminar(a.id)}>
                  <DeleteIcon color="error" />
                </IconButton>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Box>
  )
}

export default AnotacionesPage