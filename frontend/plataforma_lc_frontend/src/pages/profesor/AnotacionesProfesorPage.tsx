import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { Box, Chip, CircularProgress, Table, TableBody, TableCell, TableHead, TableRow, Typography } from '@mui/material'
import { getAnotacionesByCurso, crearAnotacion } from '../../api/anotacionesApi'
import AnotacionForm from '../../components/anotaciones/AnotacionForm'
import type { Anotacion } from '../../types/Anotacion'

function AnotacionesProfesorPage() {
  const { id } = useParams()
  const cursoId = Number(id)
  const [anotaciones, setAnotaciones] = useState<Anotacion[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const cargar = async () => {
    try {
      const data = await getAnotacionesByCurso(cursoId)
      setAnotaciones(data)
    } catch {
      setError('No se pudo conectar con el servidor.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { cargar() }, [cursoId])

  const handleCrear = async (anotacion: Anotacion) => {
    await crearAnotacion(anotacion)
    cargar()
  }

  if (loading) return <CircularProgress />
  if (error) return <Typography color="error">{error}</Typography>

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      <Typography variant="h5">Anotaciones</Typography>

      <AnotacionForm cursoId={cursoId} onSubmit={handleCrear} />

      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Estudiante</TableCell>
            <TableCell>Tipo</TableCell>
            <TableCell>Descripción</TableCell>
            <TableCell>Fecha</TableCell>
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
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Box>
  )
}

export default AnotacionesProfesorPage