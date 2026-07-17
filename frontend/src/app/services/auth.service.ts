import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, map, of, switchMap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest } from '../models/auth.model';
import { Usuario } from '../models/usuario.model';
import { Conta } from '../models/conta.model';
import { SessionService } from '../core/services/session.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionService);
  private readonly baseUrl = `${environment.apiUrl}/auth`;

  login(request: LoginRequest): Observable<void> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/login`, request)
      .pipe(switchMap((auth) => this.finalizarLogin(auth, request.email)));
  }

  registrar(request: RegisterRequest): Observable<void> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/register`, request)
      .pipe(switchMap((auth) => this.finalizarLogin(auth, request.email, request.nome)));
  }

  logout(): void {
    this.session.encerrarSessao();
  }

  private finalizarLogin(auth: AuthResponse, email: string, nomeConhecido?: string): Observable<void> {
    this.session.salvarTokens(auth);
    return this.bootstrapPerfil(email, nomeConhecido);
  }

  /**
   * Não existe claim de perfil no JWT nem endpoint "/me" (ver SessionService
   * para o raciocínio completo). Na ausência de uma fonte de verdade melhor,
   * inferimos o perfil tentando GET /usuarios (ADMIN-only):
   *   - 200 → é ADMIN; localizamos o próprio registro na lista pelo e-mail.
   *   - 403 → é CLIENTE; tentamos então GET /contas para descobrir o próprio
   *     usuarioId/nome a partir de uma conta existente (se houver alguma).
   */
  private bootstrapPerfil(email: string, nomeConhecido?: string): Observable<void> {
    return this.http.get<Usuario[]>(`${environment.apiUrl}/usuarios`).pipe(
      map((usuarios) => {
        const eu = usuarios.find((u) => u.email === email);
        this.session.definirSessao({
          email,
          id: eu?.id ?? null,
          nome: eu?.nome ?? nomeConhecido ?? email,
          perfil: 'ADMIN'
        });
      }),
      catchError((erro: unknown) => {
        if (erro instanceof HttpErrorResponse && erro.status === 403) {
          return this.bootstrapComoCliente(email, nomeConhecido);
        }
        // Qualquer outro erro (rede, 401 por token ainda não propagado, etc.):
        // seguimos como CLIENTE com o que já sabemos, para não travar o login.
        return this.bootstrapComoCliente(email, nomeConhecido);
      })
    );
  }

  private bootstrapComoCliente(email: string, nomeConhecido?: string): Observable<void> {
    return this.http.get<Conta[]>(`${environment.apiUrl}/contas`).pipe(
      map((contas) => {
        const primeira = contas[0];
        this.session.definirSessao({
          email,
          id: primeira?.usuarioId ?? null,
          nome: primeira?.usuarioNome ?? nomeConhecido ?? email,
          perfil: 'CLIENTE'
        });
      }),
      catchError(() => {
        this.session.definirSessao({
          email,
          id: null,
          nome: nomeConhecido ?? email,
          perfil: 'CLIENTE'
        });
        return of(void 0);
      })
    );
  }
}
