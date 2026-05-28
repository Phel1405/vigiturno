import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { UsuariosComponent } from './pages/usuarios/usuarios.component';
import { ZonasComponent } from './pages/zonas/zonas.component';
import { TurnosComponent } from './pages/turnos/turnos.component';
import { IncidentesComponent } from './pages/incidentes/incidentes.component';
import { ReasignacionesComponent } from './pages/reasignaciones/reasignaciones.component';
import { NotificacionesComponent } from './pages/notificaciones/notificaciones.component';
import { authGuard } from './core/auth.guard';
import { roleGuard } from './core/role.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { 
    path: 'dashboard', 
    component: DashboardComponent, 
    canActivate: [authGuard, roleGuard], 
    data: { roles: ['ADMINISTRADOR', 'COORDINADOR'] } 
  },
  { 
    path: 'usuarios', 
    component: UsuariosComponent, 
    canActivate: [authGuard, roleGuard], 
    data: { roles: ['ADMINISTRADOR'] } 
  },
  { 
    path: 'zonas', 
    component: ZonasComponent, 
    canActivate: [authGuard, roleGuard], 
    data: { roles: ['ADMINISTRADOR'] } 
  },
  { 
    path: 'turnos', 
    component: TurnosComponent, 
    canActivate: [authGuard] 
  },
  { 
    path: 'incidentes', 
    component: IncidentesComponent, 
    canActivate: [authGuard] 
  },
  { 
    path: 'reasignaciones', 
    component: ReasignacionesComponent, 
    canActivate: [authGuard] 
  },
  { 
    path: 'notificaciones', 
    component: NotificacionesComponent, 
    canActivate: [authGuard] 
  },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];
