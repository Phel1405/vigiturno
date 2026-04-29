import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Zona } from '../../core/models';

@Component({
  selector: 'app-zonas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page-header">
      <div><h1>Zonas</h1><p>CRUD de lugares físicos de vigilancia.</p></div>
      <button class="secondary" (click)="nuevo()">Nueva zona</button>
    </section>

    <div *ngIf="error" class="error">{{ error }}</div>

    <section class="grid cols-2">
      <article class="card">
        <h2>{{ editando ? 'Editar zona' : 'Crear zona' }}</h2>
        <form class="form" (ngSubmit)="guardar()">
          <label>Nombre <input name="nombre" [(ngModel)]="form.nombre" required></label>
          <label>Capacidad máxima <input name="capacidadMaxima" type="number" [(ngModel)]="form.capacidadMaxima"></label>
          <label class="full">Descripción <textarea name="descripcion" [(ngModel)]="form.descripcion"></textarea></label>
          <label><span>Activa</span><select name="activa" [(ngModel)]="form.activa"><option [ngValue]="true">Sí</option><option [ngValue]="false">No</option></select></label>
          <div class="actions full">
            <button type="submit">{{ editando ? 'Guardar cambios' : 'Crear' }}</button>
            <button class="ghost" type="button" (click)="nuevo()">Limpiar</button>
          </div>
        </form>
      </article>

      <article class="card">
        <h2>Zonas registradas</h2>
        <table *ngIf="zonas.length > 0; else empty">
          <thead><tr><th>Zona</th><th>Capacidad</th><th>Estado</th><th>Acciones</th></tr></thead>
          <tbody>
            <tr *ngFor="let zona of zonas">
              <td><strong>{{ zona.nombre }}</strong><br><small>{{ zona.descripcion || 'Sin descripción' }}</small></td>
              <td>{{ zona.capacidadMaxima || '—' }}</td>
              <td><span class="badge" [class.green]="zona.activa" [class.red]="!zona.activa">{{ zona.activa ? 'Activa' : 'Inactiva' }}</span></td>
              <td class="actions">
                <button class="ghost" (click)="editar(zona)">Editar</button>
                <button class="danger" *ngIf="zona.id" (click)="eliminar(zona.id)">Eliminar</button>
              </td>
            </tr>
          </tbody>
        </table>
        <ng-template #empty><p class="empty">No hay zonas.</p></ng-template>
      </article>
    </section>
  `
})
export class ZonasComponent implements OnInit {
  zonas: Zona[] = [];
  editando = false;
  error = '';
  form: Zona = this.base();

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void { this.cargar(); }
  cargar(): void { this.api.zonas().subscribe({ next: data => this.zonas = data, error: () => this.error = 'No se pudieron cargar zonas.' }); }
  nuevo(): void { this.editando = false; this.form = this.base(); }
  editar(zona: Zona): void { this.editando = true; this.form = { ...zona }; }

  guardar(): void {
    const request = this.form.id ? this.api.actualizarZona(this.form.id, this.form) : this.api.crearZona(this.form);
    request.subscribe({ next: () => { this.nuevo(); this.cargar(); }, error: () => this.error = 'No se pudo guardar la zona.' });
  }

  eliminar(id: number): void {
    this.api.eliminarZona(id).subscribe({ next: () => this.cargar(), error: () => this.error = 'No se pudo eliminar la zona.' });
  }

  private base(): Zona { return { nombre: '', descripcion: '', capacidadMaxima: 30, activa: true }; }
}
