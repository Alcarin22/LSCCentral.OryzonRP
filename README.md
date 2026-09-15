# LSC Central · Management System

Aplicación web Full Stack desarrollada para la gestión interna de **Los Santos Customs Central** dentro de OryzonRP.

El proyecto centraliza tareas operativas del taller como facturación, fichajes, primas, convenios, gestión de vehículos y administración de empleados.

## Funcionalidades

- Autenticación mediante Discord OAuth2.
- Sesiones protegidas mediante JWT.
- Control de acceso basado en rangos y permisos.
- Creación de facturas individuales y por lotes.
- Historial de facturación con filtros y paginación.
- Gestión de tasaciones.
- Registro de fichajes y tiempo trabajado.
- Gestión de primas semanales.
- Gestión de convenios.
- Consulta de precios y servicios.
- Gestión de vehículos.
- Administración de empleados y rangos.
- Consulta de documentación y normativas internas.

## Stack tecnológico

### Frontend

- Angular 21
- TypeScript
- Tailwind CSS
- RxJS
- HTML5
- CSS3

### Backend

- Java 21
- Spring Boot 4
- Spring Web
- Spring Data JPA
- Spring Security
- OAuth2 Client
- JWT
- Maven

### Base de datos

- MySQL

## Arquitectura

El repositorio contiene los dos componentes principales de la aplicación:

```text
LSCCentral.OryzonRP/
├── backend/
│   └── API REST desarrollada con Spring Boot
│
└── taller-frontend/
    └── Aplicación web desarrollada con Angular
```

El frontend consume una API REST proporcionada por el backend.

La autenticación se realiza mediante Discord OAuth2. Una vez validado el usuario, el backend genera un JWT utilizado para autenticar las peticiones posteriores.

## Seguridad

La aplicación implementa distintas medidas de seguridad:

- Autenticación mediante Discord OAuth2.
- JWT con expiración.
- Validación del empleado contra la base de datos en cada petición autenticada.
- Bloqueo de empleados desactivados.
- Separación entre permisos de empleado y administración.
- Autorización de operaciones sensibles en backend.
- Identidad del empleado obtenida desde la sesión autenticada para operaciones personales.
- Credenciales y secretos gestionados mediante variables de entorno.

Los tokens, contraseñas y secretos no deben almacenarse en el repositorio.

## Configuración del backend

El backend utiliza variables de entorno para los datos sensibles y la configuración de producción.

Variables necesarias:

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD

DISCORD_BOT_TOKEN
DISCORD_GUILD_ID
DISCORD_EMPLEADO_ROLE_ID

DISCORD_CLIENT_ID
DISCORD_CLIENT_SECRET
DISCORD_REDIRECT_URI

FRONTEND_URL

JWT_SECRET
```

### Ejecutar backend en Windows

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

### Compilar backend

```powershell
cd backend
.\mvnw.cmd clean compile
```

## Configuración del frontend

Instalar dependencias:

```powershell
cd taller-frontend
npm install
```

Ejecutar en desarrollo:

```powershell
npm start
```

La aplicación estará disponible normalmente en:

```text
http://localhost:4200
```

El backend local se espera en:

```text
http://localhost:8080
```

### Compilar frontend

```powershell
npm run build
```

Angular utiliza `environment.ts` durante el desarrollo y `environment.prod.ts` para las compilaciones de producción.

## Autenticación y permisos

Los usuarios deben autenticarse mediante Discord.

El backend comprueba que el usuario posee el rol necesario en el servidor de Discord y mantiene sincronizada la información relevante del empleado.

Los permisos sensibles no dependen únicamente de la interfaz gráfica. Las operaciones protegidas vuelven a comprobar la autorización en el backend.

## Despliegue

La arquitectura permite desplegar de forma independiente:

- Frontend Angular.
- Backend Spring Boot.
- Base de datos MySQL.

Las credenciales de producción se proporcionan mediante variables de entorno y no forman parte del código fuente.

## Desarrollo

Proyecto desarrollado por **David Díaz Guerra**.

GitHub: [Alcarin22](https://github.com/Alcarin22)