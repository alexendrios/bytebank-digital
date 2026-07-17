import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from '../services/session.service';

/** Impede que um usuário já autenticado veja as telas de login/cadastro. */
export const guestGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  const router = inject(Router);

  if (!session.autenticado()) {
    return true;
  }

  return router.createUrlTree(['/app/dashboard']);
};
