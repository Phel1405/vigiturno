import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Meta, Notificacion, Usuario } from '../../core/models';

@Component({
  selector: 'app-notificaciones',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page-header">
      <div><h1>Notificaciones</h1><p>Recordatorios, alertas de ausencia y avisos operativos.</p></div>
      <button class="secondary" (click)="nuevo()">Nueva notificación</button>
    </section>

    <div *ngIf="error" class="error">{{ error }}</div>

    <section class="card">
      <h2>{{ editando ? 'Editar notificación' : 'Crear notificación' }}</h2>
      <form class="form" (ngSubmit)="guardar()">
        <label>Tipo
          <select name="tipo" [(ngModel)]="form.tipo" required>
            <option *ngFor="let tipo of meta?.tiposNotificacion" [ngValue]="tipo">{{ tipo }}</option>
          </select>
        </label>
        <label>Usuario
          <select name="usuarioId" [(ngModel)]="form.usuarioId" required>
            <option *ngFor="let usuario of usuarios" [ngValue]="usuario.id">{{ usuario.nombreCompleto }} — {{ usuario.rol }}</option>
          </select>
        </label>
        <label>Fecha y hora <input name="fechaHora" type="datetime-local" [(ngModel)]="form.fechaHora" required></label>
        <label><span>Leída</span><select name="leida" [(ngModel)]="form.leida"><option [ngValue]="false">No</option><option [ngValue]="true">Sí</option></select></label>
        <label class="full">Mensaje <textarea name="mensaje" [(ngModel)]="form.mensaje" required></textarea></label>
        <div class="actions full">
          <button type="submit">{{ editando ? 'Guardar cambios' : 'Crear' }}</button>
          <button class="ghost" type="button" (click)="nuevo()">Limpiar</button>
        </div>
      </form>
    </section>

    <section class="card" style="margin-top:16px">
      <h2>Listado</h2>
      <table *ngIf="notificaciones.length > 0; else empty">
        <thead><tr><th>Fecha</th><th>Usuario</th><th>Tipo</th><th>Estado</th><th>Mensaje</th><th>Acciones</th></tr></thead>
        <tbody>
          <tr *ngFor="let item of notificaciones">
            <td>{{ item.fechaHora }}</td>
            <td>{{ item.usuarioNombre }}</td>
            <td>{{ item.tipo }}</td>
            <td><span class="badge" [class.green]="item.leida" [class.yellow]="!item.leida">{{ item.leida ? 'Leída' : 'Pendiente' }}</span></td>
            <td>{{ item.mensaje }}</td>
            <td class="actions">
              <button class="ghost" (click)="editar(item)">Editar</button>
              <button class="danger" *ngIf="item.id" (click)="eliminar(item.id)">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>
      <ng-template #empty><p class="empty">No hay notificaciones.</p></ng-template>
    </section>
  `
})
export class NotificacionesComponent implements OnInit {
  notificaciones: Notificacion[] = [];
  usuarios: Usuario[] = [];
  meta?: Meta;
  editando = false;
  error = '';
  form: Notificacion = this.base();

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.cargar();
    this.api.usuarios().subscribe(data => this.usuarios = data);
    this.api.meta().subscribe(meta => this.meta = meta);
  }

  cargar(): void { this.api.notificaciones().subscribe({ next: data => this.notificaciones = data, error: () => this.error = 'No se pudieron cargar notificaciones.' }); }
  nuevo(): void { this.editando = false; this.form = this.base(); }
  editar(item: Notificacion): void { this.editando = true; this.form = { ...item, fechaHora: this.normalizarFechaHora(item.fechaHora) }; }

  guardar(): void {
    const payload = { ...this.form, usuarioId: Number(this.form.usuarioId) };
    const request = payload.id ? this.api.actualizarNotificacion(payload.id, payload) : this.api.crearNotificacion(payload);
    request.subscribe({ next: () => { this.nuevo(); this.cargar(); }, error: () => this.error = 'No se pudo guardar la notificación.' });
  }

  eliminar(id: number): void { this.api.eliminarNotificacion(id).subscribe({ next: () => this.cargar(), error: () => this.error = 'No se pudo eliminar la notificación.' }); }

  private normalizarFechaHora(value: string): string { return value?.slice(0, 16) || this.ahora(); }
  private ahora(): string { return new Date().toISOString().slice(0, 16); }
  private base(): Notificacion {
    return { tipo: 'RECORDATORIO_TURNO', mensaje: '', fechaHora: this.ahora(), leida: false, usuarioId: 0 };
  }
}
