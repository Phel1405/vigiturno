import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { Usuario } from '../../core/models';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.component.html'
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
