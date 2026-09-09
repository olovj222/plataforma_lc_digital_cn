-- Se ejecuta automáticamente al crear el contenedor de MySQL por primera vez
-- Crea una base de datos por cada microservicio del proyecto

CREATE DATABASE IF NOT EXISTS estudiante;
CREATE DATABASE IF NOT EXISTS curso;
CREATE DATABASE IF NOT EXISTS asistencia;
CREATE DATABASE IF NOT EXISTS evaluaciones;
CREATE DATABASE IF NOT EXISTS justificativos;
CREATE DATABASE IF NOT EXISTS anotaciones;
