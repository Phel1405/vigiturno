import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Meta, Reasignacion, Turno, Usuario } from '../../core/models';

@Component({
  selector: 'app-reasignaciones',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page-header">
      <div><h1>Reasignaciones</h1><p>Solicitud de reemplazos con trazabilidad del proceso.</p></div>
      <button class="secondary" (click)="nuevo()">Nueva reasignación</button>
    </section>

    <div *ngIf="error" class="error">{{ error }}</div>

    <section class="card">
      <h2>{{ editando ? 'Editar reasignación' : 'Crear reasignación' }}</h2>
      <form class="form" (ngSubmit)="guardar()">
        <label *ngIf="isAdmin()">Turno
          <select name="turnoId" [(ngModel)]="form.turnoId" required>
            <option *ngFor="let turno of turnos" [ngValue]="turno.id">{{ turno.fecha }} · {{ turno.horaInicio }}-{{ turno.horaFin }} · {{ turno.usuarioNombre }} · {{ turno.zonaNombre }}</option>
          </select>
        </label>
        <label *ngIf="!isAdmin()">Mis Turnos
          <select name="turnoId" [(ngModel)]="form.turnoId" required>
            <option *ngFor="let turno of misTurnos" [ngValue]="turno.id">{{ turno.fecha }} · {{ turno.horaInicio }}-{{ turno.horaFin }} · {{ turno.zonaNombre }}</option>
          </select>
        </label>
        <ng-container *ngIf="isAdmin()">
          <label>Estado
            <select name="estado" [(ngModel)]="form.estado" required>
              <option *ngFor="let estado of meta?.estadosReasignacion" [ngValue]="estado">{{ estado }}</option>
            </select>
          </label>
          <label>Docente original
            <select name="docenteOriginalId" [(ngModel)]="form.docenteOriginalId" required>
              <option *ngFor="let usuario of usuarios" [ngValue]="usuario.id">{{ usuario.nombreCompleto }} — {{ usuario.rol }}</option>
            </select>
          </label>
          <label>Docente reemplazo
            <select name="docenteReemplazoId" [(ngModel)]="form.docenteReemplazoId">
              <option [ngValue]="null">Pendiente</option>
              <option *ngFor="let usuario of usuarios" [ngValue]="usuario.id">{{ usuario.nombreCompleto }} — {{ usuario.rol }}</option>
            </select>
          </label>
          <label>Solicitud <input name="fechaHoraSolicitud" type="datetime-local" [(ngModel)]="form.fechaHoraSolicitud" required></label>
          <label>Respuesta <input name="fechaHoraRespuesta" type="datetime-local" [(ngModel)]="form.fechaHoraRespuesta"></label>
        </ng-container>
        <label class="full">Motivo <textarea name="motivo" [(ngModel)]="form.motivo" required></textarea></label>
        <div class="actions full">
          <button type="submit">{{ editando ? 'Guardar cambios' : 'Crear' }}</button>
          <button class="ghost" type="button" (click)="nuevo()">Limpiar</button>
        </div>
      </form>
    </section>

    <section class="card" style="margin-top:16px">
      <h2>Solicitudes</h2>
      <table *ngIf="reasignaciones.length > 0; else empty">
        <thead><tr><th>Turno</th><th>Original</th><th>Reemplazo</th><th>Estado</th><th>Motivo</th><th>Acciones</th></tr></thead>
        <tbody>
          <tr *ngFor="let item of reasignaciones">
            <td>#{{ item.turnoId }}</td>
            <td>{{ item.docenteOriginalNombre }}</td>
            <td>{{ item.docenteReemplazoNombre || 'Pendiente' }}</td>
            <td><span class="badge" [class.green]="item.estado === 'ACEPTADA'" [class.red]="item.estado === 'RECHAZADA'" [class.yellow]="item.estado === 'PENDIENTE'">{{ item.estado }}</span></td>
            <td>{{ item.motivo }}</td>
            <td class="actions">
              <button class="ghost" *ngIf="isAdmin()" (click)="editar(item)">Gestionar</button>
              <button class="danger" *ngIf="isAdmin() && item.id" (click)="eliminar(item.id)">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>
      <ng-template #empty><p class="empty">No hay reasignaciones.</p></ng-template>
    </section>
  `
})
export class ReasignacionesComponent implements OnInit {
  reasignaciones: Reasignacion[] = [];
  turnos: Turno[] = [];
  usuarios: Usuario[] = [];
  meta?: Meta;
  editando = false;
  error = '';
  form: Reasignacion = this.base();

  usuarioIdActivo: number | null = null;
  rolActivo: string = 'ADMINISTRADOR';

  constructor(private readonly api: ApiService) {
    const stored = localStorage.getItem('usuarioActivo');
    if (stored) {
      this.usuarioIdActivo = Number(stored);
      this.api.usuarios().subscribe(us => {
        const u = us.find(x => x.id === this.usuarioIdActivo);
        if (u) this.rolActivo = u.rol;
      });
    }
  }

  isAdmin(): boolean {
    return this.rolActivo !== 'DOCENTE';
  }

  get misTurnos(): Turno[] {
    return this.turnos.filter(t => t.usuarioId === this.usuarioIdActivo);
  }

  ngOnInit(): void {
    this.cargar();
    this.api.turnosReasignables().subscribe(data => this.turnos = data);
    this.api.usuarios().subscribe(data => this.usuarios = data);
    this.api.meta().subscribe(meta => this.meta = meta);
  }

  cargar(): void { 
    this.api.reasignacionesPendientesVigentes().subscribe({ 
      next: data => {
        this.reasignaciones = this.isAdmin() ? data : data.filter(r => r.docenteOriginalId === this.usuarioIdActivo);
      }, 
      error: () => this.error = 'No se pudieron cargar reasignaciones.' 
    }); 
  }
  nuevo(): void { this.editando = false; this.form = this.base(); }
  editar(item: Reasignacion): void {
    this.editando = true;
    this.form = {
      ...item,
      fechaHoraSolicitud: this.normalizarFechaHora(item.fechaHoraSolicitud),
      fechaHoraRespuesta: item.fechaHoraRespuesta ? this.normalizarFechaHora(item.fechaHoraRespuesta) : null
    };
  }

  guardar(): void {
    const payload = {
      ...this.form,
      turnoId: Number(this.form.turnoId),
      docenteOriginalId: this.isAdmin() ? Number(this.form.docenteOriginalId) : (this.usuarioIdActivo ?? 0),
      docenteReemplazoId: this.form.docenteReemplazoId ? Number(this.form.docenteReemplazoId) : null
    };
    const request = payload.id ? this.api.actualizarReasignacion(payload.id, payload) : this.api.crearReasignacion(payload);
    request.subscribe({ next: () => { this.nuevo(); this.cargar(); }, error: () => this.error = 'No se pudo guardar la reasignación.' });
  }

  eliminar(id: number): void { this.api.eliminarReasignacion(id).subscribe({ next: () => this.cargar(), error: () => this.error = 'No se pudo eliminar la reasignación.' }); }

  private normalizarFechaHora(value: string): string { return value?.slice(0, 16) || this.ahora(); }
  private ahora(): string { return new Date().toISOString().slice(0, 16); }
  private base(): Reasignacion {
    return { motivo: '', fechaHoraSolicitud: this.ahora(), fechaHoraRespuesta: null, estado: 'PENDIENTE', turnoId: 0, docenteOriginalId: 0, docenteReemplazoId: null };
  }
}
