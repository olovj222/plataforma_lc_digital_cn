import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/common/Layout'
import Home from './pages/Home'
import ProfesorLoginPage from './pages/ProfesorLoginPage'
import MisCursosPage from './pages/profesor/MisCursosPage'
import EvaluacionesPage from './pages/profesor/EvaluacionesPage'
import CursosPage from './pages/admin/CursosPage'
import EstudiantesPage from './pages/admin/EstudiantesPage'
import DetalleCursoPage from './pages/profesor/DetalleCursoPage'
import AsistenciaPage from './pages/profesor/AsistenciasPage'
import AsistenciasPage from './pages/admin/AsistenciasPage'
import ClasesPage from './pages/admin/ClasesPage'
import ClasesProfesorPage from './pages/profesor/ClasesProfesorPage'
import { MsalAuthenticationTemplate, useIsAuthenticated, useMsal } from '@azure/msal-react';
import { InteractionType } from '@azure/msal-browser';
import { useEffect } from 'react'

function App() {
  const { accounts } = useMsal();
  const currentAccount = accounts[0]; 
  const roles = currentAccount?.idTokenClaims?.roles ?? [];
  const isAdmin = roles.includes('Task.write');// Esto lo tengo asi porque me confundi creando los roles 
  const isProfesor = roles.includes('sdadsadsa');

  const { instance } = useMsal(); 
    // 2. Un hook útil para saber rápidamente si hay alguien logueado
    const isAuthenticated = useIsAuthenticated(); 

    useEffect(() => {
        let timeoutId: ReturnType<typeof setTimeout>;

        // 3. Si el usuario inicia sesión correctamente, arranca el reloj
        if (isAuthenticated) {
            const tiempoDeExpiracion = 10 * 1000; // 10 segundos de prueba

            timeoutId = setTimeout(() => {
                console.log("Simulando token expirado. Expulsando...");
                // 4. Ejecuta el cierre de sesión propio de MSAL
                instance.logoutRedirect({
                    postLogoutRedirectUri: "/" // Asegura a dónde vuelve al salir
                }); 
                // Nota: usa logoutPopup() si tu inicio de sesión fue con ventana emergente
            }, tiempoDeExpiracion);
        }

        // Limpiamos el temporizador al desmontar para evitar fugas de memoria
        return () => {
            if (timeoutId) {
                clearTimeout(timeoutId);
            }
        };
    }, [isAuthenticated, instance]);

  return (
    <MsalAuthenticationTemplate 
      interactionType={InteractionType.Redirect}
    >
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
    </MsalAuthenticationTemplate>
  )
}

export default App