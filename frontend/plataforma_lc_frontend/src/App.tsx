import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'


import Layout from './components/common/Layout'
import Home from './pages/Home'

import ProfesorLoginPage from './pages/ProfesorLoginPage'
import MisCursosPage from './pages/profesor/MisCursosPage'


import keycloak from './keycloak'
import EvaluacionesPage from './pages/profesor/EvaluacionesPage'
import CursosPage from './pages/admin/CursosPage'
import EstudiantesPage from './pages/admin/EstudiantesPage'
import DetalleCursoPage from './pages/profesor/DetalleCursoPage'
import AsistenciaPage from './pages/profesor/AsistenciasPage'
import AsistenciasPage from './pages/admin/AsistenciasPage'
import ClasesPage from './pages/admin/ClasesPage'
import ClasesProfesorPage from './pages/profesor/ClasesProfesorPage'


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
            <Route path="asistencia" element={isAdmin ? <AsistenciasPage /> : <Navigate to="/" />} />
            <Route path="clase" element={isAdmin ? <ClasesPage /> : <Navigate to="/" />} />
          </Route>
          <Route path="profesor">
            <Route index element={<ProfesorLoginPage />} />
            <Route path="mis-cursos" element={isProfesor ? <MisCursosPage /> : <Navigate to="/" />} />
            <Route path="mis-cursos/:id" element={isProfesor ? <DetalleCursoPage /> : <Navigate to="/" />} />
            <Route path="mis-cursos/:id/evaluaciones" element={isProfesor ? <EvaluacionesPage /> : <Navigate to="/" />} />
            <Route path="mis-cursos/:id/asistencia" element={isProfesor ? <AsistenciaPage /> : <Navigate to="/" />} />
            <Route path="mis-cursos/:id/clase" element={isProfesor ? <ClasesProfesorPage /> : <Navigate to="/" />} />
          </Route>
          <Route path="*" element={<Navigate to="/" />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App