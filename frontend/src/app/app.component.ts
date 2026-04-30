import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from './core/api.service';
import { Usuario } from './core/models';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule, FormsModule],
  template: `
    <div class="app-shell">
      <aside class="sidebar">
        <div class="brand">
          <span class="brand-mark">VT</span>
          <div>
            <strong>VigiTurno</strong>
            <small>Sistema de Vigilencia Docente</small>
          </div>
        </div>

        <div style="padding: 1rem;">
          <label style="font-size: 0.8rem; color: #666;">Simular Usuario</label>
          <select [(ngModel)]="usuarioSeleccionadoId" (change)="cambiarUsuario()" style="width: 100%; margin-top: 0.5rem; padding: 0.5rem; border-radius: 4px;">
            <option [ngValue]="null">-- Seleccionar (ADMIN por defecto) --</option>
            <option *ngFor="let u of usuarios" [ngValue]="u.id">{{ u.nombreCompleto }} ({{ u.rol }})</option>
          </select>
        </div>

        <nav>
          <!-- ADMIN NAVBAR -->
          <ng-container *ngIf="getRol() === 'ADMINISTRADOR'">
            <div style="padding: 0.5rem 1rem; font-size: 0.75rem; font-weight: bold; color: #999; text-transform: uppercase;">Módulo Administrador</div>
            <a routerLink="/dashboard" routerLinkActive="active">Dashboard General</a>
            <a routerLink="/turnos" routerLinkActive="active">Turnos</a>
            <a routerLink="/incidentes" routerLinkActive="active">Incidentes</a>
            <a routerLink="/reasignaciones" routerLinkActive="active">Reasignaciones</a>
            <a routerLink="/usuarios" routerLinkActive="active">Usuarios</a>
            <a routerLink="/zonas" routerLinkActive="active">Zonas</a>
            <a routerLink="/notificaciones" routerLinkActive="active">Notificaciones</a>
          </ng-container>

          <!-- COORDINADOR NAVBAR -->
          <ng-container *ngIf="getRol() === 'COORDINADOR'">
            <div style="padding: 0.5rem 1rem; font-size: 0.75rem; font-weight: bold; color: #999; text-transform: uppercase;">Módulo Coordinador</div>
            <a routerLink="/dashboard" routerLinkActive="active">Mapa de Zonas</a>
            <a routerLink="/reasignaciones" routerLinkActive="active">Reasignaciones</a>
            <a routerLink="/incidentes" routerLinkActive="active">Incidentes</a>
            <a routerLink="/notificaciones" routerLinkActive="active">Notificaciones</a>
          </ng-container>

          <!-- DOCENTE NAVBAR -->
          <ng-container *ngIf="getRol() === 'DOCENTE'">
            <div style="padding: 0.5rem 1rem; font-size: 0.75rem; font-weight: bold; color: #999; text-transform: uppercase;">Módulo Docente</div>
            <a routerLink="/turnos" routerLinkActive="active">Mis Turnos</a>
            <a routerLink="/incidentes" routerLinkActive="active">Reportar Incidente</a>
            <a routerLink="/notificaciones" routerLinkActive="active">Mis Notificaciones</a>
          </ng-container>
        </nav>
      </aside>

      <main class="content">
        <router-outlet />
      </main>
    </div>
  `
})
export class AppComponent implements OnInit {
  usuarios: Usuario[] = [];
  usuarioSeleccionadoId: number | null = null;
  
  constructor(private api: ApiService, private router: Router) {}

  ngOnInit() {
    this.api.usuarios().subscribe(data => this.usuarios = data);
    const stored = localStorage.getItem('usuarioActivo');
    if (stored) {
      this.usuarioSeleccionadoId = Number(stored);
    }
  }

  cambiarUsuario() {
    if (this.usuarioSeleccionadoId) {
      localStorage.setItem('usuarioActivo', this.usuarioSeleccionadoId.toString());
    } else {
      localStorage.removeItem('usuarioActivo');
    }

    const rol = this.getRol()
    const rutaDestino = (rol === 'DOCENTE') ? '/turnos' : '/dashboard';
    this.router.navigate([rutaDestino]).then(() => window.location.reload());
  }

  getRol(): string {
    if (!this.usuarioSeleccionadoId) return 'ADMINISTRADOR';
    const u = this.usuarios.find(x => x.id === this.usuarioSeleccionadoId);
    return u ? u.rol : 'ADMINISTRADOR';
  }

  isAdmin(): boolean {
    const rol = this.getRol();
    return rol === 'ADMINISTRADOR' || rol === 'COORDINADOR';
  }
}

