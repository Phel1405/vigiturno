import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Meta, Turno, Usuario, Zona } from '../../core/models';

@Component({
  selector: 'app-turnos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page-header">
      <div><h1>Turnos</h1><p>Calendario operativo con check-in y cierre de turno.</p></div>
      <button *ngIf="isAdmin()" class="secondary" (click)="nuevo()">Nuevo turno</button>
    </section>

    <div *ngIf="error" class="error">{{ error }}</div>

    <section class="card" *ngIf="isAdmin() && (editando || form.id === undefined)">
      <h2>{{ editando ? 'Editar turno' : 'Crear turno' }}</h2>
      <form class="form" (ngSubmit)="guardar()">
        <label>Fecha <input name="fecha" type="date" [(ngModel)]="form.fecha" required></label>
        <label>Estado
          <select name="estado" [(ngModel)]="form.estado" required>
            <option *ngFor="let estado of meta?.estadosTurno" [ngValue]="estado">{{ estado }}</option>
          </select>
        </label>
        <label>Hora inicio <input name="horaInicio" type="time" [(ngModel)]="form.horaInicio" required></label>
        <label>Hora fin <input name="horaFin" type="time" [(ngModel)]="form.horaFin" required></label>
        <label>Docente
          <select name="usuarioId" [(ngModel)]="form.usuarioId" required>
            <option *ngFor="let docente of docentes" [ngValue]="docente.id">{{ docente.nombreCompleto }}</option>
          </select>
        </label>
        <label>Zona
          <select name="zonaId" [(ngModel)]="form.zonaId" required>
            <option *ngFor="let zona of zonas" [ngValue]="zona.id">{{ zona.nombre }}</option>
          </select>
        </label>
        <div class="actions full">
          <button type="submit">{{ editando ? 'Guardar cambios' : 'Crear' }}</button>
          <button class="ghost" type="button" (click)="nuevo()">Limpiar</button>
        </div>
      </form>
    </section>

    <section class="card" style="margin-top:16px">
      <h2>Listado de turnos</h2>
      <table *ngIf="turnos.length > 0; else empty">
        <thead><tr><th>Fecha</th><th>Horario</th><th>Docente</th><th>Zona</th><th>Estado</th><th>Acciones</th></tr></thead>
        <tbody>
          <tr *ngFor="let turno of turnos">
            <td>{{ turno.fecha }}</td>
            <td>{{ turno.horaInicio }} - {{ turno.horaFin }}</td>
            <td>{{ turno.usuarioNombre }}</td>
            <td>{{ turno.zonaNombre }}</td>
            <td><span class="badge" [class.green]="(turno.estadoOperativo || turno.estado) === 'EN_CURSO'" [class.yellow]="(turno.estadoOperativo || turno.estado) === 'PENDIENTE'">{{ turno.estadoOperativo || turno.estado }}</span></td>
            <td class="actions">
              <!-- Default Actions -->
              <ng-container *ngIf="actionTurnoId !== turno.id">
                <button class="success" *ngIf="turno.id && turno.puedeCheckIn && (!isAdmin() || isMiTurno(turno))" (click)="iniciarCheckIn(turno.id)">Iniciar turno</button>
                <button class="warning" *ngIf="turno.id && (turno.estadoOperativo || turno.estado) === 'EN_CURSO' && (!isAdmin() || isMiTurno(turno))" (click)="iniciarCerrar(turno.id)">Finalizar turno</button>
                <button class="ghost" *ngIf="isAdmin()" (click)="editar(turno)">Editar</button>
                <button class="danger" *ngIf="isAdmin() && turno.id" (click)="eliminar(turno.id)">Eliminar</button>
              </ng-container>
              
              <!-- Check-In Form -->
              <div *ngIf="actionTurnoId === turno.id && actionType === 'CHECK_IN'" style="display:flex; gap:0.5rem; align-items:center;">
                <span style="font-size:0.8rem;">¿Iniciar turno ahora?</span>
                <button class="success" (click)="confirmarCheckIn()">Sí, iniciar</button>
                <button class="ghost" (click)="cancelarAccion()">Cancelar</button>
              </div>

              <!-- Cerrar Form -->
              <div *ngIf="actionTurnoId === turno.id && actionType === 'CERRAR'" style="display:flex; gap:0.5rem; align-items:center;">
                <select [(ngModel)]="actionValue" style="padding:0.25rem;">
                  <option value="">Limpieza...</option>
                  <option value="1">1 - Limpio</option>
                  <option value="2">2 - Algo de basura</option>
                  <option value="3">3 - Mucha basura</option>
                  <option value="4">4 - Crítico</option>
                </select>
                <button class="warning" (click)="confirmarCerrar()">OK</button>
                <button class="ghost" (click)="cancelarAccion()">x</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <ng-template #empty><p class="empty">No hay turnos.</p></ng-template>
    </section>
  `
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

  get docentes(): Usuario[] {
    return this.usuarios.filter(u => u.rol === 'DOCENTE');
  }

  isMiTurno(turno: Turno): boolean {
    return turno.usuarioId === this.usuarioIdActivo;
  }

  ngOnInit(): void {
    this.cargar();
    this.api.usuarios().subscribe(data => this.usuarios = data);
    this.api.zonas().subscribe(data => this.zonas = data);
    this.api.meta().subscribe(meta => this.meta = meta);
  }

  cargar(): void { 
    this.api.turnosProximos(7).subscribe({ 
      next: data => {
        this.turnos = this.isAdmin() ? data : data.filter(t => t.usuarioId === this.usuarioIdActivo);
      }, 
      error: () => this.error = 'No se pudieron cargar turnos.' 
    }); 
  }
  nuevo(): void { this.editando = false; this.form = this.base(); }
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
}
