import { Typography, Box } from '@mui/material'

function Home() {
  return (
    <Box>
      <Typography variant="h4">Bienvenido al Libro de Clases Digital</Typography>
      <Typography variant="body1" sx={{ mt: 2 }}>
        Selecciona una opción del menú para comenzar.
      </Typography>
    </Box>
  )
}

export default Home