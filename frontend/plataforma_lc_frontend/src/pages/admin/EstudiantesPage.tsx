import { useEffect, useState } from 'react'
import {
  Box, Button, CircularProgress, Dialog, DialogContent,
  DialogTitle, IconButton, Paper, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, Typography
} from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import EditIcon from '@mui/icons-material/Edit'
import { getEstudiantes, createEstudiante, updateEstudiante, deleteEstudiante } from '../../api/estudianteApi'
import EstudianteForm from '../../components/estudiante/EstudianteForm'
import type { Estudiante } from '../../types/Estudiante'

function EstudiantesPage() {
  const [estudiantes, setEstudiantes] = useState<Estudiante[]>([])
  const [loading, setLoading] = useState(true)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [estudianteEditando, setEstudianteEditando] = useState<Estudiante | undefined>(undefined)

  const cargarEstudiantes = async () => {
    setLoading(true)
    try {
      const data = await getEstudiantes()
      setEstudiantes(data)
    } catch (error) {
      console.error('Error cargando estudiantes:', error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    cargarEstudiantes()
  }, [])

  const handleAbrir = (estudiante?: Estudiante) => {
    setEstudianteEditando(estudiante)
    setDialogOpen(true)
  }

  const handleCerrar = () => {
    setEstudianteEditando(undefined)
    setDialogOpen(false)
  }

  const handleSubmit = async (estudiante: Estudiante) => {
    try {
      if (estudianteEditando?.id) {
        await updateEstudiante(estudianteEditando.id, estudiante)
      } else {
        await createEstudiante(estudiante)
      }
      handleCerrar()
      cargarEstudiantes()
    } catch (error) {
      console.error('Error guardando estudiante:', error)
    }
  }

  const handleEliminar = async (id: number) => {
    if (!confirm('¿Eliminar este estudiante?')) return
    try {
      await deleteEstudiante(id)
      cargarEstudiantes()
    } catch (error) {
      console.error('Error eliminando estudiante:', error)
    }
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Typography variant="h5">Gestión de Estudiantes</Typography>
        <Button variant="contained" onClick={() => handleAbrir()}>
          Nuevo Estudiante
        </Button>
      </Box>

      {loading ? (
        <CircularProgress />
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Nombre</TableCell>
                <TableCell>Apellido Paterno</TableCell>
                <TableCell>Apellido Materno</TableCell>
                <TableCell>Teléfono</TableCell>
                <TableCell>Cursos</TableCell>
                <TableCell>Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {estudiantes.map(estudiante => (
                <TableRow key={estudiante.id}>
                  <TableCell>{estudiante.id}</TableCell>
                  <TableCell>{estudiante.nombre}</TableCell>
                  <TableCell>{estudiante.apPaterno}</TableCell>
                  <TableCell>{estudiante.apMaterno ?? '—'}</TableCell>
                  <TableCell>{estudiante.telefono ?? '—'}</TableCell>
                  <TableCell>
                    {estudiante.cursos.map(c => c.cursoName ?? `ID: ${c.cursoId}`).join(', ')}
                  </TableCell>
                  <TableCell>
                    <IconButton onClick={() => handleAbrir(estudiante)}>
                      <EditIcon />
                    </IconButton>
                    <IconButton onClick={() => handleEliminar(estudiante.id!)}>
                      <DeleteIcon color="error" />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={dialogOpen} onClose={handleCerrar}>
        <DialogTitle>{estudianteEditando ? 'Editar Estudiante' : 'Nuevo Estudiante'}</DialogTitle>
        <DialogContent>
          <EstudianteForm onSubmit={handleSubmit} inicial={estudianteEditando} />
        </DialogContent>
      </Dialog>
    </Box>
  )
}

export default EstudiantesPage