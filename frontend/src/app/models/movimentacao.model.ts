export type TipoMovimentacao = 'DEPOSITO' | 'SAQUE' | 'PIX';

export interface Movimentacao {
  id: string;
  tipo: TipoMovimentacao;
  valor: number;
  saldoAnterior: number;
  saldoAtual: number;
  descricao: string;
  data: string;
}

/** Espelha org.springframework.data.domain.Page<T> serializado pelo backend. */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}
