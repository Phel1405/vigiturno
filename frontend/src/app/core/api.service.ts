import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Dashboard, HeatmapZona, Incidente, Meta, Notificacion, Reasignacion, Turno, Usuario, Zona } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly api = '/api';

  constructor(private readonly http: HttpClient) {}

  meta(): Observable<Meta> { return this.http.get<Meta>(`${this.api}/meta`); }
  dashboard(): Observable<Dashboard> { return this.http.get<Dashboard>(`${this.api}/dashboard/admin`); }
  mapaCalor(): Observable<HeatmapZona[]> { return this.http.get<HeatmapZona[]>(`${this.api}/dashboard/mapa-calor-zonas`); }

  usuarios(): Observable<Usuario[]> { return this.http.get<Usuario[]>(`${this.api}/usuarios`); }
  crearUsuario(data: Usuario): Observable<Usuario> { return this.http.post<Usuario>(`${this.api}/usuarios`, data); }
  actualizarUsuario(id: number, data: Usuario): Observable<Usuario> { return this.http.put<Usuario>(`${this.api}/usuarios/${id}`, data); }
  eliminarUsuario(id: number): Observable<void> { return this.http.delete<void>(`${this.api}/usuarios/${id}`); }

  zonas(): Observable<Zona[]> { return this.http.get<Zona[]>(`${this.api}/zonas`); }
  crearZona(data: Zona): Observable<Zona> { return this.http.post<Zona>(`${this.api}/zonas`, data); }
  actualizarZona(id: number, data: Zona): Observable<Zona> { return this.http.put<Zona>(`${this.api}/zonas/${id}`, data); }
  eliminarZona(id: number): Observable<void> { return this.http.delete<void>(`${this.api}/zonas/${id}`); }

  turnos(): Observable<Turno[]> { return this.http.get<Turno[]>(`${this.api}/turnos`); }
  turnosHoy(): Observable<Turno[]> { return this.http.get<Turno[]>(`${this.api}/turnos/hoy`); }
  turnosProximos(dias = 7): Observable<Turno[]> { return this.http.get<Turno[]>(`${this.api}/turnos/proximos?dias=${dias}`); }
  turnosSinCobertura(): Observable<Turno[]> { return this.http.get<Turno[]>(`${this.api}/turnos/sin-cobertura`); }
  turnosReasignables(): Observable<Turno[]> { return this.http.get<Turno[]>(`${this.api}/turnos/reasignables`); }
  crearTurno(data: Turno): Observable<Turno> { return this.http.post<Turno>(`${this.api}/turnos`, data); }
  actualizarTurno(id: number, data: Turno): Observable<Turno> { return this.http.put<Turno>(`${this.api}/turnos/${id}`, data); }
  eliminarTurno(id: number): Observable<void> { return this.http.delete<void>(`${this.api}/turnos/${id}`); }
  checkInTurno(id: number): Observable<Turno> { return this.http.post<Turno>(`${this.api}/turnos/${id}/iniciar-sin-codigo`, {}); }
  cerrarTurno(id: number, calificacionLimpieza: number): Observable<Turno> { return this.http.post<Turno>(`${this.api}/turnos/${id}/finalizar`, { calificacionLimpieza }); }

  incidentes(): Observable<Incidente[]> { return this.http.get<Incidente[]>(`${this.api}/incidentes`); }
  crearIncidente(data: Incidente): Observable<Incidente> { return this.http.post<Incidente>(`${this.api}/incidentes`, data); }
  actualizarIncidente(id: number, data: Incidente): Observable<Incidente> { return this.http.put<Incidente>(`${this.api}/incidentes/${id}`, data); }
  eliminarIncidente(id: number): Observable<void> { return this.http.delete<void>(`${this.api}/incidentes/${id}`); }

  reasignaciones(): Observable<Reasignacion[]> { return this.http.get<Reasignacion[]>(`${this.api}/reasignaciones`); }
  reasignacionesPendientesVigentes(): Observable<Reasignacion[]> { return this.http.get<Reasignacion[]>(`${this.api}/reasignaciones/pendientes-vigentes`); }
  crearReasignacion(data: Reasignacion): Observable<Reasignacion> { return this.http.post<Reasignacion>(`${this.api}/reasignaciones`, data); }
  actualizarReasignacion(id: number, data: Reasignacion): Observable<Reasignacion> { return this.http.put<Reasignacion>(`${this.api}/reasignaciones/${id}`, data); }
  eliminarReasignacion(id: number): Observable<void> { return this.http.delete<void>(`${this.api}/reasignaciones/${id}`); }

  notificaciones(): Observable<Notificacion[]> { return this.http.get<Notificacion[]>(`${this.api}/notificaciones`); }
  crearNotificacion(data: Notificacion): Observable<Notificacion> { return this.http.post<Notificacion>(`${this.api}/notificaciones`, data); }
  actualizarNotificacion(id: number, data: Notificacion): Observable<Notificacion> { return this.http.put<Notificacion>(`${this.api}/notificaciones/${id}`, data); }
  eliminarNotificacion(id: number): Observable<void> { return this.http.delete<void>(`${this.api}/notificaciones/${id}`); }
}
