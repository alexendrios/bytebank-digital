import { Perfil } from './usuario.model';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface RegisterRequest {
  nome: string;
  email: string;
  senha: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

/** Sessão local derivada do token + verificação de perfil (ver SessionService). */
export interface SessaoUsuario {
  email: string;
  id: string | null;
  nome: string | null;
  perfil: Perfil;
}
