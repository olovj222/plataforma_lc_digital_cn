import { useEffect, useState } from 'react'
import { Box, Button, Chip, Table, TableBody, TableCell, TableHead, TableRow, Typography } from '@mui/material'
import { getJustificativosPendientes, aprobarJustificativo, rechazarJustificativo } from '../../api/justificativosApi'
import type { Justificativo } from '../../types/Justificativo'

function JustificativosPage() {
  const [pendientes, setPendientes] = useState<Justificativo[]>([])

  const cargar = async () => {
    const data = await getJustificativosPendientes()
    setPendientes(data)
  }

  useEffect(() => { cargar() }, [])

  const aprobar = async (id?: number) => {
    if (!id) return
    await aprobarJustificativo(id)
    cargar()
  }

  const rechazar = async (id?: number) => {
    if (!id) return
    await rechazarJustificativo(id)
    cargar()
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Typography variant="h5">Justificativos Pendientes</Typography>

      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Estudiante</TableCell>
            <TableCell>Curso</TableCell>
            <TableCell>Fecha</TableCell>
            <TableCell>Motivo</TableCell>
            <TableCell>Acciones</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {pendientes.map(j => (
            <TableRow key={j.id}>
              <TableCell>{j.estudianteId}</TableCell>
              <TableCell>{j.cursoId}</TableCell>
              <TableCell>{j.fechaInasistencia}</TableCell>
              <TableCell>{j.motivo}</TableCell>
              <TableCell sx={{ display: 'flex', gap: 1 }}>
                <Button size="small" variant="contained" color="success" onClick={() => aprobar(j.id)}>
                  Aprobar
                </Button>
                <Button size="small" variant="outlined" color="error" onClick={() => rechazar(j.id)}>
                  Rechazar
                </Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Box>
  )
}

export default JustificativosPage