package com.javeriana.vigiturno.dtos.api;

import com.javeriana.vigiturno.dtos.api.ApiDtos.*;
import com.javeriana.vigiturno.models.entities.Incidente;
import com.javeriana.vigiturno.models.entities.Notificacion;
import com.javeriana.vigiturno.models.entities.Reasignacion;
import com.javeriana.vigiturno.models.entities.Turno;
import com.javeriana.vigiturno.models.entities.Usuario;
import com.javeriana.vigiturno.models.entities.Zona;
import com.javeriana.vigiturno.services.TurnoService;

public final class ApiMapper {
    private ApiMapper() {}

    public static UsuarioDto toDto(Usuario usuario) {
        if (usuario == null) return null;
        return new UsuarioDto(
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.getActivo()
        );
    }

    public static ZonaDto toDto(Zona zona) {
        if (zona == null) return null;
        return new ZonaDto(
                zona.getId(),
                zona.getNombre(),
                zona.getDescripcion(),
                zona.getCapacidadMaxima(),
                zona.getActiva(),
                zona.getCodigoPin()
        );
    }

    public static TurnoDto toDto(Turno turno) {
        return toDto(turno, null);
    }

    public static TurnoDto toDto(Turno turno, TurnoService turnoService) {
        if (turno == null) return null;
        var usuario = turno.getUsuario();
        var zona = turno.getZona();
        return new TurnoDto(
                turno.getId(),
                turno.getFecha(),
                turno.getHoraInicio(),
                turno.getHoraFin(),
                turno.getEstado(),
                turnoService != null ? turnoService.calcularEstadoOperativo(turno) : turno.getEstado(),
                turnoService != null ? turnoService.puedeHacerCheckIn(turno) : null,
                turnoService != null ? turnoService.estaSinCobertura(turno) : null,
                turnoService != null ? turnoService.puedeReasignarse(turno) : null,
                usuario != null ? usuario.getId() : null,
                usuario != null ? usuario.getNombreCompleto() : null,
                zona != null ? zona.getId() : null,
                zona != null ? zona.getNombre() : null,
                turno.getHoraInicioReal(),
                turno.getHoraFinReal(),
                turno.getCalificacionLimpieza()
        );
    }

    public static IncidenteDto toDto(Incidente incidente) {
        if (incidente == null) return null;
        var turno = incidente.getTurno();
        var zona = incidente.getZona();
        return new IncidenteDto(
                incidente.getId(),
                incidente.getTipo(),
                incidente.getSeveridad(),
                incidente.getDescripcion(),
                incidente.getFechaHora(),
                incidente.getNombreEstudiante(),
                incidente.getCursoEstudiante(),
                turno != null ? turno.getId() : null,
                zona != null ? zona.getId() : null,
                zona != null ? zona.getNombre() : null
        );
    }

    public static NotificacionDto toDto(Notificacion notificacion) {
        if (notificacion == null) return null;
        var usuario = notificacion.getUsuario();
        return new NotificacionDto(
                notificacion.getId(),
                notificacion.getTipo(),
                notificacion.getMensaje(),
                notificacion.getFechaHora(),
                notificacion.getLeida(),
                usuario != null ? usuario.getId() : null,
                usuario != null ? usuario.getNombreCompleto() : null
        );
    }

    public static ReasignacionDto toDto(Reasignacion reasignacion) {
        if (reasignacion == null) return null;
        var turno = reasignacion.getTurno();
        var original = reasignacion.getDocenteOriginal();
        var reemplazo = reasignacion.getDocenteReemplazo();
        return new ReasignacionDto(
                reasignacion.getId(),
                reasignacion.getMotivo(),
                reasignacion.getFechaHoraSolicitud(),
                reasignacion.getFechaHoraRespuesta(),
                reasignacion.getEstado(),
                turno != null ? turno.getId() : null,
                original != null ? original.getId() : null,
                original != null ? original.getNombreCompleto() : null,
                reemplazo != null ? reemplazo.getId() : null,
                reemplazo != null ? reemplazo.getNombreCompleto() : null
        );
    }
}
