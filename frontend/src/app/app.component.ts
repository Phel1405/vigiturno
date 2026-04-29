import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="app-shell">
      <aside class="sidebar">
        <div class="brand">
          <span class="brand-mark">VT</span>
          <div>
            <strong>VigiTurno</strong>
            <small>SPA Angular + REST</small>
          </div>
        </div>

        <nav>
          <a routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
          <a routerLink="/turnos" routerLinkActive="active">Turnos</a>
          <a routerLink="/incidentes" routerLinkActive="active">Incidentes</a>
          <a routerLink="/reasignaciones" routerLinkActive="active">Reasignaciones</a>
          <a routerLink="/usuarios" routerLinkActive="active">Usuarios</a>
          <a routerLink="/zonas" routerLinkActive="active">Zonas</a>
          <a routerLink="/notificaciones" routerLinkActive="active">Notificaciones</a>
        </nav>
      </aside>

      <main class="content">
        <router-outlet />
      </main>
    </div>
  `
})
export class AppComponent {}
