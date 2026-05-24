import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Incidente, Meta, Turno, Zona } from '../../core/models';

@Component({
  selector: 'app-incidentes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './incidentes.component.html'
})
export class IncidentesComponent implements OnInit {
  incidentes: Incidente[] = [];
  turnos: Turno[] = [];
  zonas: Zona[] = [];
  meta?: Meta;
  editando = false;
  error = '';
  form: Incidente = this.base();
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

    if (!this.isAdmin()) {
        const misTurnos = this.turnos.filter(t => t.usuarioId === this.usuarioIdActivo);
        const turnoActivo = misTurnos.find(t => t.estadoOperativo === 'EN_CURSO' || t.estado === 'EN_CURSO') || 
                            misTurnos[0]; // fallback to their closest shift
        
        if (turnoActivo) {
            payload.turnoId = turnoActivo.id ?? null;
            payload.zonaId = turnoActivo.zonaId ?? null;
        }
    }
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
