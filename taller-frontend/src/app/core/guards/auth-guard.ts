import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { SessionService } from '../services/session.service';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  const session = inject(SessionService);

  const empleado = session.getEmpleado();

  if (empleado && empleado.activo !== false) {
    session.iniciarMonitorSesion();
    return true;
  }

  session.logout();
  router.navigate(['/login']);
  return false;
};