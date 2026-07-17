import { HttpClient, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, switchMap, throwError } from 'rxjs';
import { SessionService } from '../services/session.service';
import { AuthResponse } from '../../models/auth.model';
import { environment } from '../../../environments/environment';

// Estado compartilhado entre chamadas do interceptor (o mesmo módulo é
// reaproveitado por todas as requisições), garantindo que múltiplas 401s
// simultâneas disparem apenas UMA chamada de refresh, não uma por request.
let refreshEmAndamento$: Observable<AuthResponse> | null = null;

/**
 * Captura 401 (access token expirado/inválido), tenta renovar via
 * `/auth/refresh-token` e reenvia a requisição original com o novo token.
 * Se o refresh também falhar, encerra a sessão e manda para /login.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionService);
  const http = inject(HttpClient);
  const router = inject(Router);

  const isChamadaApi = req.url.startsWith(environment.apiUrl);
  const isRotaAuth = req.url.includes('/auth/');

  return next(req).pipe(
    catchError((erro: unknown) => {
      if (
        erro instanceof HttpErrorResponse &&
        erro.status === 401 &&
        isChamadaApi &&
        !isRotaAuth &&
        session.getRefreshToken()
      ) {
        if (!refreshEmAndamento$) {
          refreshEmAndamento$ = http
            .post<AuthResponse>(`${environment.apiUrl}/auth/refresh-token`, {
              refreshToken: session.getRefreshToken()
            })
            .pipe(
              switchMap((auth) => {
                session.salvarTokens(auth);
                refreshEmAndamento$ = null;
                return [auth];
              }),
              catchError((refreshErro) => {
                refreshEmAndamento$ = null;
                session.encerrarSessao();
                router.navigate(['/login']);
                return throwError(() => refreshErro);
              })
            );
        }

        return refreshEmAndamento$.pipe(
          switchMap((auth) =>
            next(
              req.clone({
                setHeaders: { Authorization: `Bearer ${auth.accessToken}` }
              })
            )
          )
        );
      }

      return throwError(() => erro);
    })
  );
};
