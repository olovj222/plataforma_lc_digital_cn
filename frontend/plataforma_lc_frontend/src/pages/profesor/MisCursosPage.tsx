import { useEffect, useState } from 'react'
import {
  Box, Card, CardContent, CardActionArea, Typography,
  CircularProgress
} from '@mui/material'
import { useNavigate } from 'react-router-dom'

import { getCursosByProfesor } from '../../api/cursoApi'
import type { Curso } from '../../types/Curso'
import keycloak from '../../keycloak'

function MisCursosPage() {
  const [cursos, setCursos] = useState<Curso[]>([])
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)

  const profesorId = keycloak.tokenParsed?.sub ?? ''

  useEffect(() => {
    const cargar = async () => {
      try {
        const data = await getCursosByProfesor(profesorId)
        setCursos(data)
      } catch (error) {
        setError('No se pudo conectar con el servidor. Verifique que los servicios estén activos.')
      } finally {
        setLoading(false)
      }
    }
    cargar()
  }, [])

  return (
    <Box>
    <Typography variant="h5" sx={{ mb: 3 }}>Mis Cursos</Typography>

    {loading ? (
      <CircularProgress />
    ) : error ? (
      <Typography color="error">{error}</Typography>
    ) : cursos.length === 0 ? (
      <Typography>No tienes cursos asignados.</Typography>
    ) : (
    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2 }}>
  {cursos.map(curso => (
    <Box key={curso.id} sx={{ width: { xs: '100%', sm: '45%', md: '30%' } }}>
      <Card>
        <CardActionArea onClick={() => navigate(`/profesor/mis-cursos/${curso.id}`)}>
          <CardContent>
            <Typography variant="h6">{curso.nombre}</Typography>
            <Typography color="text.secondary">Código: {curso.codigo}</Typography>
          </CardContent>
        </CardActionArea>
      </Card>
    </Box>
  ))}
    </Box>
  )}
</Box>
  )
}

export default MisCursosPage