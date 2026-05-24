import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Meta, Notificacion, Usuario } from '../../core/models';

@Component({
  selector: 'app-notificaciones',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './notificaciones.component.html'
})
export class NotificacionesComponent implements OnInit {
  notificaciones: Notificacion[] = [];
  usuarios: Usuario[] = [];
  meta?: Meta;
  editando = false;
  error = '';
  form: Notificacion = this.base();

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
    this.api.usuarios().subscribe(data => this.usuarios = data);
    this.api.meta().subscribe(meta => this.meta = meta);
  }

  cargar(): void { 
    this.api.notificaciones().subscribe({ 
      next: data => {
        this.notificaciones = this.isAdmin() ? data : data.filter(n => n.usuarioId === this.usuarioIdActivo);
      }, 
      error: () => this.error = 'No se pudieron cargar notificaciones.' 
    }); 
  }
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
