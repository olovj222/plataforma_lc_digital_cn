import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'

import EstudiantesPage from './pages/EstudiantesPage'
import Layout from './components/common/Layout'
import Home from './pages/Home'
import CursosPage from './pages/CursosPage'
import ProfesorLoginPage from './pages/ProfesorLoginPage'
import MisCursosPage from './pages/MisCursosPage'
import DetalleCursoPage from './pages/DetalleCursoPage'
import AsistenciasPage from './pages/AsistenciasPage'
import keycloak from './keycloak'


function App() {
  const roles = keycloak.tokenParsed?.realm_access?.roles ?? []
  const isAdmin = roles.includes('ADMIN')
  const isProfesor = roles.includes('PROFESOR')

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={
            isAdmin ? <Navigate to="/admin/cursos" /> :
            isProfesor ? <Navigate to="/profesor/mis-cursos" /> :
            <Home />
          } />
          <Route path="admin">
            <Route path="cursos" element={isAdmin ? <CursosPage /> : <Navigate to="/" />} />
            <Route path="estudiantes" element={isAdmin ? <EstudiantesPage /> : <Navigate to="/" />} />
          </Route>
          <Route path="profesor">
            <Route index element={<ProfesorLoginPage />} />
            <Route path="mis-cursos" element={isProfesor ? <MisCursosPage /> : <Navigate to="/" />} />
            <Route path="mis-cursos/:id" element={isProfesor ? <DetalleCursoPage /> : <Navigate to="/" />} />
          </Route>
          <Route path="*" element={<Navigate to="/" />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App