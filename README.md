#Plataforma_lc_digital

## Ejecución en entorno Docker
## Requisitos previos
- Docker Desktop instalado y corriendo
- Maven Instalado o Java extension pack instalado en VSCode
## Pasos
## Ejecutar git clone en rama feature/Docker

### 1. Compilar el backend
Desde Netbeans, hacer **Clean and Build** del proyecto completo `plataforma_lc` para generar los JARs en cada carpeta `target/`.
O en su defecto ejecutar
```bash
mvn clean package -DskipTests
```

### 2. Compilar el frontend
```bash
cd frontend/plataforma_lc_frontend
npm install
npm run build
```

### 3. Levantar todo con Docker Compose
Desde la raíz del proyecto `plataforma_lc_digital`:
```bash
docker-compose up --build
```

## Servicios disponibles
| Servicio | URL |
|---------|-----|
| Frontend | http://localhost:5173 |
| API Gateway | http://localhost:8085 |
| Eureka Server | http://localhost:8761 |
| Spring Boot Admin | http://localhost:8062 |
| Keycloak | http://localhost:8090 |

## Configuración inicial de Keycloak
La primera vez que levantes el proyecto debes configurar Keycloak manualmente:

1. Acceder a `http://localhost:8090` con usuario `admin` y contraseña `admin`
2. Crear realm: `plataforma_lc`
3. Crear roles: `ADMIN`, `PROFESOR`
4. Crear client: `plataforma_lc_frontend`
   - Valid redirect URIs: `http://localhost:5173/*`
   - Web origins: `http://localhost:5173`
5. Crear usuarios y asignar roles

## Usuarios por defecto
| Usuario | Contraseña | Rol |
|---------|------------|-----|
| admin1 | admin123 | ADMIN |
| profesor1 | profesor123 | PROFESOR |

## Detener el proyecto
```bash
docker-compose down
```

Para eliminar también los volúmenes de las bases de datos:
```bash
docker-compose down -v
```
