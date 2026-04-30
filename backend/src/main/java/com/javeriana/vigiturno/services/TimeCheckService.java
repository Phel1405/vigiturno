package com.javeriana.vigiturno.services;

import com.javeriana.vigiturno.models.entities.Notificacion;
import com.javeriana.vigiturno.models.entities.Turno;
import com.javeriana.vigiturno.models.enums.EstadoTurno;
import com.javeriana.vigiturno.models.enums.TipoNotificacion;
import com.javeriana.vigiturno.repositories.NotificacionRepository;
import com.javeriana.vigiturno.repositories.TurnoRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TimeCheckService {

    private final TurnoRepository turnoRepository;
    private final TurnoService turnoService;
    private final NotificacionRepository notificacionRepository;

    public TimeCheckService(TurnoRepository turnoRepository, TurnoService turnoService, NotificacionRepository notificacionRepository) {
        this.turnoRepository = turnoRepository;
        this.turnoService = turnoService;
        this.notificacionRepository = notificacionRepository;
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void actualizarEstadosYNotificaciones() {
        List<Turno> turnos = turnoRepository.findAll();
        for (Turno turno : turnos) {
            EstadoTurno estadoAnterior = turno.getEstado();
            EstadoTurno nuevoEstado = turnoService.calcularEstadoOperativo(turno);

            if (estadoAnterior != nuevoEstado) {
                turno.setEstado(nuevoEstado);
                turnoRepository.save(turno);
            }

            // Notificación por ausencia
            if (turnoService.estaSinCobertura(turno)) {
                boolean yaNotificado = notificacionRepository.findAll().stream()
                        .anyMatch(n -> n.getUsuario() != null 
                                && n.getUsuario().getId().equals(turno.getUsuario().getId())
                                && n.getTipo() == TipoNotificacion.ALERTA_AUSENCIA
                                && n.getMensaje().contains("sin cobertura")
                                && n.getMensaje().contains(turno.getZona().getNombre()));
                
                if (!yaNotificado) {
                    Notificacion n = new Notificacion();
                    n.setFechaHora(LocalDateTime.now());
                    n.setLeida(false);
                    n.setMensaje("Alerta: El turno en " + turno.getZona().getNombre() + " está sin cobertura.");
                    n.setTipo(TipoNotificacion.ALERTA_AUSENCIA);
                    n.setUsuario(turno.getUsuario());
                    notificacionRepository.save(n);
                }
            }
            // Notificación 10 minutos antes (RECORDATORIO_TURNO)
            LocalDateTime inicio = turnoService.inicioTurno(turno);
            LocalDateTime ahora = LocalDateTime.now();
            if (ahora.isAfter(inicio.minusMinutes(10)) && ahora.isBefore(inicio) && turno.getEstado() == EstadoTurno.PENDIENTE) {
                boolean yaRecordado = notificacionRepository.findAll().stream()
                        .anyMatch(n -> n.getUsuario() != null 
                                && n.getUsuario().getId().equals(turno.getUsuario().getId())
                                && n.getTipo() == TipoNotificacion.RECORDATORIO_TURNO
                                && n.getMensaje().contains("comenzará pronto")
                                && n.getMensaje().contains(turno.getZona().getNombre()));
                
                if (!yaRecordado) {
                    Notificacion n = new Notificacion();
                    n.setFechaHora(LocalDateTime.now());
                    n.setLeida(false);
                    n.setMensaje("Recordatorio: Su turno en " + turno.getZona().getNombre() + " comenzará pronto (Hora: " + turno.getHoraInicio() + "). ¡Puede hacer check-in!");
                    n.setTipo(TipoNotificacion.RECORDATORIO_TURNO);
                    n.setUsuario(turno.getUsuario());
                    notificacionRepository.save(n);
                }
            }
        }
    }
}
