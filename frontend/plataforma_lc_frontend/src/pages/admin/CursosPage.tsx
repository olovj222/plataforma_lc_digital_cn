import { useEffect, useState } from 'react'
import {
  Box, Button, CircularProgress, Dialog, DialogContent,
  DialogTitle, IconButton, Paper, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, Typography
} from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import EditIcon from '@mui/icons-material/Edit'

import { getCursos, createCurso, updateCurso, deleteCurso } from '../../api/cursoApi'
import CursoForm from '../../components/curso/CursoForm'
import type { Curso } from '../../types/Curso'

function CursosPage() {
  const [cursos, setCursos] = useState<Curso[]>([])
  const [loading, setLoading] = useState(true)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [cursoEditando, setCursoEditando] = useState<Curso | undefined>(undefined)

  const cargarCursos = async () => {
    setLoading(true)
    try {
      const data = await getCursos()
      setCursos(data)
    } catch (error) {
      console.error('Error cargando cursos:', error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    cargarCursos()
  }, [])

  const handleAbrir = (curso?: Curso) => {
    setCursoEditando(curso)
    setDialogOpen(true)
  }

  const handleCerrar = () => {
    setCursoEditando(undefined)
    setDialogOpen(false)
  }

  const handleSubmit = async (curso: Curso) => {
    try {
      if (cursoEditando?.id) {
        await updateCurso(cursoEditando.id, curso)
      } else {
        await createCurso(curso)
      }
      handleCerrar()
      cargarCursos()
    } catch (error) {
      console.error('Error guardando curso:', error)
    }
  }

  const handleEliminar = async (id: number) => {
    if (!confirm('¿Eliminar este curso?')) return
    try {
      await deleteCurso(id)
      cargarCursos()
    } catch (error) {
      console.error('Error eliminando curso:', error)
    }
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Typography variant="h5">Gestión de Cursos</Typography>
        <Button variant="contained" onClick={() => handleAbrir()}>
          Nuevo Curso
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
                <TableCell>Código</TableCell>
                <TableCell>Profesor ID</TableCell>
                <TableCell>Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {cursos.map(curso => (
                <TableRow key={curso.id}>
                  <TableCell>{curso.id}</TableCell>
                  <TableCell>{curso.nombre}</TableCell>
                  <TableCell>{curso.codigo}</TableCell>
                  <TableCell>{curso.profesorId ?? '—'}</TableCell>
                  <TableCell>
                    <IconButton onClick={() => handleAbrir(curso)}>
                      <EditIcon />
                    </IconButton>
                    <IconButton onClick={() => handleEliminar(curso.id!)}>
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
        <DialogTitle>{cursoEditando ? 'Editar Curso' : 'Nuevo Curso'}</DialogTitle>
        <DialogContent>
          <CursoForm onSubmit={handleSubmit} inicial={cursoEditando} />
        </DialogContent>
      </Dialog>
    </Box>
  )
}

export default CursosPage