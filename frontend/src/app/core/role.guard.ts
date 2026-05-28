import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

export const roleGuard: CanActivateFn = (route) => {
  const router = inject(Router);
  const expectedRoles = route.data['roles'] as string[];
  const userRol = localStorage.getItem('usuarioRol');

  if (userRol && expectedRoles.includes(userRol)) {
    return true;
  }

  // Redirigir a una ruta por defecto si no tiene permiso para esta sección
  if (userRol === 'DOCENTE') {
    router.navigate(['/turnos']);
  } else {
    router.navigate(['/dashboard']);
  }
  return false;
};
