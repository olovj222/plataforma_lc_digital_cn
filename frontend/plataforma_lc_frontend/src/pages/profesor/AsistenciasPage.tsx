import { useEffect, useState } from 'react'
import {
  Box, Button, CircularProgress, Dialog, DialogContent,
  DialogTitle, MenuItem, Paper, Select, Table, TableBody,
  TableCell, TableContainer, TableHead, TableRow, Typography,
  TextField, FormControl, InputLabel, IconButton
} from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import { useParams, useNavigate } from 'react-router-dom'
import { getAsistenciasPorCurso, createAsistencia, deleteAsistencia } from '../../api/asistenciaApi'
import { getEstudiantes } from '../../api/estudianteApi'
import type { Asistencia } from '../../types/Asistencia'
import type { Estudiante, EstudianteCurso } from '../../types/Estudiante'

function AsistenciaPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const cursoId = Number(id)

  const [asistencias, setAsistencias] = useState<Asistencia[]>([])
  const [estudiantes, setEstudiantes] = useState<Estudiante[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [form, setForm] = useState<Asistencia>({
    id_clase: cursoId,
    id_estudiante: 0,
    estado: 'PRESENT',
    fecha: new Date()
  })

  const cargar = async () => {
    setLoading(true)
    try {
      const [asis, todos] = await Promise.all([
        getAsistenciasPorCurso(cursoId),
        getEstudiantes()
      ])
      setAsistencias(asis)
      setEstudiantes(todos.filter((e: Estudiante) => e.cursos.some((c: EstudianteCurso) => Number(c.cursoId) === cursoId)))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { cargar() }, [])

  const handleSubmit = async () => {
    try {
      await createAsistencia(form)
      setDialogOpen(false)
      setForm({ id_clase: cursoId, id_estudiante: 0, estado: 'PRESENT', fecha: new Date() })
      cargar()
    } catch {
      setError('Error al registrar asistencia.')
    }
  }

  const handleEliminar = async (asistenciaId: number) => {
    if (!confirm('¿Eliminar este registro?')) return
    await deleteAsistencia(asistenciaId)
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
        <Typography variant="h5">Registro de Asistencia</Typography>
        <Button variant="contained" onClick={() => setDialogOpen(true)}>
          Registrar Asistencia
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Estudiante</TableCell>
              <TableCell>Estado</TableCell>
              <TableCell>Fecha</TableCell>
              <TableCell>Acciones</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {asistencias.map(a => (
              <TableRow key={a.id}>
                <TableCell>{getNombreEstudiante(a.id_estudiante)}</TableCell>
                <TableCell>{a.estado}</TableCell>
                <TableCell>{a.fecha ? new Date(a.fecha).toLocaleDateString() : '—'}</TableCell>
                <TableCell>
                  <IconButton onClick={() => handleEliminar(a.id!)}>
                    <DeleteIcon color="error" />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)}>
        <DialogTitle>Registrar Asistencia</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1, minWidth: 350 }}>
            <FormControl>
              <InputLabel>Estudiante</InputLabel>
              <Select
                value={form.id_estudiante}
                label="Estudiante"
                onChange={e => setForm(prev => ({ ...prev, id_estudiante: Number(e.target.value) }))}
              >
                {estudiantes.map(e => (
                  <MenuItem key={e.id} value={e.id}>
                    {e.nombre} {e.apPaterno}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <FormControl>
              <InputLabel>Estado</InputLabel>
              <Select
                value={form.estado}
                label="Estado"
                onChange={e => setForm(prev => ({ ...prev, estado: e.target.value }))}
              >
                <MenuItem value="PRESENT">Presente</MenuItem>
                <MenuItem value="ABSENT">Ausente</MenuItem>
                <MenuItem value="JUSTIFIED">Justificado</MenuItem>
              </Select>
            </FormControl>
            <TextField
              label="Fecha"
              type="date"
              slotProps={{ inputLabel: { shrink: true } }}
              onChange={e => setForm(prev => ({ ...prev, fecha: new Date(e.target.value) }))}
            />
            <Button variant="contained" onClick={handleSubmit}>Guardar</Button>
          </Box>
        </DialogContent>
      </Dialog>
    </Box>
  )
}

export default AsistenciaPage