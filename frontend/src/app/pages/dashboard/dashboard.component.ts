import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/api.service';
import { Dashboard, HeatmapZona, Incidente, Turno, Zona } from '../../core/models';
import { forkJoin } from 'rxjs';

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

    <section class="card" *ngIf="isAdmin()" style="margin-top:16px;">
      <h2>Mapa Operativo de Zonas</h2>
      <p>Estado en tiempo real de las zonas según los turnos de hoy.</p>
      <div class="grid cols-4" style="margin-top:1rem;">
        <article *ngFor="let mz of mapaZonas" class="card" 
                 [ngStyle]="{'border-left': '4px solid ' + mz.color}">
          <h3 style="margin-top: 0;">{{ mz.zona.nombre }}</h3>
          <p style="margin: 0; font-size: 0.9rem; color: #666;">
            <strong>Estado:</strong> {{ mz.estadoStr }}
          </p>
          <p *ngIf="mz.turno" style="margin: 0; font-size: 0.8rem; color: #888;">
            Turno: {{ mz.turno.horaInicio }} - {{ mz.turno.horaFin }} <br>
            Docente: {{ mz.turno.usuarioNombre }}
          </p>
          <p *ngIf="!mz.turno" style="margin: 0; font-size: 0.8rem; color: #888;">
            No hay turno asignado para esta hora.
          </p>
        </article>
      </div>
    </section>

    <section class="card" *ngIf="isAdmin()" style="margin-top:16px;">
      <h2>Mapa de Limpieza de Zonas (Hoy)</h2>
      <p>Último reporte de limpieza registrado en cada zona.</p>
      <div class="grid cols-4" style="margin-top:1rem;">
        <article *ngFor="let mz of mapaLimpieza" class="card" 
                 [ngStyle]="{'border-left': '4px solid ' + mz.color}">
          <h3 style="margin-top: 0;">{{ mz.zona.nombre }}</h3>
          <p style="margin: 0; font-size: 0.9rem;">
             Calificación: <strong>{{ mz.calificacionStr }}</strong>
          </p>
          <p *ngIf="mz.turno" style="margin: 0; font-size: 0.8rem; color: #888;">
             Registrada a las {{ mz.turno.horaFinReal || mz.turno.horaFin }} por {{ mz.turno.usuarioNombre }}
          </p>
        </article>
      </div>
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

  mapaZonas: {zona: Zona, color: string, estadoStr: string, turno?: Turno}[] = [];
  mapaLimpieza: {zona: Zona, color: string, calificacionStr: string, turno?: Turno}[] = [];
  
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

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.error = '';
    this.api.dashboard().subscribe({ next: data => this.dashboard = data, error: () => this.error = 'No se pudo cargar el dashboard. Revisa que Spring Boot esté corriendo en el puerto 8080.' });
    this.api.mapaCalor().subscribe({ next: data => this.heatmap = data });
    this.api.incidentes().subscribe({ next: data => this.incidentes = data });
    this.api.turnos().subscribe({ next: data => {
       this.turnos = this.isAdmin() ? data : data.filter(t => t.usuarioId === this.usuarioIdActivo);
    }});
    
    if (this.isAdmin()) {
      forkJoin({
        zonas: this.api.zonas(),
        turnosHoy: this.api.turnosHoy()
      }).subscribe(({zonas, turnosHoy}) => {
        this.calcularMapaZonas(zonas, turnosHoy);
        this.calcularMapaLimpieza(zonas, turnosHoy);
      });
    }
  }

  private calcularMapaZonas(zonas: Zona[], turnosHoy: Turno[]) {
    const ahoraStr = new Date().toTimeString().slice(0, 5); // "HH:MM"
    
    this.mapaZonas = zonas.map(zona => {
      const turnosDeZona = turnosHoy.filter(t => t.zonaId === zona.id);
      
      // Find current or next shift
      let turnoActivo = turnosDeZona.find(t => t.horaInicio <= ahoraStr && t.horaFin >= ahoraStr);
      if (!turnoActivo) {
         // Find next shift today
         turnoActivo = turnosDeZona.filter(t => t.horaInicio > ahoraStr).sort((a,b) => a.horaInicio.localeCompare(b.horaInicio))[0];
      }

      let color = '#ccc'; // Gris (sin turno asignado)
      let estadoStr = 'Sin cobertura actualmente';
      
      if (turnoActivo) {
         const estadoOp = turnoActivo.estadoOperativo || turnoActivo.estado;
         if (turnoActivo.sinCobertura) {
             color = '#e74c3c'; // Rojo
             estadoStr = 'Sin cobertura (Umbral superado)';
         } else if (estadoOp === 'EN_CURSO') {
             color = '#2ecc71'; // Verde
             estadoStr = 'Cubierta (Turno iniciado)';
         } else if (estadoOp === 'PENDIENTE') {
             color = '#f1c40f'; // Amarillo
             estadoStr = 'Turno próximo o esperando inicio';
         } else if (estadoOp === 'FINALIZADO' || estadoOp === 'CANCELADO') {
             color = '#ccc';
             estadoStr = 'Turno finalizado/cancelado';
         }
      }

      return { zona, color, estadoStr, turno: turnoActivo };
    });
  }

  private calcularMapaLimpieza(zonas: Zona[], turnosHoy: Turno[]) {
    this.mapaLimpieza = zonas.map(zona => {
      const turnosDeZona = turnosHoy.filter(t => t.zonaId === zona.id && t.calificacionLimpieza);
      // Sort to get the latest finished shift with a cleaning rating
      const ultimoTurno = turnosDeZona.sort((a, b) => (b.horaFinReal || b.horaFin).localeCompare(a.horaFinReal || a.horaFin))[0];

      let color = '#ccc'; // Gris (sin datos)
      let calificacionStr = 'Sin registros hoy';
      
      if (ultimoTurno) {
         const cal = ultimoTurno.calificacionLimpieza;
         if (cal === 1) { color = '#2ecc71'; calificacionStr = '1 - Limpio'; }
         else if (cal === 2) { color = '#f1c40f'; calificacionStr = '2 - Algo de basura'; }
         else if (cal === 3) { color = '#e67e22'; calificacionStr = '3 - Mucha basura'; }
         else if (cal === 4) { color = '#e74c3c'; calificacionStr = '4 - Crítico'; }
      }

      return { zona, color, calificacionStr, turno: ultimoTurno };
    });
  }
}
