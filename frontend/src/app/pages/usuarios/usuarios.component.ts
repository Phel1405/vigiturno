import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Meta, Usuario } from '../../core/models';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './usuarios.component.html'
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
