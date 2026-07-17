/**
 * Converte strings de moeda no formato usado nos .feature (ex.: "R$ 1.234,56")
 * para number (ex.: 1234.56). Também aceita o texto puro extraído do DOM,
 * que já vem formatado pelo CurrencyPipe do Angular (ex.: "R$1.234,56").
 */
export function parseMoeda(valor: string): number {
  const limpo = valor
    .replace(/[^\d,.-]/g, '')
    .replace(/\.(?=\d{3}(\D|$))/g, '')
    .replace(',', '.');
  return parseFloat(limpo);
}
