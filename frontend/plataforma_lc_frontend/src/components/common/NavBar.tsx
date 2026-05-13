import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material'
import { useNavigate } from 'react-router-dom'

function Navbar() {
  const navigate = useNavigate()

  return (
    <AppBar position="static">
      <Toolbar>
        <Typography variant="h6" sx={{ flexGrow: 1 }}>
          Libro de Clases Digital
        </Typography>
        <Box>
          <Button color="inherit" onClick={() => navigate('/admin/cursos')}>
            Cursos
          </Button>
          <Button color="inherit" onClick={() => navigate('/admin/estudiantes')}>
            Estudiantes
          </Button>
        </Box>
      </Toolbar>
    </AppBar>
  )
}

export default Navbar