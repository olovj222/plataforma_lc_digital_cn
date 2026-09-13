import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { Box, Chip, CircularProgress, Table, TableBody, TableCell, TableHead, TableRow, Typography } from '@mui/material'
import { getJustificativosByCurso, crearJustificativo } from '../../api/justificativosApi'
import JustificativoForm from '../../components/justificativos/JustificativoForm'
import type { Justificativo } from '../../types/Justificativo'

function JustificativosProfesorPage() {
  const { id } = useParams()
  const cursoId = Number(id)
  const [justificativos, setJustificativos] = useState<Justificativo[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const cargar = async () => {
    try {
      const data = await getJustificativosByCurso(cursoId)
      setJustificativos(data)
    } catch {
      setError('No se pudo conectar con el servidor.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { cargar() }, [cursoId])

  const handleCrear = async (justificativo: Justificativo) => {
    await crearJustificativo(justificativo)
    cargar()
  }

  if (loading) return <CircularProgress />
  if (error) return <Typography color="error">{error}</Typography>

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
      <Typography variant="h5">Justificativos de Inasistencia</Typography>

      <JustificativoForm cursoId={cursoId} onSubmit={handleCrear} />

      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Estudiante</TableCell>
            <TableCell>Fecha</TableCell>
            <TableCell>Motivo</TableCell>
            <TableCell>Estado</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {justificativos.map(j => (
            <TableRow key={j.id}>
              <TableCell>{j.estudianteId}</TableCell>
              <TableCell>{j.fechaInasistencia}</TableCell>
              <TableCell>{j.motivo}</TableCell>
              <TableCell>
                <Chip
                  label={j.estado}
                  color={j.estado === 'APROBADO' ? 'success' : j.estado === 'RECHAZADO' ? 'error' : 'warning'}
                  size="small"
                />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Box>
  )
}

export default JustificativosProfesorPage