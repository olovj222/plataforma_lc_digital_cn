import { useEffect, useState } from 'react'
import {
  Box, Button, CircularProgress, Paper, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, Typography
} from '@mui/material'
import { useNavigate, useParams } from 'react-router-dom'

import { getCursoById } from '../../api/cursoApi'
import { getEstudiantes } from '../../api/estudianteApi'
import type { Curso } from '../../types/Curso'
import type { Estudiante } from '../../types/Estudiante'

function DetalleCursoPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [curso, setCurso] = useState<Curso | null>(null)
  const [estudiantes, setEstudiantes] = useState<Estudiante[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const cargar = async () => {
      try {
        const cursoData = await getCursoById(Number(id))
        setCurso(cursoData)

        // Filtramos estudiantes que tengan este cursoId
        const todosEstudiantes = await getEstudiantes()
        const filtrados = todosEstudiantes.filter(e =>
          e.cursos.some(c => c.cursoId === Number(id))
        )
        setEstudiantes(filtrados)
      } catch (error) {
        setError('No se pudo conectar con el servidor. Verifique que los servicios estén activos.')
      } finally {
        setLoading(false)
      }
    }
    cargar()
  }, [id])

  if (loading) return <CircularProgress />
  if (error) return <Typography color="error">{error}</Typography>

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 1 }}>{curso?.nombre}</Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>Código: {curso?.codigo}</Typography>

      <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
        <Button variant="outlined" onClick={() => navigate(`/profesor/mis-cursos/${id}/asistencia`)}>
          Registrar Asistencia
        </Button>
        <Button variant="outlined" onClick={() => navigate(`/profesor/mis-cursos/${id}/evaluaciones`)}>
          Registrar Evaluaciones
        </Button>
      </Box>

      <Typography variant="h6" sx={{ mb: 2 }}>Estudiantes</Typography>
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Nombre</TableCell>
              <TableCell>Apellido Paterno</TableCell>
              <TableCell>Apellido Materno</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {estudiantes.map(e => (
              <TableRow key={e.id}>
                <TableCell>{e.id}</TableCell>
                <TableCell>{e.nombre}</TableCell>
                <TableCell>{e.apPaterno}</TableCell>
                <TableCell>{e.apMaterno ?? '—'}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  )
}

export default DetalleCursoPage