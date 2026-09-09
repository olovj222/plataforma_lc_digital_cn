import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { useMsal } from '@azure/msal-react'

function Navbar() {
  const navigate = useNavigate()
  const { instance, accounts } = useMsal()
  const currentAccount = accounts[0]
  const roles = currentAccount?.idTokenClaims?.roles ?? []
  const isAdmin = roles.includes('Administrador')
  const isProfesor = roles.includes('PROFESOR')

  const handleLogout = () => {
    instance.logoutRedirect()
  }

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
              <Button color="inherit" onClick={() => navigate('/admin/clase')}>
                Clase
              </Button>
              <Button color="inherit" onClick={() => navigate('/admin/justificativos')}>
                Justificativos
              </Button>
              <Button color="inherit" onClick={() => navigate('/admin/anotaciones')}>
                Anotaciones
              </Button>
            </>
          )}

          {isProfesor && (
            <Button color="inherit" onClick={() => navigate('/profesor/mis-cursos')}>
              Mis Cursos
            </Button>
          )}
          <Button color="inherit" onClick={() => handleLogout()}>
            Cerrar Sesión
          </Button>
        </Box>
      </Toolbar>
    </AppBar>
  )
}

export default Navbar