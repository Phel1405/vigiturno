import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { Usuario } from '../../core/models';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="page-header" style="text-align: center; display: block; margin-bottom: 2rem;">
      <h1>Bienvenido a VigiTurno</h1>
      <p>Selecciona tu rol para ingresar al sistema.</p>
    </section>

    <div class="grid cols-3" *ngIf="paso === 'ROL'">
      <article class="card" style="text-align: center; cursor: pointer; transition: transform 0.2s;" (click)="seleccionarRol('DOCENTE')" onmouseover="this.style.transform='scale(1.05)'" onmouseout="this.style.transform='scale(1)'">
        <h2>Entrar como Docente</h2>
        <p>Inicia tus turnos, registra limpieza y reporta incidentes.</p>
      </article>

      <article class="card" style="text-align: center; cursor: pointer; transition: transform 0.2s;" (click)="seleccionarRol('COORDINADOR')" onmouseover="this.style.transform='scale(1.05)'" onmouseout="this.style.transform='scale(1)'">
        <h2>Entrar como Coordinador</h2>
        <p>Revisa el mapa de zonas y gestiona reasignaciones e incidentes.</p>
      </article>

      <article class="card" style="text-align: center; cursor: pointer; transition: transform 0.2s;" (click)="entrarAdmin()" onmouseover="this.style.transform='scale(1.05)'" onmouseout="this.style.transform='scale(1)'">
        <h2>Entrar como Administrador</h2>
        <p>Gestión global de usuarios, zonas, turnos y dashboard central.</p>
      </article>
    </div>

    <section class="card" *ngIf="paso === 'USUARIO'" style="max-width: 500px; margin: 0 auto;">
      <h2>Selecciona tu usuario ({{ rolSeleccionado }})</h2>
      <div style="display: flex; flex-direction: column; gap: 0.5rem; margin-top: 1rem;">
        <button class="ghost" *ngFor="let u of usuariosFiltrados" (click)="entrar(u.id)" style="text-align: left; justify-content: flex-start;">
          {{ u.nombreCompleto }}
        </button>
      </div>
      <button class="secondary" style="margin-top: 1rem; width: 100%;" (click)="paso = 'ROL'">Volver</button>
    </section>
  `
})
export class HomeComponent implements OnInit {
  paso: 'ROL' | 'USUARIO' = 'ROL';
  rolSeleccionado: string = '';
  usuarios: Usuario[] = [];
  usuariosFiltrados: Usuario[] = [];

  constructor(private api: ApiService, private router: Router) {}

  ngOnInit() {
    this.api.usuarios().subscribe(data => this.usuarios = data);
    const stored = localStorage.getItem('usuarioActivo');
    if (stored) {
      const id = Number(stored);
      const u = this.usuarios.find(x => x.id === id);
      const ruta = u?.rol === 'DOCENTE' ? '/turnos' : '/dashboard';
      this.router.navigate([ruta]);
    }
  }

  seleccionarRol(rol: string) {
    this.rolSeleccionado = rol;
    this.usuariosFiltrados = this.usuarios.filter(u => u.rol === rol);
    this.paso = 'USUARIO';
  }

  entrar(id?: number) {
    if (!id) return;
    localStorage.setItem('usuarioActivo', id.toString());

    const u = this.usuarios.find(x => x.id === id);
    const ruta = u?.rol === 'DOCENTE' ? '/turnos' : '/dashboard';
    window.location.href = ruta;
  }

  entrarAdmin() {
    const admin = this.usuarios.find(u => u.rol === 'ADMINISTRADOR');
    if (admin && admin.id) {
      this.entrar(admin.id);
    } else {
      alert('No hay administradores registrados.');
    }
  }
}
