import { useEffect, useState } from 'react'
import {
  Box, Button, CircularProgress, Paper, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, Typography
} from '@mui/material'
import { useParams, useNavigate } from 'react-router-dom'
import { getClasesPorCurso } from '../../api/claseApi'
import type { Clase } from '../../types/Clase'

function ClasesProfesorPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const cursoId = Number(id)

  const [clases, setClases] = useState<Clase[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const cargarClases = async () => {
    setLoading(true)
    try {
      // Usamos el endpoint que definimos previamente para buscar por cursoId
      const data = await getClasesPorCurso(cursoId)
      setClases(data)
    } catch {
      setError('Error al cargar las clases del curso.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { 
    cargarClases() 
  }, [cursoId])

  if (loading) return <CircularProgress />
  if (error) return <Typography color="error">{error}</Typography>

  return (
    <Box>
      <Button variant="text" onClick={() => navigate(-1)} sx={{ mb: 1 }}>
        ← Volver
      </Button>
      
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Typography variant="h5">Cronograma de Clases - Curso {cursoId}</Typography>
        {/* Aquí NO incluimos el botón de "Registrar Clase" porque es 
            una vista para un usuario con rol de solo lectura (estudiante/apoderado) */}
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Fecha</TableCell>
              <TableCell>Descripción de la Clase</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {clases.map(c => (
              <TableRow key={c.id}>
                <TableCell>
                  {/* Se asume que c.fecha viene como string desde el backend */}
                  {c.fecha ? new Date(c.fecha).toLocaleDateString() : '—'}
                </TableCell>
                <TableCell>{c.descripcion || 'Sin descripción'}</TableCell>
              </TableRow>
            ))}
            
            {/* Mensaje amigable si el curso aún no tiene clases */}
            {clases.length === 0 && (
              <TableRow>
                <TableCell colSpan={2} align="center">
                  <Typography variant="body2" color="textSecondary" sx={{ py: 2 }}>
                    Aún no hay clases registradas para este curso.
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  )
}

export default ClasesProfesorPage