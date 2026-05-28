import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Meta, Turno, Usuario, Zona } from '../../core/models';

@Component({
  selector: 'app-turnos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './turnos.component.html'
})
export class TurnosComponent implements OnInit {
  turnos: Turno[] = [];
  usuarios: Usuario[] = [];
  zonas: Zona[] = [];
  meta?: Meta;
  editando = false;
  error = '';
  form: Turno = this.base();

  usuarioIdActivo: number | null = null;
  rolActivo: string = 'DOCENTE';

  constructor(private readonly api: ApiService) {
    // Leemos directamente los datos reales guardados por el login en app.component / login.component
    const storedId = localStorage.getItem('usuarioId') || localStorage.getItem('usuarioActivo');
    if (storedId) {
      this.usuarioIdActivo = Number(storedId);
    }

    // Obtenemos el rol real guardado en la sesión
    const storedRol = localStorage.getItem('usuarioRol');
    if (storedRol) {
      this.rolActivo = storedRol;
    }
  }

  isAdmin(): boolean {
    return this.rolActivo === 'ADMINISTRADOR' || this.rolActivo === 'COORDINADOR';
  }

  get docentes(): Usuario[] {
    return this.usuarios.filter(u => u.rol === 'DOCENTE');
  }

  isMiTurno(turno: Turno): boolean {
    return turno.usuarioId === this.usuarioIdActivo;
  }
  get turnosFiltrados(): Turno[] {
    if (this.isAdmin()) {
      return this.turnos;
    }else{
      return this.turnos.filter(t => this.isMiTurno(t));
    }
  }

  ngOnInit(): void {
    // 1. Cargar metadatos globales
    this.api.meta().subscribe(meta => this.meta = meta);

    // 2. Si es Administrador/Coordinador, sí necesita la lista de usuarios para el combo "Docente"
    if (this.isAdmin()) {
      this.api.usuarios().subscribe({
        next: (us) => this.usuarios = us,
        error: () => this.error = 'No se pudo cargar la lista de usuarios.'
      });
      // Nota: Como administrador también cargamos las zonas para el formulario
      this.api.zonas().subscribe(z => this.zonas = z);
    }

    // 3. Cargar los turnos inmediatamente (ya conocemos el rol de manera segura y síncrona)
    this.cargar();
  }

  cargar(): void {
    this.api.turnos().subscribe({
      next: (data) => {
        // Hacemos el filtrado drástico aquí apenas llegan los datos del servidor
        if (this.isAdmin()) {
          this.turnos = data; // El Administrador/Coordinador ve todo
        } else {
          // El Docente es filtrado inmediatamente por su ID activo
          this.turnos = data.filter(turno => turno.usuarioId === this.usuarioIdActivo);
        }
      },
      error: () => this.error = 'No se pudieron cargar los turnos.'
    });
  }
  editar(turno: Turno): void { this.editando = true; this.form = { ...turno, horaInicio: this.normalizarHora(turno.horaInicio), horaFin: this.normalizarHora(turno.horaFin) }; }

  guardar(): void {
    const payload = { ...this.form, usuarioId: Number(this.form.usuarioId), zonaId: Number(this.form.zonaId) };
    const request = payload.id ? this.api.actualizarTurno(payload.id, payload) : this.api.crearTurno(payload);
    request.subscribe({ next: () => { this.nuevo(); this.cargar(); }, error: () => this.error = 'No se pudo guardar el turno.' });
  }

  actionTurnoId: number | null = null;
  actionType: 'CHECK_IN' | 'CERRAR' | null = null;
  actionValue: string = '';

  iniciarCheckIn(id: number): void {
    this.actionTurnoId = id;
    this.actionType = 'CHECK_IN';
    this.actionValue = '';
    this.error = '';
  }

  confirmarCheckIn(): void {
    if (!this.actionTurnoId) return;
    this.api.checkInTurno(this.actionTurnoId).subscribe({ 
      next: () => { this.cargar(); this.cancelarAccion(); }, 
      error: (err) => this.error = err?.error?.message || 'No se pudo iniciar el turno.' 
    });
  }
  
  iniciarCerrar(id: number): void {
    this.actionTurnoId = id;
    this.actionType = 'CERRAR';
    this.actionValue = '';
    this.error = '';
  }

  confirmarCerrar(): void {
    const cal = Number(this.actionValue);
    if (!this.actionTurnoId || isNaN(cal) || cal < 1 || cal > 4) {
      this.error = 'Debe seleccionar una calificación válida (1 a 4).';
      return;
    }
    this.api.cerrarTurno(this.actionTurnoId, cal).subscribe({ 
      next: () => { this.cargar(); this.cancelarAccion(); }, 
      error: (err) => this.error = err?.error?.message || 'No se pudo cerrar el turno.' 
    });
  }

  cancelarAccion(): void {
    this.actionTurnoId = null;
    this.actionType = null;
    this.actionValue = '';
  }
  
  eliminar(id: number): void { this.api.eliminarTurno(id).subscribe({ next: () => this.cargar(), error: () => this.error = 'No se pudo eliminar el turno.' }); }

  private normalizarHora(hora: string): string { return hora?.slice(0, 5) || '10:00'; }
  private base(): Turno {
    const hoy = new Date().toISOString().slice(0, 10);
    return { fecha: hoy, horaInicio: '10:00', horaFin: '10:30', estado: 'PENDIENTE', usuarioId: 0, zonaId: 0 };
  }
  nuevo(): void { this.editando = false; this.form = this.base(); }

}
