import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/api.service';
import { Dashboard, HeatmapZona, Incidente, Turno, Zona } from '../../core/models';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html'
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
      
      // Encuentra el turno actual o el siguiente
      let turnoActivo = turnosDeZona.find(t => t.horaInicio <= ahoraStr && t.horaFin >= ahoraStr);
      if (!turnoActivo) {
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
