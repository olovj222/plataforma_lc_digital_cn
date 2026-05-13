import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'

import EstudiantesPage from './pages/EstudiantesPage'
import Layout from './components/common/Layout'
import Home from './pages/Home'
import CursosPage from './pages/CursosPage'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout/>}>
          <Route index element={<Home />} />
          <Route path="admin">
            <Route path="cursos" element={<CursosPage />} />
            <Route path="estudiantes" element={<EstudiantesPage />} />
          </Route>
          <Route path="*" element={<Navigate to="/" />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App