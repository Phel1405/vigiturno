import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Incidente, Meta, Turno, Zona } from '../../core/models';

@Component({
  selector: 'app-incidentes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page-header">
      <div><h1>Incidentes</h1><p>Registro rápido en máximo tres pasos: tipo, severidad y ubicación.</p></div>
      <button class="secondary" (click)="nuevo()">Nuevo incidente</button>
    </section>

    <div *ngIf="error" class="error">{{ error }}</div>

    <section class="card">
      <h2>{{ editando ? 'Editar incidente' : 'Registrar incidente' }}</h2>
      <form class="form" (ngSubmit)="guardar()">
        <label>Tipo
          <select name="tipo" [(ngModel)]="form.tipo" required>
            <option *ngFor="let tipo of meta?.tiposIncidente" [ngValue]="tipo">{{ tipo }}</option>
          </select>
        </label>
        <label>Severidad
          <select name="severidad" [(ngModel)]="form.severidad" required>
            <option *ngFor="let severidad of meta?.severidadesIncidente" [ngValue]="severidad">{{ severidad }}</option>
          </select>
        </label>
        <label>Fecha y hora <input name="fechaHora" type="datetime-local" [(ngModel)]="form.fechaHora" required></label>
        <label>Turno asociado
          <select name="turnoId" [(ngModel)]="form.turnoId">
            <option [ngValue]="null">Sin turno específico</option>
            <option *ngFor="let turno of turnos" [ngValue]="turno.id">{{ turno.fecha }} · {{ turno.usuarioNombre }} · {{ turno.zonaNombre }}</option>
          </select>
        </label>
        <label>Zona manual
          <select name="zonaId" [(ngModel)]="form.zonaId">
            <option [ngValue]="null">Usar zona del turno</option>
            <option *ngFor="let zona of zonas" [ngValue]="zona.id">{{ zona.nombre }}</option>
          </select>
        </label>
        <label>Nombre estudiante <input name="nombreEstudiante" [(ngModel)]="form.nombreEstudiante" placeholder="Solo si aplica"></label>
        <label>Curso estudiante <input name="cursoEstudiante" [(ngModel)]="form.cursoEstudiante" placeholder="Ej. 7B"></label>
        <label class="full">Descripción <textarea name="descripcion" [(ngModel)]="form.descripcion" required></textarea></label>
        <div class="actions full">
          <button type="submit">{{ editando ? 'Guardar cambios' : 'Registrar' }}</button>
          <button class="ghost" type="button" (click)="nuevo()">Limpiar</button>
        </div>
      </form>
    </section>

    <section class="card" style="margin-top:16px">
      <h2>Historial</h2>
      <table *ngIf="incidentes.length > 0; else empty">
        <thead><tr><th>Fecha</th><th>Tipo</th><th>Severidad</th><th>Zona</th><th>Descripción</th><th>Acciones</th></tr></thead>
        <tbody>
          <tr *ngFor="let incidente of incidentes">
            <td>{{ incidente.fechaHora }}</td>
            <td>{{ incidente.tipo }}</td>
            <td><span class="badge" [class.red]="incidente.severidad === 'S3'" [class.yellow]="incidente.severidad === 'S2'">{{ incidente.severidad }}</span></td>
            <td>{{ incidente.zonaNombre || incidente.zonaId || '—' }}</td>
            <td>{{ incidente.descripcion }}</td>
            <td class="actions">
              <button class="ghost" (click)="editar(incidente)">Editar</button>
              <button class="danger" *ngIf="incidente.id" (click)="eliminar(incidente.id)">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>
      <ng-template #empty><p class="empty">No hay incidentes.</p></ng-template>
    </section>
  `
})
export class IncidentesComponent implements OnInit {
  incidentes: Incidente[] = [];
  turnos: Turno[] = [];
  zonas: Zona[] = [];
  meta?: Meta;
  editando = false;
  error = '';
  form: Incidente = this.base();

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.cargar();
    this.api.turnos().subscribe(data => this.turnos = data);
    this.api.zonas().subscribe(data => this.zonas = data);
    this.api.meta().subscribe(meta => this.meta = meta);
  }

  cargar(): void { this.api.incidentes().subscribe({ next: data => this.incidentes = data, error: () => this.error = 'No se pudieron cargar incidentes.' }); }
  nuevo(): void { this.editando = false; this.form = this.base(); }
  editar(incidente: Incidente): void { this.editando = true; this.form = { ...incidente, fechaHora: this.normalizarFechaHora(incidente.fechaHora) }; }

  guardar(): void {
    const payload = {
      ...this.form,
      turnoId: this.form.turnoId ? Number(this.form.turnoId) : null,
      zonaId: this.form.zonaId ? Number(this.form.zonaId) : null
    };
    const request = payload.id ? this.api.actualizarIncidente(payload.id, payload) : this.api.crearIncidente(payload);
    request.subscribe({ next: () => { this.nuevo(); this.cargar(); }, error: () => this.error = 'No se pudo guardar el incidente. Si no escoges turno, debes escoger zona.' });
  }

  eliminar(id: number): void { this.api.eliminarIncidente(id).subscribe({ next: () => this.cargar(), error: () => this.error = 'No se pudo eliminar el incidente.' }); }

  private normalizarFechaHora(value: string): string { return value?.slice(0, 16) || this.ahora(); }
  private ahora(): string { return new Date().toISOString().slice(0, 16); }
  private base(): Incidente {
    return { tipo: 'SEGURIDAD', severidad: 'S1', descripcion: '', fechaHora: this.ahora(), turnoId: null, zonaId: null, nombreEstudiante: '', cursoEstudiante: '' };
  }
}
