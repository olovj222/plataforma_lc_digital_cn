import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'

import EstudiantesPage from './pages/EstudiantesPage'
import Layout from './components/common/Layout'
import Home from './pages/Home'
import CursosPage from './pages/CursosPage'
import ProfesorLoginPage from './pages/ProfesorLoginPage'
import MisCursosPage from './pages/MisCursosPage'
import DetalleCursoPage from './pages/DetalleCursoPage'
import AsistenciasPage from './pages/AsistenciasPage'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout/>}>
          <Route index element={<Home />} />
          <Route path="admin">
            <Route path="cursos" element={<CursosPage />} />
            <Route path="estudiantes" element={<EstudiantesPage />} />
            <Route path="asistencias" element={<AsistenciasPage />} />
          </Route>
          <Route path="*" element={<Navigate to="/" />} />
        </Route>
        <Route path="profesor">
          <Route index element={<ProfesorLoginPage />} />
          <Route path="mis-cursos" element={<MisCursosPage />} />
          <Route path="mis-cursos/:id" element={<DetalleCursoPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App