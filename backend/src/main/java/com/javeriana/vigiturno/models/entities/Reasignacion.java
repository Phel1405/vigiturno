package com.javeriana.vigiturno.models.entities;

import com.javeriana.vigiturno.models.enums.EstadoReasignacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reasignaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reasignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String motivo;

    @Column(name = "fecha_hora_solicitud", nullable = false)
    private LocalDateTime fechaHoraSolicitud;

    @Column(name = "fecha_hora_respuesta")
    private LocalDateTime fechaHoraRespuesta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReasignacion estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id", nullable = false)
    private Turno turno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_original_id", nullable = false)
    private Usuario docenteOriginal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_reemplazo_id")
    private Usuario docenteReemplazo;
}