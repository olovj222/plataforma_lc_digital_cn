import { useEffect, useState } from 'react'
import { Box, Chip, IconButton, Table, TableBody, TableCell, TableHead, TableRow, Typography } from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import { getTodasLasAnotaciones, eliminarAnotacion } from '../../api/anotacionesApi'
import type { Anotacion } from '../../types/Anotacion'

function AnotacionesAdminPage() {
  const [anotaciones, setAnotaciones] = useState<Anotacion[]>([])

  const cargar = async () => {
    const data = await getTodasLasAnotaciones()
    setAnotaciones(data)
  }

  useEffect(() => { cargar() }, [])

  const eliminar = async (id?: number) => {
    if (!id) return
    await eliminarAnotacion(id)
    cargar()
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Typography variant="h5">Todas las Anotaciones (Administrador)</Typography>

      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Estudiante ID</TableCell>
            <TableCell>Tipo</TableCell>
            <TableCell>Descripción</TableCell>
            <TableCell>Fecha</TableCell>
            <TableCell>Acciones</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {anotaciones.map(a => (
            <TableRow key={a.id}>
              <TableCell>{a.estudianteId}</TableCell>
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

export default AnotacionesAdminPage