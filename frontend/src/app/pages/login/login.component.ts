import { AfterViewInit, Component, ElementRef, NgZone, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { environment } from '../../../environments/environment';

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (config: GoogleIdentityConfig) => void;
          renderButton: (element: HTMLElement, options: GoogleButtonOptions) => void;
        };
      };
    };
  }
}

interface GoogleIdentityConfig {
  client_id: string;
  callback: (response: GoogleCredentialResponse) => void;
  ux_mode?: 'popup' | 'redirect';
}

interface GoogleCredentialResponse {
  credential?: string;
}

interface GoogleButtonOptions {
  theme: 'outline' | 'filled_blue' | 'filled_black';
  size: 'large' | 'medium' | 'small';
  type: 'standard' | 'icon';
  shape: 'rectangular' | 'pill' | 'circle' | 'square';
  text: 'signin_with' | 'signup_with' | 'continue_with' | 'signin';
  width?: number;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrls: ['./login.component.css'],
  templateUrl: './login.component.html',
})
export class LoginComponent implements OnInit, AfterViewInit {
  @ViewChild('googleButton', { static: false }) googleButton?: ElementRef<HTMLDivElement>;

  email = '';
  password = '';
  loading = false;
  googleLoading = false;
  errorMsg = '';

  constructor(private api: ApiService, private router: Router, private zone: NgZone) {}

  ngOnInit() {
    const token = localStorage.getItem('token');
    const rol = localStorage.getItem('usuarioRol');
    if (token && rol) {
      const route = rol === 'DOCENTE' ? '/turnos' : '/dashboard';
      this.router.navigate([route]);
    }
  }

  ngAfterViewInit() {
    this.loadGoogleIdentityServices()
      .then(() => this.renderGoogleButton())
      .catch(() => {
        this.errorMsg = 'Configura googleClientId en environment.ts para habilitar Google Login.';
      });
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

  onGoogleCredential(response: GoogleCredentialResponse) {
    if (!response.credential) {
      this.zone.run(() => {
        this.errorMsg = 'Google no devolvió un token válido. Intenta nuevamente.';
      });
      return;
    }

    this.zone.run(() => {
      this.googleLoading = true;
      this.loading = true;
      this.errorMsg = '';
    });

    this.api.googleLogin(response.credential).subscribe({
      next: (res) => {
        this.zone.run(() => this.saveSessionAndRedirect(res));
      },
      error: (err) => {
        this.zone.run(() => {
          this.googleLoading = false;
          this.loading = false;
          if (err.status === 401) {
            this.errorMsg = typeof err.error === 'string'
              ? err.error
              : 'Google no pudo validar esta cuenta.';
          } else if (err.status === 500) {
            this.errorMsg = 'El backend no tiene configurado GOOGLE_CLIENT_ID.';
          } else {
            this.errorMsg = 'Error de comunicación al ingresar con Google.';
          }
        });
      }
    });
  }

  private loadGoogleIdentityServices(): Promise<void> {
    if (!environment.googleClientId || environment.googleClientId.includes('TU_CLIENT_ID')) {
      return Promise.reject();
    }

    if (window.google?.accounts?.id) {
      return Promise.resolve();
    }

    return new Promise((resolve, reject) => {
      const existingScript = document.querySelector<HTMLScriptElement>('script[src="https://accounts.google.com/gsi/client"]');
      if (existingScript) {
        existingScript.addEventListener('load', () => resolve());
        existingScript.addEventListener('error', () => reject());
        return;
      }

      const script = document.createElement('script');
      script.src = 'https://accounts.google.com/gsi/client';
      script.async = true;
      script.defer = true;
      script.onload = () => resolve();
      script.onerror = () => reject();
      document.head.appendChild(script);
    });
  }

  private renderGoogleButton() {
    if (!this.googleButton?.nativeElement || !window.google?.accounts?.id) {
      return;
    }

    window.google.accounts.id.initialize({
      client_id: environment.googleClientId,
      callback: (response) => this.onGoogleCredential(response),
      ux_mode: 'popup'
    });

    this.googleButton.nativeElement.innerHTML = '';
    window.google.accounts.id.renderButton(this.googleButton.nativeElement, {
      theme: 'outline',
      size: 'large',
      type: 'standard',
      shape: 'rectangular',
      text: 'continue_with',
      width: 370
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
