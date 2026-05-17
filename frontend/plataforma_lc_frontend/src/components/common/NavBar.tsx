import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import keycloak from '../../keycloak'

function Navbar() {
  const navigate = useNavigate()
  const roles = keycloak.tokenParsed?.realm_access?.roles ?? []
  const isAdmin = roles.includes('ADMIN')
  const isProfesor = roles.includes('PROFESOR')

  return (
    <AppBar position="static">
      <Toolbar>
        <Typography variant="h6" sx={{ flexGrow: 1 }}>
          Libro de Clases Digital
        </Typography>
        <Box>
          {isAdmin && (
            <>
              <Button color="inherit" onClick={() => navigate('/admin/cursos')}>
                Cursos
              </Button>
              <Button color="inherit" onClick={() => navigate('/admin/estudiantes')}>
                Estudiantes
              </Button>
              <Button color="inherit" onClick={() => navigate('/admin/asistencia')}>
                Asistencia
                </Button>
            </>
          )}
          {isProfesor && (
            <Button color="inherit" onClick={() => navigate('/profesor/mis-cursos')}>
              Mis Cursos
            </Button>
          )}
          <Button color="inherit" onClick={() => keycloak.logout()}>
            Cerrar Sesión
          </Button>
        </Box>
      </Toolbar>
    </AppBar>
  )
}

export default Navbar