export type RolNombre = 'DOCENTE' | 'COORDINADOR' | 'ADMINISTRADOR';
export type EstadoTurno = 'PENDIENTE' | 'EN_CURSO' | 'FINALIZADO' | 'REASIGNADO' | 'CANCELADO';
export type TipoIncidente = 'SEGURIDAD' | 'CONVIVENCIA' | 'USO_ESPACIO' | 'OBSERVACION_SOCIAL';
export type SeveridadIncidente = 'S1' | 'S2' | 'S3';
export type TipoNotificacion = 'RECORDATORIO_TURNO' | 'ALERTA_AUSENCIA' | 'INCIDENTE_REPORTADO' | 'REASIGNACION_PROPUESTA' | 'REASIGNACION_RESPUESTA';
export type EstadoReasignacion = 'PENDIENTE' | 'ACEPTADA' | 'RECHAZADA' | 'EXPIRADA';

export interface Usuario {
  id?: number;
  nombreCompleto: string;
  correo: string;
  password?: string;
  rol: RolNombre;
  activo: boolean;
}

export interface Zona {
  id?: number;
  nombre: string;
  descripcion?: string;
  capacidadMaxima?: number;
  activa: boolean;
}

export interface Turno {
  id?: number;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  estado: EstadoTurno;
  estadoOperativo?: EstadoTurno;
  puedeCheckIn?: boolean;
  sinCobertura?: boolean;
  puedeReasignar?: boolean;
  usuarioId: number;
  usuarioNombre?: string;
  zonaId: number;
  zonaNombre?: string;
}

export interface Incidente {
  id?: number;
  tipo: TipoIncidente;
  severidad: SeveridadIncidente;
  descripcion: string;
  fechaHora: string;
  nombreEstudiante?: string;
  cursoEstudiante?: string;
  turnoId?: number | null;
  zonaId?: number | null;
  zonaNombre?: string;
}

export interface Notificacion {
  id?: number;
  tipo: TipoNotificacion;
  mensaje: string;
  fechaHora: string;
  leida: boolean;
  usuarioId: number;
  usuarioNombre?: string;
}

export interface Reasignacion {
  id?: number;
  motivo: string;
  fechaHoraSolicitud: string;
  fechaHoraRespuesta?: string | null;
  estado: EstadoReasignacion;
  turnoId: number;
  docenteOriginalId: number;
  docenteOriginalNombre?: string;
  docenteReemplazoId?: number | null;
  docenteReemplazoNombre?: string;
}

export interface Dashboard {
  totalUsuarios: number;
  totalZonas: number;
  totalTurnos: number;
  totalIncidentes: number;
  totalReasignaciones: number;
  totalNotificaciones: number;
}

export interface HeatmapZona {
  zonaId: number;
  zonaNombre: string;
  incidentes: number;
  porcentaje: number;
}

export interface Meta {
  roles: RolNombre[];
  estadosTurno: EstadoTurno[];
  tiposIncidente: TipoIncidente[];
  severidadesIncidente: SeveridadIncidente[];
  tiposNotificacion: TipoNotificacion[];
  estadosReasignacion: EstadoReasignacion[];
}
