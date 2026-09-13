import { Outlet } from 'react-router-dom'
import { Box } from '@mui/material'
import Navbar from './NavBar'

function Layout() {
  return (
    <>
      <Navbar />
      <Box sx={{ padding: 3 }}>
        <Outlet />
      </Box>
    </>
  )
}

export default Layout