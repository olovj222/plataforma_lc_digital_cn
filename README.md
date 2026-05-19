# Plataforma LC - Libro de Clases Digital
## Ejecución en entorno local

## Requisitos previos
- Java 17
- Maven
- Node.js 20+
- MySQL
- Docker Desktop
## Ejecutar git clone en rama Develop
## Bases de datos
Crear las siguientes bases de datos en MySQL antes de levantar los microservicios:
```sql
CREATE DATABASE estudiante;
CREATE DATABASE curso;
CREATE DATABASE asistencia;
CREATE DATABASE evaluaciones;
```

## Orden de arranque

### 1. Keycloak
```bash
docker run -p 8090:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:26.2.4 start-dev
```
Acceder a `http://localhost:8090` y configurar:
- Crear realm: `plataforma_lc`
- Crear roles: `ADMIN`, `PROFESOR`
- Crear client: `plataforma_lc_frontend` con redirect URI `http://localhost:5173/*`
- Crear usuarios y asignar roles

### 2. Eureka Server
Correr el módulo `eurekaServer` desde Netbeans.
Verificar en `http://localhost:8761`

### 3. Microservicios de negocio
Correr en cualquier orden desde Netbeans:
- `estudiante` → puerto 8080
- `curso` → puerto 8081
- `asistencia` → puerto 8082
- `evaluaciones` → puerto 8083

### 4. Spring Boot Admin
Correr el módulo `adminSpringBoot` desde Netbeans.
Verificar en `http://localhost:8062`

### 5. API Gateway
Correr el módulo `apiGateway` desde Netbeans.
Verificar en `http://localhost:8085`

### 6. Frontend
```bash
cd frontend/plataforma_lc_frontend
npm install
npm run dev
```
Acceder a `http://localhost:5173`

## Usuarios por defecto
| Usuario | Contraseña | Rol |
|---------|------------|-----|
| admin1 | admin123 | ADMIN |
| profesor1 | profesor123 | PROFESOR |

