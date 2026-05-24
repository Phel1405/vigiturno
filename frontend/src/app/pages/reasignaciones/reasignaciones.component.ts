import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Meta, Reasignacion, Turno, Usuario } from '../../core/models';

@Component({
  selector: 'app-reasignaciones',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reasignaciones.component.html'
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
