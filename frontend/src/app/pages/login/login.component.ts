import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../core/api.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styles: [`
    .login-container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #0f172a 0%, #1e1b4b 50%, #311042 100%);
      font-family: 'Outfit', 'Inter', sans-serif;
      padding: 20px;
      position: relative;
      overflow: hidden;
    }

    .bg-light-1,
    .bg-light-2 {
      position: absolute;
      border-radius: 999px;
      pointer-events: none;
      z-index: 1;
    }

    .bg-light-1 {
      width: 500px;
      height: 500px;
      background: radial-gradient(circle, rgba(37, 99, 235, 0.15) 0%, rgba(0,0,0,0) 70%);
      top: -100px;
      left: -100px;
    }

    .bg-light-2 {
      width: 600px;
      height: 600px;
      background: radial-gradient(circle, rgba(168, 85, 247, 0.12) 0%, rgba(0,0,0,0) 70%);
      bottom: -150px;
      right: -100px;
    }

    .login-card {
      position: relative;
      z-index: 10;
      background: rgba(15, 23, 42, 0.72);
      backdrop-filter: blur(16px);
      -webkit-backdrop-filter: blur(16px);
      border: 1px solid rgba(255, 255, 255, 0.08);
      border-radius: 24px;
      padding: 40px;
      width: 100%;
      max-width: 450px;
      box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
    }

    .brand-header {
      text-align: center;
      margin-bottom: 30px;
    }

    .logo-container {
      width: 60px;
      height: 60px;
      border-radius: 18px;
      background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 16px;
      font-size: 24px;
      font-weight: 800;
      color: #ffffff;
      box-shadow: 0 10px 20px rgba(37, 99, 235, 0.3);
    }

    .brand-name {
      font-size: 28px;
      font-weight: 700;
      color: #ffffff;
      margin: 0;
    }

    .brand-tagline {
      font-size: 14px;
      color: #94a3b8;
      margin: 6px 0 0 0;
    }

    .form-group {
      margin-bottom: 20px;
    }

    .form-label {
      display: block;
      font-size: 13px;
      font-weight: 600;
      color: #cbd5e1;
      margin-bottom: 8px;
    }

    .input-field {
      width: 100%;
      box-sizing: border-box;
      background: rgba(15, 23, 42, 0.8);
      border: 1px solid rgba(255, 255, 255, 0.12);
      border-radius: 12px;
      padding: 12px 16px;
      color: #ffffff;
      font-size: 15px;
      transition: all 0.25s ease;
    }

    .input-field:focus {
      outline: none;
      border-color: #3b82f6;
      background: rgba(15, 23, 42, 0.95);
      box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.15);
    }

    .error-banner {
      background: rgba(239, 68, 68, 0.1);
      border: 1px solid rgba(239, 68, 68, 0.2);
      color: #fca5a5;
      padding: 12px 16px;
      border-radius: 12px;
      font-size: 14px;
      margin-bottom: 20px;
      text-align: center;
    }

    .login-btn {
      width: 100%;
      background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
      color: #ffffff;
      border: none;
      border-radius: 12px;
      padding: 14px;
      font-size: 16px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.25s ease;
      box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
    }

    .login-btn:hover:not(:disabled) {
      transform: translateY(-1px);
      box-shadow: 0 6px 20px rgba(37, 99, 235, 0.35);
    }

    .login-btn:disabled {
      opacity: 0.65;
      cursor: not-allowed;
    }

    .demo-divider {
      display: flex;
      align-items: center;
      margin: 25px 0;
      color: #64748b;
      font-size: 12px;
      text-transform: uppercase;
      letter-spacing: 1px;
    }

    .demo-divider::before,
    .demo-divider::after {
      content: '';
      flex: 1;
      height: 1px;
      background: rgba(255, 255, 255, 0.08);
    }

    .demo-divider span {
      padding: 0 10px;
    }

    .demo-grid {
      display: grid;
      grid-template-columns: 1fr;
      gap: 10px;
    }

    .demo-btn {
      background: rgba(255, 255, 255, 0.03);
      border: 1px solid rgba(255, 255, 255, 0.08);
      border-radius: 12px;
      padding: 10px 14px;
      color: #cbd5e1;
      font-size: 13px;
      cursor: pointer;
      transition: all 0.25s ease;
      display: flex;
      justify-content: space-between;
      align-items: center;
      text-align: left;
      gap: 12px;
    }

    .demo-btn:hover {
      background: rgba(255, 255, 255, 0.08);
      border-color: rgba(255, 255, 255, 0.15);
      color: #ffffff;
      transform: translateX(2px);
    }

    .role-badge {
      font-size: 10px;
      font-weight: 700;
      text-transform: uppercase;
      padding: 2px 6px;
      border-radius: 6px;
      letter-spacing: 0.5px;
      white-space: nowrap;
    }

    .demo-btn.admin .role-badge { background: rgba(239, 68, 68, 0.15); color: #fca7a7; }
    .demo-btn.coord .role-badge { background: rgba(234, 179, 8, 0.15); color: #fef08a; }
    .demo-btn.docente .role-badge { background: rgba(34, 197, 94, 0.15); color: #bbf7d0; }

    .signup-link {
      text-align: center;
      margin-top: 25px;
      font-size: 14px;
      color: #94a3b8;
    }

    .signup-link a {
      color: #3b82f6;
      text-decoration: none;
      font-weight: 600;
    }

    .signup-link a:hover {
      color: #60a5fa;
      text-decoration: underline;
    }
  `],
  template: `
    <div class="login-container">
      <div class="bg-light-1"></div>
      <div class="bg-light-2"></div>

      <div class="login-card">
        <div class="brand-header">
          <div class="logo-container">VT</div>
          <h1 class="brand-name">VigiTurno</h1>
          <p class="brand-tagline">Sistema de Gestión de Supervisión Docente</p>
        </div>

        <div class="error-banner" *ngIf="errorMsg">
          {{ errorMsg }}
        </div>

        <form (ngSubmit)="onSubmit()" #loginForm="ngForm">
          <div class="form-group">
            <label class="form-label" for="email">Correo Electrónico</label>
            <input
              type="email"
              id="email"
              name="email"
              class="input-field"
              placeholder="ejemplo@vigiturno.edu.co"
              [(ngModel)]="email"
              required
            />
          </div>

          <div class="form-group">
            <label class="form-label" for="password">Contraseña</label>
            <input
              type="password"
              id="password"
              name="password"
              class="input-field"
              placeholder="••••••••"
              [(ngModel)]="password"
              required
            />
          </div>

          <button type="submit" class="login-btn" [disabled]="loginForm.invalid || loading">
            <span *ngIf="!loading">Ingresar al Sistema</span>
            <span *ngIf="loading">Cargando...</span>
          </button>
        </form>

        <div class="demo-divider">
          <span>Acceso Rápido (Demo)</span>
        </div>

        <div class="demo-grid">
          <button class="demo-btn admin" (click)="quickLogin('admin@vigiturno.edu.co', 'admin123')">
            <div>
              <strong>Laura Martínez</strong>
              <div style="font-size: 11px; color: #64748b; margin-top: 2px;">admin@vigiturno.edu.co</div>
            </div>
            <span class="role-badge">Admin</span>
          </button>

          <button class="demo-btn coord" (click)="quickLogin('coordinador@vigiturno.edu.co', 'coord123')">
            <div>
              <strong>Carlos Gómez</strong>
              <div style="font-size: 11px; color: #64748b; margin-top: 2px;">coordinador@vigiturno.edu.co</div>
            </div>
            <span class="role-badge">Coordinador</span>
          </button>

          <button class="demo-btn docente" (click)="quickLogin('ana.perez@vigiturno.edu.co', 'docente123')">
            <div>
              <strong>Ana Pérez</strong>
              <div style="font-size: 11px; color: #64748b; margin-top: 2px;">ana.perez@vigiturno.edu.co</div>
            </div>
            <span class="role-badge">Docente</span>
          </button>
        </div>

        <div class="signup-link">
          ¿No tienes una cuenta? <a href="/register">Crear cuenta</a>
        </div>
      </div>
    </div>
  `
})
export class LoginComponent implements OnInit {
  email = '';
  password = '';
  loading = false;
  errorMsg = '';

  constructor(private api: ApiService, private router: Router) {}

  ngOnInit() {
    const token = localStorage.getItem('token');
    const rol = localStorage.getItem('usuarioRol');
    if (token && rol) {
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
