export type Perfil = 'ADMIN' | 'CLIENTE';

export interface Usuario {
  id: string;
  nome: string;
  email: string;
  perfil: Perfil;
  dataCadastro: string;
}

export interface UsuarioRequest {
  nome: string;
  email: string;
  /** Opcional em atualizações — se omitida, o backend mantém a senha atual. */
  senha?: string;
  perfil: Perfil;
}
