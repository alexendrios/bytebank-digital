export interface Conta {
  id: string;
  numero: string;
  agencia: string;
  saldo: number;
  usuarioId: string;
  usuarioNome: string;
  dataCriacao: string;
}

export interface ContaRequest {
  agencia: string;
  usuarioId: string;
}

export interface ValorOperacaoRequest {
  valor: number;
}
