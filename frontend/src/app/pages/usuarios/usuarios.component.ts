import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Meta, Usuario } from '../../core/models';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="page-header">
      <div><h1>Usuarios</h1><p>CRUD de docentes, coordinadores y administradores.</p></div>
      <button class="secondary" (click)="nuevo()">Nuevo usuario</button>
    </section>

    <div *ngIf="error" class="error">{{ error }}</div>

    <section class="grid cols-2">
      <article class="card">
        <h2>{{ editando ? 'Editar usuario' : 'Crear usuario' }}</h2>
        <form class="form" (ngSubmit)="guardar()">
          <label>Nombre completo <input name="nombreCompleto" [(ngModel)]="form.nombreCompleto" required></label>
          <label>Correo <input name="correo" type="email" [(ngModel)]="form.correo" required></label>
          <label>Password demo <input name="password" [(ngModel)]="form.password" placeholder="123456"></label>
          <label>Rol
            <select name="rol" [(ngModel)]="form.rol" required>
              <option *ngFor="let rol of meta?.roles" [ngValue]="rol">{{ rol }}</option>
            </select>
          </label>
          <label><span>Activo</span><select name="activo" [(ngModel)]="form.activo"><option [ngValue]="true">Sí</option><option [ngValue]="false">No</option></select></label>
          <div class="actions full">
            <button type="submit">{{ editando ? 'Guardar cambios' : 'Crear' }}</button>
            <button class="ghost" type="button" (click)="nuevo()">Limpiar</button>
          </div>
        </form>
      </article>

      <article class="card">
        <h2>Usuarios registrados</h2>
        <table *ngIf="usuarios.length > 0; else empty">
          <thead><tr><th>Nombre</th><th>Rol</th><th>Estado</th><th>Acciones</th></tr></thead>
          <tbody>
            <tr *ngFor="let usuario of usuarios">
              <td><strong>{{ usuario.nombreCompleto }}</strong><br><small>{{ usuario.correo }}</small></td>
              <td><span class="badge">{{ usuario.rol }}</span></td>
              <td><span class="badge" [class.green]="usuario.activo" [class.red]="!usuario.activo">{{ usuario.activo ? 'Activo' : 'Inactivo' }}</span></td>
              <td class="actions">
                <button class="ghost" (click)="editar(usuario)">Editar</button>
                <button class="danger" *ngIf="usuario.id" (click)="eliminar(usuario.id)">Eliminar</button>
              </td>
            </tr>
          </tbody>
        </table>
        <ng-template #empty><p class="empty">No hay usuarios.</p></ng-template>
      </article>
    </section>
  `
})
export class UsuariosComponent implements OnInit {
  usuarios: Usuario[] = [];
  meta?: Meta;
  editando = false;
  error = '';
  form: Usuario = this.base();

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void { this.cargar(); this.api.meta().subscribe(meta => this.meta = meta); }

  cargar(): void { this.api.usuarios().subscribe({ next: data => this.usuarios = data, error: () => this.error = 'No se pudieron cargar usuarios.' }); }
  nuevo(): void { this.editando = false; this.form = this.base(); }

  editar(usuario: Usuario): void {
    this.editando = true;
    this.form = { ...usuario, password: usuario.password || '' };
  }

  guardar(): void {
    const request = this.form.id
      ? this.api.actualizarUsuario(this.form.id, this.form)
      : this.api.crearUsuario(this.form);
    request.subscribe({ next: () => { this.nuevo(); this.cargar(); }, error: () => this.error = 'No se pudo guardar el usuario.' });
  }

  eliminar(id: number): void {
    this.api.eliminarUsuario(id).subscribe({ next: () => this.cargar(), error: () => this.error = 'No se pudo eliminar el usuario.' });
  }

  private base(): Usuario {
    return { nombreCompleto: '', correo: '', password: '123456', rol: 'DOCENTE', activo: true };
  }
}
