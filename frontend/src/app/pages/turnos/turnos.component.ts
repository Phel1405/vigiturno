import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Meta, Turno, Usuario, Zona } from '../../core/models';

@Component({
  selector: 'app-turnos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page-header">
      <div><h1>Turnos</h1><p>Calendario operativo con check-in y cierre de turno.</p></div>
      <button class="secondary" (click)="nuevo()">Nuevo turno</button>
    </section>

    <div *ngIf="error" class="error">{{ error }}</div>

    <section class="card">
      <h2>{{ editando ? 'Editar turno' : 'Crear turno' }}</h2>
      <form class="form" (ngSubmit)="guardar()">
        <label>Fecha <input name="fecha" type="date" [(ngModel)]="form.fecha" required></label>
        <label>Estado
          <select name="estado" [(ngModel)]="form.estado" required>
            <option *ngFor="let estado of meta?.estadosTurno" [ngValue]="estado">{{ estado }}</option>
          </select>
        </label>
        <label>Hora inicio <input name="horaInicio" type="time" [(ngModel)]="form.horaInicio" required></label>
        <label>Hora fin <input name="horaFin" type="time" [(ngModel)]="form.horaFin" required></label>
        <label>Docente
          <select name="usuarioId" [(ngModel)]="form.usuarioId" required>
            <option *ngFor="let usuario of usuarios" [ngValue]="usuario.id">{{ usuario.nombreCompleto }} — {{ usuario.rol }}</option>
          </select>
        </label>
        <label>Zona
          <select name="zonaId" [(ngModel)]="form.zonaId" required>
            <option *ngFor="let zona of zonas" [ngValue]="zona.id">{{ zona.nombre }}</option>
          </select>
        </label>
        <div class="actions full">
          <button type="submit">{{ editando ? 'Guardar cambios' : 'Crear' }}</button>
          <button class="ghost" type="button" (click)="nuevo()">Limpiar</button>
        </div>
      </form>
    </section>

    <section class="card" style="margin-top:16px">
      <h2>Listado de turnos</h2>
      <table *ngIf="turnos.length > 0; else empty">
        <thead><tr><th>Fecha</th><th>Horario</th><th>Docente</th><th>Zona</th><th>Estado</th><th>Acciones</th></tr></thead>
        <tbody>
          <tr *ngFor="let turno of turnos">
            <td>{{ turno.fecha }}</td>
            <td>{{ turno.horaInicio }} - {{ turno.horaFin }}</td>
            <td>{{ turno.usuarioNombre }}</td>
            <td>{{ turno.zonaNombre }}</td>
            <td><span class="badge" [class.green]="turno.estado === 'EN_CURSO'" [class.yellow]="turno.estado === 'PENDIENTE'">{{ turno.estado }}</span></td>
            <td class="actions">
              <button class="success" *ngIf="turno.id" (click)="checkIn(turno.id)">Check-in</button>
              <button class="warning" *ngIf="turno.id" (click)="cerrar(turno.id)">Cerrar</button>
              <button class="ghost" (click)="editar(turno)">Editar</button>
              <button class="danger" *ngIf="turno.id" (click)="eliminar(turno.id)">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>
      <ng-template #empty><p class="empty">No hay turnos.</p></ng-template>
    </section>
  `
})
export class TurnosComponent implements OnInit {
  turnos: Turno[] = [];
  usuarios: Usuario[] = [];
  zonas: Zona[] = [];
  meta?: Meta;
  editando = false;
  error = '';
  form: Turno = this.base();

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.cargar();
    this.api.usuarios().subscribe(data => this.usuarios = data);
    this.api.zonas().subscribe(data => this.zonas = data);
    this.api.meta().subscribe(meta => this.meta = meta);
  }

  cargar(): void { this.api.turnos().subscribe({ next: data => this.turnos = data, error: () => this.error = 'No se pudieron cargar turnos.' }); }
  nuevo(): void { this.editando = false; this.form = this.base(); }
  editar(turno: Turno): void { this.editando = true; this.form = { ...turno, horaInicio: this.normalizarHora(turno.horaInicio), horaFin: this.normalizarHora(turno.horaFin) }; }

  guardar(): void {
    const payload = { ...this.form, usuarioId: Number(this.form.usuarioId), zonaId: Number(this.form.zonaId) };
    const request = payload.id ? this.api.actualizarTurno(payload.id, payload) : this.api.crearTurno(payload);
    request.subscribe({ next: () => { this.nuevo(); this.cargar(); }, error: () => this.error = 'No se pudo guardar el turno.' });
  }

  checkIn(id: number): void { this.api.checkInTurno(id).subscribe({ next: () => this.cargar(), error: () => this.error = 'No se pudo registrar el check-in.' }); }
  cerrar(id: number): void { this.api.cerrarTurno(id).subscribe({ next: () => this.cargar(), error: () => this.error = 'No se pudo cerrar el turno.' }); }
  eliminar(id: number): void { this.api.eliminarTurno(id).subscribe({ next: () => this.cargar(), error: () => this.error = 'No se pudo eliminar el turno.' }); }

  private normalizarHora(hora: string): string { return hora?.slice(0, 5) || '10:00'; }
  private base(): Turno {
    const hoy = new Date().toISOString().slice(0, 10);
    return { fecha: hoy, horaInicio: '10:00', horaFin: '10:30', estado: 'PENDIENTE', usuarioId: 0, zonaId: 0 };
  }
}
