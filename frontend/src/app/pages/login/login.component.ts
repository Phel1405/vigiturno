import { AfterViewInit, Component, ElementRef, NgZone, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../core/api.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrls: ['./login.component.css'],
  templateUrl: './login.component.html',
})
export class LoginComponent implements OnInit{

  email = '';
  password = '';
  loading = false;
  errorMsg = '';

  constructor(private api: ApiService, private router: Router, private zone: NgZone) {}

  ngOnInit() {
    const token = localStorage.getItem('token');
    if (token) {
      const rol = localStorage.getItem('usuarioRol');
      const route = rol === 'DOCENTE' ? '/turnos' : '/dashboard';
      this.router.navigate([route]);
    }
  }

  quickLogin(correo: string, pass: string) {
    this.email = correo;
    this.password = pass;
    this.onSubmit();
  }

  onSubmit() {
    if (!this.email || !this.password) return;
    this.loading = true;
    this.errorMsg = '';

    this.api.login(this.email, this.password).subscribe({
      next: (res) => {
        this.saveSessionAndRedirect(res);
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 401) {
          this.errorMsg = 'Correo o contraseña incorrectos.';
        } else {
          this.errorMsg = 'Error de conexión con el servidor.';
        }
      }
    });
  }

  private saveSessionAndRedirect(res: any) {
    localStorage.setItem('token', res.token);
    localStorage.setItem('usuarioId', res.id.toString());
    localStorage.setItem('usuarioEmail', res.correo);
    localStorage.setItem('usuarioNombre', res.nombreCompleto);
    localStorage.setItem('usuarioRol', res.rol);
    localStorage.setItem('usuarioActivo', res.id.toString());

    const route = res.rol === 'DOCENTE' ? '/turnos' : '/dashboard';
    window.location.href = route;
  }
}
