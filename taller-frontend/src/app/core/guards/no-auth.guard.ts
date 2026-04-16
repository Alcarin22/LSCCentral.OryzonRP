import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from '../services/session.service';

export const noAuthGuard: CanActivateFn = () => {
  const router = inject(Router);
  const session = inject(SessionService);

  if (session.isLogged()) {
    // Si ya está logueado → lo mandas al dashboard
    router.navigate(['/dashboard']);
    return false;
  }

  return true;
};