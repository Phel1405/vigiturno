import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

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
            <small>App para vigilancia docente</small>
          </div>
        </div>

        <div style="padding: 1rem; border-top: 1px solid rgba(255,255,255,0.08); border-bottom: 1px solid rgba(255,255,255,0.08); margin: 10px 0;" *ngIf="usuarioSeleccionadoId">
          <label style="font-size: 0.75rem; color: #94a3b8; text-transform: uppercase; font-weight: 600;">Usuario actual</label>
          <div style="font-size: 0.9rem; margin-top: 0.25rem; font-weight: bold; color: #ffffff;">
            {{ usuarioNombre }}
          </div>
          <div style="font-size: 0.75rem; color: #3b82f6; font-weight: 600; margin-top: 2px;">
            {{ getRol() }}
          </div>
          <button class="ghost" style="margin-top: 0.75rem; width: 100%; color: #ef4444; border: 1px solid #ef4444; background: transparent;" (click)="salir()">Cerrar Sesión</button>
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
            <a routerLink="/turnos" routerLinkActive="active">Turnos</a>
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
          
          <!-- NO NAVBAR -->
          <ng-container *ngIf="!usuarioSeleccionadoId">
            <div style="padding: 0.5rem 1rem; font-size: 0.75rem; font-weight: bold; color: #999; text-transform: uppercase;">Inicia Sesión</div>
            <a routerLink="/login" routerLinkActive="active">Iniciar Sesión</a>
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
  usuarioNombre: string | null = null;
  usuarioRol: string | null = null;
  usuarioSeleccionadoId: number | null = null;
  
  constructor(private router: Router) {}

  ngOnInit() {
    const token = localStorage.getItem('token');
    if (token) {
      this.usuarioNombre = localStorage.getItem('usuarioNombre');
      this.usuarioRol = localStorage.getItem('usuarioRol');
      this.usuarioSeleccionadoId = Number(localStorage.getItem('usuarioId'));
    }
  }

  salir() {
    localStorage.clear();
    this.usuarioNombre = null;
    this.usuarioRol = null;
    this.usuarioSeleccionadoId = null;
    this.router.navigate(['/login']);
  }

  getRol(): string {
    return this.usuarioRol || '';
  }

  isAdmin(): boolean {
    const rol = this.getRol();
    return rol === 'ADMINISTRADOR' || rol === 'COORDINADOR';
  }
}
