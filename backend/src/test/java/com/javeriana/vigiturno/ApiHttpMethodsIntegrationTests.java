package com.javeriana.vigiturno;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javeriana.vigiturno.dtos.api.ApiDtos.IncidenteRequest;
import com.javeriana.vigiturno.dtos.api.ApiDtos.ReasignacionRequest;
import com.javeriana.vigiturno.models.entities.Incidente;
import com.javeriana.vigiturno.models.entities.Turno;
import com.javeriana.vigiturno.models.entities.Usuario;
import com.javeriana.vigiturno.models.entities.Zona;
import com.javeriana.vigiturno.models.enums.EstadoReasignacion;
import com.javeriana.vigiturno.models.enums.EstadoTurno;
import com.javeriana.vigiturno.models.enums.RolNombre;
import com.javeriana.vigiturno.models.enums.SeveridadIncidente;
import com.javeriana.vigiturno.models.enums.TipoIncidente;
import com.javeriana.vigiturno.repositories.IncidenteRepository;
import com.javeriana.vigiturno.repositories.NotificacionRepository;
import com.javeriana.vigiturno.repositories.ReasignacionRepository;
import com.javeriana.vigiturno.repositories.TurnoRepository;
import com.javeriana.vigiturno.repositories.UsuarioRepository;
import com.javeriana.vigiturno.repositories.ZonaRepository;
import com.javeriana.vigiturno.security.JwtTokenProvider;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiHttpMethodsIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ZonaRepository zonaRepository;

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private IncidenteRepository incidenteRepository;

    @Autowired
    private ReasignacionRepository reasignacionRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    private Usuario admin;
    private Usuario docenteOriginal;
    private Usuario docenteDisponible;
    private Usuario docenteOcupado;
    private Zona zonaPatio;
    private Zona zonaCafeteria;
    private Turno turnoReasignable;
    private String adminToken;

    @BeforeEach
    void setUp() {
        limpiarDatos();

        admin = guardarUsuario("Admin Test", "admin.test@vigiturno.edu.co", RolNombre.ADMINISTRADOR);
        docenteOriginal = guardarUsuario("Docente Original", "original.test@vigiturno.edu.co", RolNombre.DOCENTE);
        docenteDisponible = guardarUsuario("Docente Disponible", "disponible.test@vigiturno.edu.co", RolNombre.DOCENTE);
        docenteOcupado = guardarUsuario("Docente Ocupado", "ocupado.test@vigiturno.edu.co", RolNombre.DOCENTE);

        zonaPatio = zonaRepository.save(Zona.builder()
                .nombre("Patio pruebas")
                .descripcion("Zona para pruebas de integración")
                .capacidadMaxima(50)
                .activa(true)
                .build());

        zonaCafeteria = zonaRepository.save(Zona.builder()
                .nombre("Cafeteria pruebas")
                .descripcion("Zona secundaria para pruebas")
                .capacidadMaxima(30)
                .activa(true)
                .build());

        LocalDate fecha = LocalDate.now().plusDays(1);
        turnoReasignable = turnoRepository.save(Turno.builder()
                .fecha(fecha)
                .horaInicio(LocalTime.of(9, 0))
                .horaFin(LocalTime.of(10, 0))
                .estado(EstadoTurno.PENDIENTE)
                .usuario(docenteOriginal)
                .zona(zonaPatio)
                .build());

        turnoRepository.save(Turno.builder()
                .fecha(fecha)
                .horaInicio(LocalTime.of(9, 30))
                .horaFin(LocalTime.of(10, 30))
                .estado(EstadoTurno.PENDIENTE)
                .usuario(docenteOcupado)
                .zona(zonaCafeteria)
                .build());

        adminToken = "Bearer " + jwtTokenProvider.generateToken(admin);
    }

    @Test
    void getDocentesDisponiblesFiltraDocenteOriginalYOcupados() throws Exception {
        mockMvc.perform(get("/api/turnos/{id}/docentes-disponibles", turnoReasignable.getId())
                        .header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.correo == 'disponible.test@vigiturno.edu.co')]").exists())
                .andExpect(jsonPath("$[?(@.correo == 'original.test@vigiturno.edu.co')]").doesNotExist())
                .andExpect(jsonPath("$[?(@.correo == 'ocupado.test@vigiturno.edu.co')]").doesNotExist());
    }

    @Test
    void postReasignacionAceptadaCambiaDocenteYEstadoDelTurno() throws Exception {
        ReasignacionRequest request = new ReasignacionRequest(
                "Prueba de reasignacion aceptada",
                LocalDateTime.now(),
                null,
                EstadoReasignacion.ACEPTADA,
                turnoReasignable.getId(),
                docenteOriginal.getId(),
                docenteDisponible.getId()
        );

        mockMvc.perform(post("/api/reasignaciones")
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACEPTADA"))
                .andExpect(jsonPath("$.docenteOriginalId").value(docenteOriginal.getId()))
                .andExpect(jsonPath("$.docenteReemplazoId").value(docenteDisponible.getId()));

        Turno turnoActualizado = turnoRepository.findById(turnoReasignable.getId()).orElseThrow();
        assertThat(turnoActualizado.getUsuario().getId()).isEqualTo(docenteDisponible.getId());
        assertThat(turnoActualizado.getEstado()).isEqualTo(EstadoTurno.REASIGNADO);
    }

    @Test
    void putIncidenteAsociadoATurnoActivoTomaLaZonaDelTurno() throws Exception {
        Turno turnoActivo = turnoRepository.save(Turno.builder()
                .fecha(LocalDate.now())
                .horaInicio(LocalTime.now().minusMinutes(5))
                .horaFin(LocalTime.now().plusMinutes(25))
                .estado(EstadoTurno.EN_CURSO)
                .usuario(docenteOriginal)
                .zona(zonaPatio)
                .build());

        Incidente incidente = incidenteRepository.save(Incidente.builder()
                .tipo(TipoIncidente.CONVIVENCIA)
                .severidad(SeveridadIncidente.S1)
                .descripcion("Incidente inicial")
                .fechaHora(LocalDateTime.now())
                .nombreEstudiante("Estudiante Uno")
                .cursoEstudiante("6A")
                .zona(zonaCafeteria)
                .build());

        IncidenteRequest request = new IncidenteRequest(
                TipoIncidente.SEGURIDAD,
                SeveridadIncidente.S2,
                "Incidente actualizado con turno activo",
                LocalDateTime.now(),
                "Estudiante Dos",
                "7B",
                turnoActivo.getId(),
                zonaCafeteria.getId()
        );

        mockMvc.perform(put("/api/incidentes/{id}", incidente.getId())
                        .header(HttpHeaders.AUTHORIZATION, adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("SEGURIDAD"))
                .andExpect(jsonPath("$.severidad").value("S2"))
                .andExpect(jsonPath("$.turnoId").value(turnoActivo.getId()))
                .andExpect(jsonPath("$.zonaId").value(zonaPatio.getId()));
    }

    @Test
    void deleteTurnoEliminaElRecursoYLuegoRetorna404() throws Exception {
        Turno turnoParaEliminar = turnoRepository.save(Turno.builder()
                .fecha(LocalDate.now().plusDays(2))
                .horaInicio(LocalTime.of(11, 0))
                .horaFin(LocalTime.of(12, 0))
                .estado(EstadoTurno.PENDIENTE)
                .usuario(docenteOriginal)
                .zona(zonaPatio)
                .build());

        mockMvc.perform(delete("/api/turnos/{id}", turnoParaEliminar.getId())
                        .header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isNoContent());

        assertThat(turnoRepository.existsById(turnoParaEliminar.getId())).isFalse();

        mockMvc.perform(get("/api/turnos/{id}", turnoParaEliminar.getId())
                        .header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isNotFound());
    }

    private Usuario guardarUsuario(String nombre, String correo, RolNombre rol) {
        return usuarioRepository.save(Usuario.builder()
                .nombreCompleto(nombre)
                .correo(correo)
                .password(passwordEncoder.encode("123456"))
                .rol(rol)
                .activo(true)
                .build());
    }

    private void limpiarDatos() {
        reasignacionRepository.deleteAll();
        incidenteRepository.deleteAll();
        notificacionRepository.deleteAll();
        turnoRepository.deleteAll();
        zonaRepository.deleteAll();
        usuarioRepository.deleteAll();
    }
}
