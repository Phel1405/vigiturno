# Cambios de lógica de tiempo para VigiTurno

Esta versión corrige el problema principal: los turnos y reasignaciones ya no se muestran ni se accionan solo porque existen en la base de datos, sino según la fecha y hora actual.

## Archivos modificados

### Backend Spring Boot

- `TurnoRepository.java`
- `TurnoService.java`
- `ReasignacionService.java`
- `TurnoRestController.java`
- `ReasignacionRestController.java`
- `DashboardRestController.java`
- `ApiDtos.java`
- `ApiMapper.java`

### Frontend Angular

- `frontend/src/app/core/models.ts`
- `frontend/src/app/core/api.service.ts`
- `frontend/src/app/pages/turnos/turnos.component.ts`
- `frontend/src/app/pages/reasignaciones/reasignaciones.component.ts`

## Reglas implementadas

### Turnos

El backend calcula un estado operativo con base en `LocalDateTime.now()`:

- Si el turno todavía no empieza: `PENDIENTE`.
- Si el turno está dentro de la ventana horaria y tiene check-in: `EN_CURSO`.
- Si ya terminó: `FINALIZADO`.
- Si fue cancelado o reasignado, conserva ese estado.

### Check-in

El check-in solo se permite desde 10 minutos antes del inicio del turno y hasta antes de la hora de fin.

### Ausencia de cobertura

Un turno queda como `sinCobertura = true` cuando:

- Ya pasaron más de 2 minutos desde la hora de inicio.
- El turno todavía no terminó.
- El estado sigue en `PENDIENTE`.

### Reasignaciones

Solo se puede reasignar un turno si:

- El turno no ha terminado.
- El turno no está cancelado.
- El docente original coincide con el docente asignado al turno.
- El reemplazo no es el mismo docente original.

Cuando una reasignación queda `ACEPTADA`, el sistema cambia el docente del turno por el reemplazo y marca el turno como `REASIGNADO`.

### Docentes disponibles

El endpoint `/api/turnos/{id}/docentes-disponibles` propone docentes activos con rol `DOCENTE` que no tengan otro turno cruzado en la misma fecha y horario.

## Endpoints nuevos

```text
GET /api/turnos/hoy
GET /api/turnos/proximos?dias=7
GET /api/turnos/activos
GET /api/turnos/sin-cobertura
GET /api/turnos/reasignables
GET /api/turnos/{id}/docentes-disponibles
GET /api/reasignaciones/pendientes-vigentes
POST /api/reasignaciones/{id}/aceptar
POST /api/reasignaciones/{id}/rechazar
```

## Qué decir en el video

> Antes, el sistema mostraba turnos y reasignaciones solamente porque estaban guardados en la base de datos. Ahora agregamos una capa de lógica de negocio en los servicios de Spring Boot. Los turnos se evalúan contra la fecha y hora actual, el check-in solo se permite en una ventana válida, las alertas de ausencia se calculan después del umbral de dos minutos y las reasignaciones solo se permiten para turnos vigentes. Además, el frontend Angular consume endpoints más específicos como turnos de hoy, turnos reasignables y reasignaciones pendientes vigentes.
