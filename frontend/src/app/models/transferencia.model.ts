export interface Transferencia {
  id: string;
  contaOrigemId: string;
  contaOrigemNumero: string;
  contaDestinoId: string;
  contaDestinoNumero: string;
  valor: number;
  data: string;
}

export interface TransferenciaRequest {
  contaOrigemId: string;
  contaDestinoId: string;
  valor: number;
}
