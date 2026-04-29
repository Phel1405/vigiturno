import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/api.service';
import { Dashboard, HeatmapZona, Incidente, Turno } from '../../core/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="page-header">
      <div>
        <h1>Dashboard operativo</h1>
        <p>Resumen en vivo para convertir VigiTurno en una experiencia SPA.</p>
      </div>
      <button class="secondary" (click)="cargar()">Actualizar</button>
    </section>

    <div *ngIf="error" class="error">{{ error }}</div>

    <section class="grid cols-3" *ngIf="dashboard">
      <article class="card metric"><span>Usuarios</span><strong>{{ dashboard.totalUsuarios }}</strong></article>
      <article class="card metric"><span>Zonas</span><strong>{{ dashboard.totalZonas }}</strong></article>
      <article class="card metric"><span>Turnos</span><strong>{{ dashboard.totalTurnos }}</strong></article>
      <article class="card metric"><span>Incidentes</span><strong>{{ dashboard.totalIncidentes }}</strong></article>
      <article class="card metric"><span>Reasignaciones</span><strong>{{ dashboard.totalReasignaciones }}</strong></article>
      <article class="card metric"><span>Notificaciones</span><strong>{{ dashboard.totalNotificaciones }}</strong></article>
    </section>

    <section class="grid cols-2" style="margin-top:16px">
      <article class="card">
        <h2>Mapa de calor por zona</h2>
        <p class="empty" *ngIf="heatmap.length === 0">Sin datos de incidentes todavía.</p>
        <div class="heat-row" *ngFor="let zona of heatmap">
          <strong>{{ zona.zonaNombre }}</strong>
          <div class="bar"><span [style.width.%]="zona.porcentaje"></span></div>
          <span>{{ zona.porcentaje }}%</span>
        </div>
      </article>

      <article class="card">
        <h2>Últimos incidentes</h2>
        <table *ngIf="incidentes.length > 0; else emptyIncidentes">
          <thead><tr><th>Tipo</th><th>Zona</th><th>Severidad</th></tr></thead>
          <tbody>
            <tr *ngFor="let item of incidentes.slice(0, 6)">
              <td>{{ item.tipo }}</td>
              <td>{{ item.zonaNombre || '—' }}</td>
              <td><span class="badge" [class.red]="item.severidad === 'S3'" [class.yellow]="item.severidad === 'S2'">{{ item.severidad }}</span></td>
            </tr>
          </tbody>
        </table>
        <ng-template #emptyIncidentes><p class="empty">Aún no hay incidentes registrados.</p></ng-template>
      </article>
    </section>

    <section class="card" style="margin-top:16px">
      <h2>Turnos recientes</h2>
      <table *ngIf="turnos.length > 0; else emptyTurnos">
        <thead><tr><th>Fecha</th><th>Horario</th><th>Docente</th><th>Zona</th><th>Estado</th></tr></thead>
        <tbody>
          <tr *ngFor="let turno of turnos.slice(0, 8)">
            <td>{{ turno.fecha }}</td>
            <td>{{ turno.horaInicio }} - {{ turno.horaFin }}</td>
            <td>{{ turno.usuarioNombre }}</td>
            <td>{{ turno.zonaNombre }}</td>
            <td><span class="badge" [class.green]="turno.estado === 'EN_CURSO'" [class.yellow]="turno.estado === 'PENDIENTE'">{{ turno.estado }}</span></td>
          </tr>
        </tbody>
      </table>
      <ng-template #emptyTurnos><p class="empty">Aún no hay turnos creados.</p></ng-template>
    </section>
  `
})
export class DashboardComponent implements OnInit {
  dashboard?: Dashboard;
  heatmap: HeatmapZona[] = [];
  incidentes: Incidente[] = [];
  turnos: Turno[] = [];
  error = '';

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.error = '';
    this.api.dashboard().subscribe({ next: data => this.dashboard = data, error: () => this.error = 'No se pudo cargar el dashboard. Revisa que Spring Boot esté corriendo en el puerto 8080.' });
    this.api.mapaCalor().subscribe({ next: data => this.heatmap = data });
    this.api.incidentes().subscribe({ next: data => this.incidentes = data });
    this.api.turnos().subscribe({ next: data => this.turnos = data });
  }
}
