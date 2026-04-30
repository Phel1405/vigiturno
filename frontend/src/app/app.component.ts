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
            <small>App para vigilancia docente</small>
          </div>
        </div>

        <div style="padding: 1rem;" *ngIf="usuarioSeleccionadoId">
          <label style="font-size: 0.8rem; color: #666;">Usuario actual</label>
          <div style="font-size: 0.9rem; margin-top: 0.25rem;">
            <strong>{{ getRol() }}</strong>
          </div>
          <button class="ghost" style="margin-top: 0.5rem; width: 100%; color: #e74c3c; border: 1px solid #e74c3c;" (click)="salir()">Salir</button>
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
            <div style="padding: 0.5rem 1rem; font-size: 0.75rem; font-weight: bold; color: #999; text-transform: uppercase;">Selecciona un rol</div>
            <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}">Home</a>
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

  salir() {
    localStorage.removeItem('usuarioActivo');
    this.usuarioSeleccionadoId = null;
    this.router.navigate(['/']).then(() => window.location.reload());
  }

  getRol(): string {
    if (!this.usuarioSeleccionadoId) return '';
    const u = this.usuarios.find(x => x.id === this.usuarioSeleccionadoId);
    return u ? u.rol : 'ADMINISTRADOR';
  }

  isAdmin(): boolean {
    const rol = this.getRol();
    return rol === 'ADMINISTRADOR' || rol === 'COORDINADOR';
  }
}

