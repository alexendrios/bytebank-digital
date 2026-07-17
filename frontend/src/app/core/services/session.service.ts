import { Injectable, computed, signal } from '@angular/core';
import { AuthResponse, SessaoUsuario } from '../../models/auth.model';
import { Perfil } from '../../models/usuario.model';

const ACCESS_TOKEN_KEY = 'bb_access_token';
const REFRESH_TOKEN_KEY = 'bb_refresh_token';
const SESSAO_KEY = 'bb_sessao';

interface JwtPayload {
  sub: string;
  type: 'access' | 'refresh';
  iat: number;
  exp: number;
}

/**
 * Fonte única de verdade da sessão no frontend.
 *
 * Nota de arquitetura importante: o JWT emitido pelo backend carrega apenas
 * `sub` (e-mail) e `type` (access/refresh) — não existe claim de perfil, e
 * não existe endpoint `/auth/me` ou `/usuarios/me` para o próprio usuário
 * consultar seus dados (GET /usuarios é ADMIN-only). Por isso o perfil é
 * inferido, uma vez por login, pelo `AuthService.bootstrapSessao()`, e
 * guardado aqui — não pelo JwtService do backend. É um trade-off consciente,
 * equivalente em espírito ao documentado em docs/09-seguranca.md.
 */
@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly _sessao = signal<SessaoUsuario | null>(this.carregarSessaoPersistida());

  readonly sessao = computed(() => this._sessao());
  readonly autenticado = computed(() => this._sessao() !== null);
  readonly perfil = computed<Perfil | null>(() => this._sessao()?.perfil ?? null);
  readonly isAdmin = computed(() => this._sessao()?.perfil === 'ADMIN');

  salvarTokens(auth: AuthResponse): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, auth.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, auth.refreshToken);
  }

  definirSessao(sessao: SessaoUsuario): void {
    this._sessao.set(sessao);
    localStorage.setItem(SESSAO_KEY, JSON.stringify(sessao));
  }

  atualizarDadosUsuario(parcial: Partial<SessaoUsuario>): void {
    const atual = this._sessao();
    if (!atual) return;
    const nova = { ...atual, ...parcial };
    this._sessao.set(nova);
    localStorage.setItem(SESSAO_KEY, JSON.stringify(nova));
  }

  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  /** E-mail extraído do access token atual (claim `sub`), sem round-trip à API. */
  emailDoToken(): string | null {
    const token = this.getAccessToken();
    if (!token) return null;
    const payload = this.decodificarPayload(token);
    return payload?.sub ?? null;
  }

  encerrarSessao(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(SESSAO_KEY);
    this._sessao.set(null);
  }

  private carregarSessaoPersistida(): SessaoUsuario | null {
    const raw = localStorage.getItem(SESSAO_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as SessaoUsuario;
    } catch {
      return null;
    }
  }

  private decodificarPayload(token: string): JwtPayload | null {
    try {
      const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      const json = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
          .join('')
      );
      return JSON.parse(json) as JwtPayload;
    } catch {
      return null;
    }
  }
}
