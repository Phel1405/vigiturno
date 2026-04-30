import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { UsuariosComponent } from './pages/usuarios/usuarios.component';
import { ZonasComponent } from './pages/zonas/zonas.component';
import { TurnosComponent } from './pages/turnos/turnos.component';
import { IncidentesComponent } from './pages/incidentes/incidentes.component';
import { ReasignacionesComponent } from './pages/reasignaciones/reasignaciones.component';
import { NotificacionesComponent } from './pages/notificaciones/notificaciones.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'usuarios', component: UsuariosComponent },
  { path: 'zonas', component: ZonasComponent },
  { path: 'turnos', component: TurnosComponent },
  { path: 'incidentes', component: IncidentesComponent },
  { path: 'reasignaciones', component: ReasignacionesComponent },
  { path: 'notificaciones', component: NotificacionesComponent },
  { path: '**', redirectTo: '' }
];
