import { useEffect, useState } from 'react'
import {
  Box, Button, CircularProgress, Dialog, DialogContent,
  DialogTitle, IconButton, Paper, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, Typography
} from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import EditIcon from '@mui/icons-material/Edit'
import { getClases, createClase, updateClase, deleteClase } from '../../api/claseApi'
import ClaseForm from '../../components/clase/ClaseForm'
import type { Clase } from '../../types/Clase'

function ClasesPage() {
  const [clases, setClases] = useState<Clase[]>([])
  const [loading, setLoading] = useState(true)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [claseEditando, setClaseEditando] = useState<Clase | undefined>(undefined)

  const cargarClases = async () => {
    setLoading(true)
    try {
      const data = await getClases()
      setClases(data)
    } catch (error) {
      console.error('Error cargando clases:', error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    cargarClases()
  }, [])

  const handleAbrir = (clase?: Clase) => {
    setClaseEditando(clase)
    setDialogOpen(true)
  }

  const handleCerrar = () => {
    setClaseEditando(undefined)
    setDialogOpen(false)
  }

  const handleSubmit = async (clase: Clase) => {
    try {
      if (claseEditando?.id) {
        await updateClase(claseEditando.id, clase)
      } else {
        await createClase(clase)
      }
      handleCerrar()
      cargarClases()
    } catch (err: any) {
      // Atrapamos los ResponseEntity.badRequest() de Spring Boot
      const mensaje = err.response?.data || 'Error al guardar la clase.'
      alert(mensaje) 
    }
  }

  const handleEliminar = async (id: number) => {
    if (!confirm('¿Seguro que deseas eliminar esta clase? Las asistencias asociadas podrían verse afectadas.')) return
    try {
      await deleteClase(id)
      cargarClases()
    } catch (error) {
      console.error('Error eliminando clase:', error)
    }
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Typography variant="h5">Gestión de Clases</Typography>
        <Button variant="contained" onClick={() => handleAbrir()}>
          Registrar Nueva Clase
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
                <TableCell>ID Curso</TableCell>
                <TableCell>Fecha</TableCell>
                <TableCell>Descripción</TableCell>
                <TableCell>Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {clases.map(clase => (
                <TableRow key={clase.id}>
                  <TableCell>{clase.id}</TableCell>
                  <TableCell>{clase.cursoId}</TableCell>
                  <TableCell>
                    {clase.fecha ? String(clase.fecha).split('T')[0] : '—'}
                  </TableCell>
                  <TableCell>{clase.descripcion || '—'}</TableCell>
                  <TableCell>
                    <IconButton onClick={() => handleAbrir(clase)}>
                      <EditIcon />
                    </IconButton>
                    <IconButton onClick={() => handleEliminar(clase.id!)}>
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
        <DialogTitle>{claseEditando ? 'Editar Clase' : 'Nueva Clase'}</DialogTitle>
        <DialogContent>
          <ClaseForm onSubmit={handleSubmit} inicial={claseEditando} />
        </DialogContent>
      </Dialog>
    </Box>
  )
}

export default ClasesPage