import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessionService } from '../services/session.service';

/**
 * Protege rotas privadas. A checagem aqui é só para UX (evitar telas
 * "piscando" antes de um 401) — a API já protege tudo de qualquer forma
 * no backend, então essa é uma segunda linha de defesa, não a principal.
 */
export const authGuard: CanActivateFn = () => {
  const session = inject(SessionService);
  const router = inject(Router);

  if (session.autenticado()) {
    return true;
  }

  return router.createUrlTree(['/login']);
};
