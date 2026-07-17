import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { SessionService } from '../services/session.service';
import { environment } from '../../../environments/environment';

/** Anexa `Authorization: Bearer <token>` em requisições para a API do ByteBank. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionService);

  const isChamadaApi = req.url.startsWith(environment.apiUrl);
  const isRotaPublica = req.url.includes('/auth/login') || req.url.includes('/auth/register');

  if (!isChamadaApi || isRotaPublica) {
    return next(req);
  }

  const token = session.getAccessToken();
  if (!token) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    })
  );
};
