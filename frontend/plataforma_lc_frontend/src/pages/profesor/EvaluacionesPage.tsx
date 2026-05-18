import { useEffect, useState } from 'react'
import {
  Box, Button, CircularProgress, Dialog, DialogContent,
  DialogTitle, IconButton, Paper, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, Typography,
  TextField
} from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import { useParams, useNavigate } from 'react-router-dom'
import {
  getEvaluacionesPorCurso, crearEvaluacion,
  eliminarEvaluacion, type Evaluacion
} from '../../api/evaluacionesApi'
import { getEstudiantes } from '../../api/estudianteApi'
import type { Estudiante } from '../../types/Estudiante'

function EvaluacionesPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const cursoId = Number(id)

  const [evaluaciones, setEvaluaciones] = useState<Evaluacion[]>([])
  const [estudiantes, setEstudiantes] = useState<Estudiante[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [form, setForm] = useState<Evaluacion>({
    nombre: '', cursoId, estudianteId: 0, calificacion: 0
  })

  const cargar = async () => {
    setLoading(true)
    try {
      const [evals, todos] = await Promise.all([
        getEvaluacionesPorCurso(cursoId),
        getEstudiantes()
      ])
      setEvaluaciones(evals)
      setEstudiantes(todos.filter(e => e.cursos.some(c => c.cursoId === cursoId)))
    } catch {
      setError('No se pudo conectar con el servidor.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { cargar() }, [])

  const handleSubmit = async () => {
    try {
      await crearEvaluacion(form)
      setDialogOpen(false)
      setForm({ nombre: '', cursoId, estudianteId: 0, calificacion: 0 })
      cargar()
    } catch {
      setError('Error al crear evaluación.')
    }
  }

  const handleEliminar = async (evalId: number) => {
    if (!confirm('¿Eliminar esta evaluación?')) return
    await eliminarEvaluacion(evalId)
    cargar()
  }

  const getNombreEstudiante = (estudianteId: number) => {
    const e = estudiantes.find(est => est.id === estudianteId)
    return e ? `${e.nombre} ${e.apPaterno}` : `ID: ${estudianteId}`
  }

  if (loading) return <CircularProgress />
  if (error) return <Typography color="error">{error}</Typography>

  return (
    <Box>
      <Button variant="text" onClick={() => navigate(-1)} sx={{ mb: 1 }}>
        ← Volver
      </Button>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
        <Typography variant="h5">Evaluaciones del Curso</Typography>
        <Button variant="contained" onClick={() => setDialogOpen(true)}>
          Nueva Evaluación
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Nombre</TableCell>
              <TableCell>Estudiante</TableCell>
              <TableCell>Calificación</TableCell>
              <TableCell>Acciones</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {evaluaciones.map(ev => (
              <TableRow key={ev.id}>
                <TableCell>{ev.nombre}</TableCell>
                <TableCell>{getNombreEstudiante(ev.estudianteId)}</TableCell>
                <TableCell>{ev.calificacion}</TableCell>
                <TableCell>
                  <IconButton onClick={() => handleEliminar(ev.id!)}>
                    <DeleteIcon color="error" />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)}>
        <DialogTitle>Nueva Evaluación</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1, minWidth: 350 }}>
            <TextField
              label="Nombre"
              value={form.nombre}
              onChange={e => setForm(prev => ({ ...prev, nombre: e.target.value }))}
            />
            <TextField
              label="ID Estudiante"
              type="number"
              value={form.estudianteId}
              onChange={e => setForm(prev => ({ ...prev, estudianteId: Number(e.target.value) }))}
            />
            <TextField
              label="Calificación"
              type="number"
              value={form.calificacion}
              onChange={e => setForm(prev => ({ ...prev, calificacion: Number(e.target.value) }))}
            />
            <Button variant="contained" onClick={handleSubmit}>Guardar</Button>
          </Box>
        </DialogContent>
      </Dialog>
    </Box>
  )
}

export default EvaluacionesPage