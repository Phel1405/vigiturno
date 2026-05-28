package com.javeriana.vigiturno.dtos.api;

import com.javeriana.vigiturno.models.enums.EstadoReasignacion;
import com.javeriana.vigiturno.models.enums.EstadoTurno;
import com.javeriana.vigiturno.models.enums.RolNombre;
import com.javeriana.vigiturno.models.enums.SeveridadIncidente;
import com.javeriana.vigiturno.models.enums.TipoIncidente;
import com.javeriana.vigiturno.models.enums.TipoNotificacion;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class ApiDtos {
    private ApiDtos() {}

    public record LoginRequest(String correo, String password) {}
    public record LoginResponse(String token, String correo, String nombreCompleto, RolNombre rol, Long id) {}
    public record GoogleLoginRequest(String idToken) {}

    public record UsuarioDto(Long id, String nombreCompleto, String correo, RolNombre rol, Boolean activo) {}
    public record UsuarioRequest(String nombreCompleto, String correo, String password, RolNombre rol, Boolean activo) {}

    public record ZonaDto(Long id, String nombre, String descripcion, Integer capacidadMaxima, Boolean activa, String codigoPin) {}
    public record ZonaRequest(String nombre, String descripcion, Integer capacidadMaxima, Boolean activa, String codigoPin) {}

    public record TurnoDto(
            Long id,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            EstadoTurno estado,
            EstadoTurno estadoOperativo,
            Boolean puedeCheckIn,
            Boolean sinCobertura,
            Boolean puedeReasignar,
            Long usuarioId,
            String usuarioNombre,
            Long zonaId,
            String zonaNombre,
            LocalTime horaInicioReal,
            LocalTime horaFinReal,
            Integer calificacionLimpieza
    ) {}
    public record TurnoRequest(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, EstadoTurno estado, Long usuarioId, Long zonaId, LocalTime horaInicioReal, LocalTime horaFinReal, Integer calificacionLimpieza) {}
    public record FinalizarTurnoRequest(Integer calificacionLimpieza) {}

    public record IncidenteDto(
            Long id,
            TipoIncidente tipo,
            SeveridadIncidente severidad,
            String descripcion,
            LocalDateTime fechaHora,
            String nombreEstudiante,
            String cursoEstudiante,
            Long turnoId,
            Long zonaId,
            String zonaNombre
    ) {}
    public record IncidenteRequest(
            TipoIncidente tipo,
            SeveridadIncidente severidad,
            String descripcion,
            LocalDateTime fechaHora,
            String nombreEstudiante,
            String cursoEstudiante,
            Long turnoId,
            Long zonaId
    ) {}

    public record NotificacionDto(
            Long id,
            TipoNotificacion tipo,
            String mensaje,
            LocalDateTime fechaHora,
            Boolean leida,
            Long usuarioId,
            String usuarioNombre
    ) {}
    public record NotificacionRequest(TipoNotificacion tipo, String mensaje, LocalDateTime fechaHora, Boolean leida, Long usuarioId) {}

    public record ReasignacionDto(
            Long id,
            String motivo,
            LocalDateTime fechaHoraSolicitud,
            LocalDateTime fechaHoraRespuesta,
            EstadoReasignacion estado,
            Long turnoId,
            Long docenteOriginalId,
            String docenteOriginalNombre,
            Long docenteReemplazoId,
            String docenteReemplazoNombre
    ) {}
    public record ReasignacionRequest(
            String motivo,
            LocalDateTime fechaHoraSolicitud,
            LocalDateTime fechaHoraRespuesta,
            EstadoReasignacion estado,
            Long turnoId,
            Long docenteOriginalId,
            Long docenteReemplazoId
    ) {}

    public record DashboardDto(
            long totalUsuarios,
            long totalZonas,
            long totalTurnos,
            long totalIncidentes,
            long totalReasignaciones,
            long totalNotificaciones
    ) {}

    public record DocenteDashboardDto(
            UsuarioDto docente,
            long totalTurnos,
            long totalIncidentes,
            long totalNotificaciones,
            TurnoDto proximoTurno,
            List<TurnoDto> turnosRecientes,
            List<IncidenteDto> incidentesRecientes,
            List<NotificacionDto> notificacionesRecientes
    ) {}

    public record HeatmapZonaDto(Long zonaId, String zonaNombre, long incidentes, double porcentaje) {}

    public record MetaDto(
            RolNombre[] roles,
            EstadoTurno[] estadosTurno,
            TipoIncidente[] tiposIncidente,
            SeveridadIncidente[] severidadesIncidente,
            TipoNotificacion[] tiposNotificacion,
            EstadoReasignacion[] estadosReasignacion
    ) {}
}
