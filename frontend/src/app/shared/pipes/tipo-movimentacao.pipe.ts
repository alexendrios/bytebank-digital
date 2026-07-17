import { Pipe, PipeTransform } from '@angular/core';
import { TipoMovimentacao } from '../../models/movimentacao.model';

const LABELS: Record<TipoMovimentacao, string> = {
  DEPOSITO: 'Depósito',
  SAQUE: 'Saque',
  PIX: 'Pix'
};

@Pipe({ name: 'tipoMovimentacao', standalone: true })
export class TipoMovimentacaoPipe implements PipeTransform {
  transform(tipo: TipoMovimentacao): string {
    return LABELS[tipo] ?? tipo;
  }
}
