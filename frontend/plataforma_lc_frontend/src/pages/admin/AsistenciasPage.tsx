import { useEffect, useState } from 'react'
import {
  Box, Button, CircularProgress, Dialog, DialogContent,
  DialogTitle, IconButton, Paper, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, Typography
} from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import EditIcon from '@mui/icons-material/Edit'
import { getAsistencia, createAsistencia, updateAsistencia, deleteAsistencia } from '../../api/asistenciaApi'
import AsistenciaForm from '../../components/asistencia/AsistenciaForm' 
import type { Asistencia } from '../../types/Asistencia'

function AsistenciasPage() {
  const [asistencias, setAsistencias] = useState<Asistencia[]>([])
  const [loading, setLoading] = useState(true)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [asistenciaEditando, setAsistenciaEditando] = useState<Asistencia | undefined>(undefined)

  const cargarAsistencias = async () => {
    setLoading(true)
    try {
      const data = await getAsistencia()
      setAsistencias(data)
    } catch (error) {
      console.error('Error cargando asistencias:', error)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    cargarAsistencias()
  }, [])

  const handleAbrir = (asistencia?: Asistencia) => {
    setAsistenciaEditando(asistencia)
    setDialogOpen(true)
  }

  const handleCerrar = () => {
    setAsistenciaEditando(undefined)
    setDialogOpen(false)
  }

  const handleSubmit = async (asistencia: Asistencia) => {
  try {
    if (asistenciaEditando?.id) {
      await updateAsistencia(asistenciaEditando.id, asistencia)
    } else {
      await createAsistencia(asistencia)
    }
    handleCerrar()
    cargarAsistencias()
  } catch (err: any) {
    const mensaje = err.response?.data || 'Error al registrar asistencia.'
    alert(mensaje)
  }
}

  const handleEliminar = async (id: number) => {
    if (!confirm('¿Eliminar este registro de asistencia?')) return
    try {
      await deleteAsistencia(id)
      cargarAsistencias()
    } catch (error) {
      console.error('Error eliminando asistencia:', error)
    }
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Typography variant="h5">Gestión de Asistencia</Typography>
        <Button variant="contained" onClick={() => handleAbrir()}>
          Registrar Asistencia
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
                <TableCell>Estudiante (ID)</TableCell>
                <TableCell>Curso (ID)</TableCell>
                <TableCell>Fecha</TableCell>
                <TableCell>Estado</TableCell>
                <TableCell>Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
  {asistencias.map(asistencia => (
    <TableRow key={asistencia.id}>
      {/* 1. ID */}
      <TableCell>{asistencia.id}</TableCell>
      
      {/* 2. Estudiante (ID) - Soporta múltiples formatos del backend */}
      <TableCell>
        {asistencia.id_estudiante || '—'}
      </TableCell>
      
      {/* 3. Curso (ID) - Soporta múltiples formatos del backend */}
      <TableCell>
        {asistencia.id_curso || '—'}
      </TableCell>
      
      {/* 4. Fecha */}
      <TableCell>
        {asistencia.fecha 
          ? String(asistencia.fecha).split('T')[0]
          : '—'}
      </TableCell>
      
      {/* 5. Estado */}
      <TableCell>{asistencia.estado || '—'}</TableCell>
      
      {/* 6. Acciones (Ahora alineado correctamente al final) */}
      <TableCell>
        <IconButton onClick={() => handleAbrir(asistencia)}>
          <EditIcon />
        </IconButton>
        <IconButton onClick={() => handleEliminar(asistencia.id!)}>
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
        <DialogTitle>{asistenciaEditando ? 'Editar Asistencia' : 'Nueva Asistencia'}</DialogTitle>
        <DialogContent>
          <AsistenciaForm onSubmit={handleSubmit} inicial={asistenciaEditando} />
        </DialogContent>
      </Dialog>
    </Box>
  )
}

export default AsistenciasPage