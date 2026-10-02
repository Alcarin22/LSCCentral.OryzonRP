import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { SessionService } from '../services/session.service';

export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);
  const session = inject(SessionService);
  const empleado = session.getEmpleado();

  if (!session.isLogged()) {
    session.logout();
    return router.createUrlTree(['/login']);
  }

  const nombreRango = (empleado?.rango?.nombre ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .trim()
    .toLowerCase();

  const esEncargado = nombreRango === 'encargado';
  const tieneNivelAdministracion = (empleado?.rango?.nivel ?? 0) >= 4;

  if (esEncargado || tieneNivelAdministracion) {
    return true;
  }

  return router.createUrlTree(['/dashboard']);
};
