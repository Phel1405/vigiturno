package com.javeriana.vigiturno.services;

import com.javeriana.vigiturno.exceptions.BusinessException;
import com.javeriana.vigiturno.models.entities.Turno;
import com.javeriana.vigiturno.models.entities.Usuario;
import com.javeriana.vigiturno.models.enums.EstadoTurno;
import com.javeriana.vigiturno.models.enums.RolNombre;
import com.javeriana.vigiturno.repositories.TurnoRepository;
import com.javeriana.vigiturno.repositories.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TurnoService {

    private static final int MINUTOS_ANTES_CHECK_IN = 10;
    private static final int MINUTOS_ALERTA_AUSENCIA = 2;

    private final TurnoRepository turnoRepository;
    private final UsuarioRepository usuarioRepository;

    public TurnoService(TurnoRepository turnoRepository, UsuarioRepository usuarioRepository) {
        this.turnoRepository = turnoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Turno> listarTodos() {
        return turnoRepository.findAll();
    }

    public List<Turno> listarPorUsuarioId(Long usuarioId) {
        return turnoRepository.findByUsuarioIdOrderByFechaAscHoraInicioAsc(usuarioId);
    }

    public List<Turno> listarDeHoy() {
        return turnoRepository.findByFechaOrderByHoraInicioAsc(LocalDate.now());
    }

    public List<Turno> listarProximos(int dias) {
        LocalDate hoy = LocalDate.now();
        return turnoRepository.findByFechaBetweenOrderByFechaAscHoraInicioAsc(hoy, hoy.plusDays(Math.max(dias, 1)));
    }

    public List<Turno> listarActivosAhora() {
        return listarDeHoy().stream()
                .filter(this::estaEnVentanaDelTurno)
                .toList();
    }

    public List<Turno> listarSinCobertura() {
        return listarDeHoy().stream()
                .filter(this::estaSinCobertura)
                .toList();
    }

    public List<Turno> listarReasignables() {
        return listarProximos(7).stream()
                .filter(this::puedeReasignarse)
                .toList();
    }

    public Optional<Turno> buscarPorId(Long id) {
        return turnoRepository.findById(id);
    }

    public Turno guardar(Turno turno) {
        validarTurno(turno);
        if (turno.getEstado() == null) {
            turno.setEstado(EstadoTurno.PENDIENTE);
        }
        return turnoRepository.save(turno);
    }

    @Transactional
    public Turno registrarCheckIn(Turno turno, String codigoPin) {
        if (!puedeHacerCheckIn(turno)) {
            throw new BusinessException("El check-in solo está disponible desde " + MINUTOS_ANTES_CHECK_IN
                    + " minutos antes del inicio y hasta antes de finalizar el turno.");
        }
        if (turno.getEstado() == EstadoTurno.CANCELADO || turno.getEstado() == EstadoTurno.FINALIZADO) {
            throw new BusinessException("No se puede registrar check-in en un turno cerrado o cancelado.");
        }
        if (turno.getZona().getCodigoPin() != null && !turno.getZona().getCodigoPin().equals(codigoPin)) {
            throw new BusinessException("El código PIN/QR no es válido para esta zona.");
        }
        turno.setEstado(EstadoTurno.EN_CURSO);
        turno.setHoraInicioReal(LocalTime.now());
        return turnoRepository.save(turno);
    }

    @Transactional
    public Turno cerrarTurno(Turno turno, Integer calificacionLimpieza) {
        if (turno.getEstado() != EstadoTurno.EN_CURSO) {
            throw new BusinessException("Solo se pueden cerrar turnos que estén EN CURSO.");
        }
        if (calificacionLimpieza == null || calificacionLimpieza < 1 || calificacionLimpieza > 4) {
            throw new BusinessException("Es obligatorio registrar una calificación de limpieza válida (1 a 4).");
        }
        turno.setEstado(EstadoTurno.FINALIZADO);
        turno.setHoraFinReal(LocalTime.now());
        turno.setCalificacionLimpieza(calificacionLimpieza);
        return turnoRepository.save(turno);
    }

    public void eliminarPorId(Long id) {
        turnoRepository.deleteById(id);
    }

    public LocalDateTime inicioTurno(Turno turno) {
        return LocalDateTime.of(turno.getFecha(), turno.getHoraInicio());
    }

    public LocalDateTime finTurno(Turno turno) {
        return LocalDateTime.of(turno.getFecha(), turno.getHoraFin());
    }

    public EstadoTurno calcularEstadoOperativo(Turno turno) {
        if (turno.getEstado() == EstadoTurno.CANCELADO || turno.getEstado() == EstadoTurno.REASIGNADO) {
            return turno.getEstado();
        }

        LocalDateTime ahora = LocalDateTime.now();
        if (ahora.isBefore(inicioTurno(turno))) {
            return EstadoTurno.PENDIENTE;
        }
        if (ahora.isAfter(finTurno(turno))) {
            return EstadoTurno.FINALIZADO;
        }
        return turno.getEstado() == EstadoTurno.EN_CURSO ? EstadoTurno.EN_CURSO : EstadoTurno.PENDIENTE;
    }

    public boolean estaEnVentanaDelTurno(Turno turno) {
        LocalDateTime ahora = LocalDateTime.now();
        return !ahora.isBefore(inicioTurno(turno)) && ahora.isBefore(finTurno(turno));
    }

    public boolean puedeHacerCheckIn(Turno turno) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioPermitido = inicioTurno(turno).minusMinutes(MINUTOS_ANTES_CHECK_IN);
        LocalDateTime finPermitido = finTurno(turno);

        return !ahora.isBefore(inicioPermitido)
                && ahora.isBefore(finPermitido)
                && turno.getEstado() != EstadoTurno.FINALIZADO
                && turno.getEstado() != EstadoTurno.CANCELADO;
    }

    public boolean estaSinCobertura(Turno turno) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = inicioTurno(turno).plusMinutes(MINUTOS_ALERTA_AUSENCIA);
        return ahora.isAfter(limite)
                && ahora.isBefore(finTurno(turno))
                && turno.getEstado() == EstadoTurno.PENDIENTE;
    }

    public boolean puedeReasignarse(Turno turno) {
        LocalDateTime ahora = LocalDateTime.now();
        return ahora.isBefore(finTurno(turno))
                && turno.getEstado() != EstadoTurno.FINALIZADO
                && turno.getEstado() != EstadoTurno.CANCELADO;
    }

    public List<Usuario> proponerDocentesDisponibles(Turno turno) {
        if (!puedeReasignarse(turno)) {
            throw new BusinessException("No se puede proponer reemplazo para un turno que ya finalizó o fue cancelado.");
        }

        return usuarioRepository.findAll().stream()
                .filter(usuario -> usuario.getActivo() == null || usuario.getActivo())
                .filter(usuario -> usuario.getRol() == RolNombre.DOCENTE)
                .filter(usuario -> turno.getUsuario() == null || !usuario.getId().equals(turno.getUsuario().getId()))
                .filter(usuario -> estaDisponible(usuario, turno))
                .toList();
    }

    public boolean estaDisponible(Usuario usuario, Turno turno) {
        return turnoRepository.findByUsuarioIdAndFechaAndHoraInicioLessThanAndHoraFinGreaterThan(
                usuario.getId(),
                turno.getFecha(),
                turno.getHoraFin(),
                turno.getHoraInicio()
        ).isEmpty();
    }

    private void validarTurno(Turno turno) {
        if (turno.getFecha() == null || turno.getHoraInicio() == null || turno.getHoraFin() == null) {
            throw new BusinessException("El turno debe tener fecha, hora de inicio y hora de fin.");
        }
        if (!turno.getHoraFin().isAfter(turno.getHoraInicio())) {
            throw new BusinessException("La hora de fin debe ser posterior a la hora de inicio.");
        }
    }
}
