# Plataforma LC - Libro de Clases Digital

Este proyecto es una plataforma digital para el Libro de Clases, compuesta por múltiples microservicios (backend) y una aplicación frontend.

## Arquitectura Actual y Despliegue (AWS & Azure)

La plataforma actualmente se encuentra alojada en **AWS** (Amazon Web Services) y utiliza **Azure Entra ID** para la gestión de identidad, distribuyéndose de la siguiente manera:

- **Instancias EC2:**
  - Se dispone de una instancia EC2 dedicada exclusivamente para el **Backend**, donde corren todos los microservicios.
  - Se dispone de otra instancia EC2 dedicada para el **Frontend**.
  
- **API Gateway (AWS):**
  - La administración principal de las APIs y el enrutamiento público hacia el backend está gestionado por el **API Gateway de AWS**.

- **Autenticación y Autorización (Azure Entra ID):**
  - La autenticación de usuarios, generación de tokens (JWT) y autorización de acceso está completamente delegada y gestionada por **Entra ID de Azure** (anteriormente Azure AD), reemplazando cualquier solución de identidad local previa (como Keycloak).

- **Backend (Docker):**
  - Todo el entorno de backend (bases de datos, microservicios, etc.) se ejecuta mediante contenedores de **Docker**.

- **Frontend (Cloudflare):**
  - El frontend se levanta en su instancia EC2 y se expone al exterior mediante un enlace (túnel) de **Cloudflare**, el cual es generado por la propia instancia del front.

### El rol del módulo "API Gateway" interno (BFF)

Dentro de la estructura del proyecto (específicamente en `/sistema_Infra`), existe un módulo llamado **`api gateway`**. 
Es importante aclarar que **su función principal es actuar como un BFF (Backend For Frontend)**. No expone los servicios directamente a internet, sino que sirve para orquestar y adaptar las llamadas entre el frontend y los microservicios internos. El API Gateway "real" que administra y protege el tráfico de entrada desde el exterior es el API Gateway de AWS.

---

## Ejecución en entorno local (Desarrollo)

Si deseas levantar el entorno de manera local para desarrollo, considera los siguientes requisitos:

### Requisitos previos
- Java 17
- Maven
- Node.js 20+
- MySQL (o usar el docker-compose incluido)
- Docker Desktop

### 1. Levantar el Backend (Docker)
Todo el backend está dockerizado, por lo que puedes levantarlo usando Docker Compose:
```bash
docker-compose up -d
```
Asegúrate de configurar correctamente las variables de entorno para que se conecten al tenant de **Azure Entra ID** correspondiente para el entorno de desarrollo.

### 2. Frontend
```bash
cd frontend/plataforma_lc_frontend
npm install
npm run dev
```
Acceder a `http://localhost:5173`

> **Nota sobre bases de datos**: El backend utiliza bases de datos separadas por microservicio (estudiante, curso, asistencia, evaluaciones). La creación de estos esquemas se gestiona a través de los scripts de inicialización en el entorno Docker.
