# Conversión de VigiTurno a SPA con Angular

Este proyecto quedó dividido en dos partes:

1. **Backend Spring Boot**: expone servicios REST bajo `/api/...`.
2. **Frontend Angular**: está en la carpeta `frontend/` y consume esos servicios REST.

## Qué cambió

### Backend

Se agregaron:

- `src/main/java/com/javeriana/vigiturno/controllers/api/*RestController.java`
- `src/main/java/com/javeriana/vigiturno/dtos/api/ApiDtos.java`
- `src/main/java/com/javeriana/vigiturno/dtos/api/ApiMapper.java`
- `src/main/java/com/javeriana/vigiturno/config/CorsConfig.java`
- `src/main/java/com/javeriana/vigiturno/exceptions/ApiExceptionHandler.java`

Los controladores Thymeleaf originales se dejaron para no romper la primera entrega, pero la SPA nueva usa únicamente `/api`.

## Endpoints principales

- `GET /api/dashboard/admin`
- `GET /api/dashboard/mapa-calor-zonas`
- `GET /api/meta`
- CRUD completo:
  - `/api/usuarios`
  - `/api/zonas`
  - `/api/turnos`
  - `/api/incidentes`
  - `/api/reasignaciones`
  - `/api/notificaciones`
- Acciones de turno:
  - `POST /api/turnos/{id}/check-in`
  - `POST /api/turnos/{id}/cerrar`

## Cómo correrlo

### 1. Base de datos

Crea una base de datos MySQL llamada `vigiturno` y revisa estas credenciales en:

```properties
src/main/resources/application.properties
```

Por defecto están así:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/vigiturno?useSSL=false&serverTimezone=America/Bogota&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=admin
```

### 2. Backend

Desde la raíz del proyecto:

```bash
./mvnw spring-boot:run
```

En Windows:

```bash
mvnw.cmd spring-boot:run
```

Debe quedar corriendo en:

```text
http://localhost:8080
```

### 3. Frontend Angular

En otra terminal:

```bash
cd frontend
npm install
npm start
```

Debe abrirse en:

```text
http://localhost:4200
```

El archivo `frontend/proxy.conf.json` hace que Angular redirija `/api` hacia Spring Boot en `localhost:8080`.

## Cómo explicarlo en el video

Puedes decir:

> Antes el proyecto era una aplicación multipágina porque Spring Boot renderizaba cada vista con Thymeleaf. En esta versión se separó la arquitectura: Spring Boot ahora funciona como API REST y Angular se encarga de toda la navegación del usuario en una sola página. Los cambios de pantalla ya no recargan HTML desde el servidor; Angular cambia de componente con `Router` y consume datos con `HttpClient` desde `/api`.

## Pendiente para la tercera entrega

Esta conversión deja lista la arquitectura SPA + REST. Para la tercera entrega todavía falta agregar autenticación y autorización real por roles con Spring Security y proteger rutas en Angular.
