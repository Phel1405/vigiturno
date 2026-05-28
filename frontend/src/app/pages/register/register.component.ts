import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../core/api.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrls: ['./register.component.css'],
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  nombreCompleto = '';
  correo = '';
  password = '';
  rol = 'DOCENTE';
  
  loading = false;
  registered = false;
  errorMsg = '';
  successMsg = '';

  constructor(private api: ApiService, private router: Router) {}

  onSubmit() {
    if (!this.nombreCompleto || !this.correo || !this.password || !this.rol) return;
    this.loading = true;
    this.errorMsg = '';
    this.successMsg = '';

    const payload = {
      nombreCompleto: this.nombreCompleto,
      correo: this.correo,
      password: this.password,
      rol: this.rol,
      activo: true
    };

    this.api.registrar(payload).subscribe({
      next: (res) => {
        this.loading = false;
        this.registered = true;
        this.successMsg = '¡Registro completado con éxito! Redirigiéndote al inicio de sesión...';
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2500);
      },
      error: (err) => {
        this.loading = false;
        if (err.error && typeof err.error === 'string') {
          this.errorMsg = err.error;
        } else if (err.error && err.error.message) {
          this.errorMsg = err.error.message;
        } else {
          this.errorMsg = 'Error en el servidor al registrar el usuario.';
        }
      }
    });
  }
}
