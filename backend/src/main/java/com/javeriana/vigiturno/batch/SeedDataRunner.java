package com.javeriana.vigiturno.batch;

import com.javeriana.vigiturno.models.entities.Incidente;
import com.javeriana.vigiturno.models.entities.Notificacion;
import com.javeriana.vigiturno.models.entities.Reasignacion;
import com.javeriana.vigiturno.models.entities.Turno;
import com.javeriana.vigiturno.models.entities.Usuario;
import com.javeriana.vigiturno.models.entities.Zona;
import com.javeriana.vigiturno.models.enums.EstadoReasignacion;
import com.javeriana.vigiturno.models.enums.EstadoTurno;
import com.javeriana.vigiturno.models.enums.RolNombre;
import com.javeriana.vigiturno.models.enums.SeveridadIncidente;
import com.javeriana.vigiturno.models.enums.TipoIncidente;
import com.javeriana.vigiturno.models.enums.TipoNotificacion;
import com.javeriana.vigiturno.repositories.IncidenteRepository;
import com.javeriana.vigiturno.repositories.NotificacionRepository;
import com.javeriana.vigiturno.repositories.ReasignacionRepository;
import com.javeriana.vigiturno.repositories.TurnoRepository;
import com.javeriana.vigiturno.repositories.UsuarioRepository;
import com.javeriana.vigiturno.repositories.ZonaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SeedDataRunner implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ZonaRepository zonaRepository;
    private final TurnoRepository turnoRepository;
    private final IncidenteRepository incidenteRepository;
    private final NotificacionRepository notificacionRepository;
    private final ReasignacionRepository reasignacionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0 || zonaRepository.count() > 0) {
            System.out.println("Batch de carga inicial omitido: la base de datos ya contiene información.");
            return;
        }

        System.out.println("Iniciando batch de carga inicial de VigiTurno...");

        Usuario admin = Usuario.builder()
                .nombreCompleto("Laura Martínez - Administradora")
                .correo("admin@vigiturno.edu.co")
                .password(passwordEncoder.encode("admin123"))
                .rol(RolNombre.ADMINISTRADOR)
                .activo(true)
                .build();
 
        Usuario coordinador = Usuario.builder()
                .nombreCompleto("Carlos Gómez - Coordinador")
                .correo("coordinador@vigiturno.edu.co")
                .password(passwordEncoder.encode("coord123"))
                .rol(RolNombre.COORDINADOR)
                .activo(true)
                .build();
 
        Usuario docenteAna = Usuario.builder()
                .nombreCompleto("Ana Pérez - Docente")
                .correo("ana.perez@vigiturno.edu.co")
                .password(passwordEncoder.encode("docente123"))
                .rol(RolNombre.DOCENTE)
                .activo(true)
                .build();
 
        Usuario docenteLuis = Usuario.builder()
                .nombreCompleto("Luis Rodríguez - Docente")
                .correo("luis.rodriguez@vigiturno.edu.co")
                .password(passwordEncoder.encode("docente123"))
                .rol(RolNombre.DOCENTE)
                .activo(true)
                .build();
 
        Usuario docenteMarta = Usuario.builder()
                .nombreCompleto("Marta Sánchez - Docente")
                .correo("marta.sanchez@vigiturno.edu.co")
                .password(passwordEncoder.encode("docente123"))
                .rol(RolNombre.DOCENTE)
                .activo(true)
                .build();

        usuarioRepository.saveAll(List.of(admin, coordinador, docenteAna, docenteLuis, docenteMarta));

        Zona entradaPrincipal = Zona.builder()
                .nombre("Entrada Principal")
                .descripcion("Punto de acceso y salida de rutas")
                .capacidadMaxima(250)
                .activa(true)
                .build();

        Zona patioCentral = Zona.builder()
                .nombre("Patio central")
                .descripcion("Zona principal de recreo de bachillerato")
                .capacidadMaxima(180)
                .activa(true)
                .build();

        Zona cafeteria = Zona.builder()
                .nombre("Cafetería")
                .descripcion("Área de almuerzo y compra de alimentos")
                .capacidadMaxima(120)
                .activa(true)
                .build();

        Zona cancha = Zona.builder()
                .nombre("Cancha múltiple")
                .descripcion("Zona deportiva con alto movimiento")
                .capacidadMaxima(100)
                .activa(true)
                .build();

        Zona pasilloPrimaria = Zona.builder()
                .nombre("Pasillo primaria")
                .descripcion("Corredor de acceso a salones de primaria")
                .capacidadMaxima(80)
                .activa(true)
                .build();

        zonaRepository.saveAll(List.of(patioCentral, cafeteria, cancha, pasilloPrimaria, entradaPrincipal));

        LocalDate hoy = LocalDate.now();

        Turno turno1 = Turno.builder()
                .fecha(hoy)
                .horaInicio(LocalTime.of(9, 30))
                .horaFin(LocalTime.of(10, 0))
                .estado(EstadoTurno.PENDIENTE)
                .usuario(docenteAna)
                .zona(patioCentral)
                .build();

        Turno turno2 = Turno.builder()
                .fecha(hoy)
                .horaInicio(LocalTime.of(9, 30))
                .horaFin(LocalTime.of(10, 0))
                .estado(EstadoTurno.EN_CURSO)
                .usuario(docenteLuis)
                .zona(cafeteria)
                .build();

        Turno turno3 = Turno.builder()
                .fecha(hoy)
                .horaInicio(LocalTime.of(12, 30))
                .horaFin(LocalTime.of(13, 10))
                .estado(EstadoTurno.PENDIENTE)
                .usuario(docenteMarta)
                .zona(cancha)
                .build();

        Turno turno4 = Turno.builder()
                .fecha(hoy.plusDays(1))
                .horaInicio(LocalTime.of(9, 30))
                .horaFin(LocalTime.of(10, 0))
                .estado(EstadoTurno.PENDIENTE)
                .usuario(docenteAna)
                .zona(pasilloPrimaria)
                .build();

        Turno turnoCritico = Turno.builder()
                .fecha(hoy)
                .horaInicio(LocalTime.now().minusMinutes(5)) // Ya pasaron 5 min y sigue PENDIENTE
                .horaFin(LocalTime.now().plusMinutes(25))
                .estado(EstadoTurno.PENDIENTE)
                .usuario(docenteMarta)
                .zona(entradaPrincipal)
                .build();

        Turno turnoFinalizado = Turno.builder()
                .fecha(hoy.minusDays(1))
                .horaInicio(LocalTime.of(12, 0))
                .horaFin(LocalTime.of(12, 30))
                .estado(EstadoTurno.FINALIZADO)
                .calificacionLimpieza(3) // Mucha basura
                .usuario(docenteLuis)
                .zona(cafeteria)
                .build();

        Turno turnoPasilloEnCurso = Turno.builder()
                .fecha(hoy)
                .horaInicio(LocalTime.now().minusMinutes(10))
                .horaFin(LocalTime.now().plusMinutes(20))
                .estado(EstadoTurno.EN_CURSO)
                .usuario(docenteAna)
                .zona(pasilloPrimaria)
                .build();

        Turno turnoPasilloProximo = Turno.builder()
                .fecha(hoy)
                .horaInicio(LocalTime.now().plusMinutes(5))
                .horaFin(LocalTime.now().plusMinutes(35))
                .estado(EstadoTurno.PENDIENTE)
                .usuario(docenteLuis)
                .zona(pasilloPrimaria)
                .build();

        turnoRepository.saveAll(List.of(turno1, turno2, turno3, turno4, turnoCritico, turnoFinalizado, turnoPasilloEnCurso, turnoPasilloProximo));

        Incidente incidente1 = Incidente.builder()
                .tipo(TipoIncidente.CONVIVENCIA)
                .severidad(SeveridadIncidente.S2)
                .descripcion("Discusión entre estudiantes durante el recreo. Se realiza intervención preventiva.")
                .fechaHora(LocalDateTime.now().minusHours(2))
                .zona(patioCentral)
                .turno(turno1)
                .build();

        Incidente incidente2 = Incidente.builder()
                .tipo(TipoIncidente.SEGURIDAD)
                .severidad(SeveridadIncidente.S1)
                .descripcion("Caída leve cerca de la cafetería. El estudiante continúa en observación.")
                .fechaHora(LocalDateTime.now().minusHours(1))
                .zona(cafeteria)
                .turno(turno2)
                .build();

        Incidente incidente3 = Incidente.builder()
                .tipo(TipoIncidente.USO_ESPACIO)
                .severidad(SeveridadIncidente.S1)
                .descripcion("Uso inadecuado de mobiliario en zona común.")
                .fechaHora(LocalDateTime.now().minusDays(1))
                .zona(cancha)
                .turno(turno3)
                .build();

        Incidente incidenteGrave = Incidente.builder()
                .tipo(TipoIncidente.SEGURIDAD)
                .severidad(SeveridadIncidente.S3)
                .descripcion("Estudiante con herida abierta tras caída en cancha. Se traslada a enfermería.")
                .fechaHora(LocalDateTime.now())
                .zona(cancha)
                .turno(turno4)
                .build();

        incidenteRepository.saveAll(List.of(incidente1, incidente2, incidente3, incidenteGrave));

        Notificacion notificacion1 = Notificacion.builder()
                .tipo(TipoNotificacion.RECORDATORIO_TURNO)
                .mensaje("Tu turno inicia en 10 minutos en Patio central.")
                .fechaHora(LocalDateTime.now().minusMinutes(15))
                .leida(false)
                .usuario(docenteAna)
                .build();

        Notificacion notificacion2 = Notificacion.builder()
                .tipo(TipoNotificacion.ALERTA_AUSENCIA)
                .mensaje("Zona Cancha múltiple pendiente por cobertura.")
                .fechaHora(LocalDateTime.now().minusMinutes(5))
                .leida(false)
                .usuario(coordinador)
                .build();

        Notificacion alertaIncidenteS3 = Notificacion.builder()
                .tipo(TipoNotificacion.ALERTA_CRITICA)
                .mensaje("¡URGENTE! Incidente S3 en " + cancha.getNombre() + ": Estudiante requiere atención inmediata. Ver detalles en el módulo de incidentes.")
                .fechaHora(LocalDateTime.now())
                .leida(false)
                .usuario(coordinador)
                .build();

        notificacionRepository.saveAll(List.of(notificacion1, notificacion2, alertaIncidenteS3));

        Reasignacion reasignacion = Reasignacion.builder()
                .motivo("Docente original reporta reunión institucional inesperada.")
                .fechaHoraSolicitud(LocalDateTime.now().minusMinutes(20))
                .fechaHoraRespuesta(LocalDateTime.now().minusMinutes(10))
                .estado(EstadoReasignacion.ACEPTADA)
                .turno(turno3)
                .docenteOriginal(docenteMarta)
                .docenteReemplazo(docenteLuis)
                .build();

        reasignacionRepository.save(reasignacion);

        System.out.println("Batch de carga inicial finalizado correctamente.");
    }
}
