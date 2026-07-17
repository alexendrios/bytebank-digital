import { HttpErrorResponse } from '@angular/common/http';
import { ApiError } from '../../models/api-error.model';

/**
 * Extrai uma mensagem amigável de um erro HTTP, aproveitando o formato
 * estruturado do ApiError produzido pelo GlobalExceptionHandler do backend
 * (ver docs/10-api.md). Quando há `details` (erros de validação por campo),
 * eles são concatenados para dar contexto extra ao usuário.
 */
export function extrairMensagemErro(erro: unknown, fallback = 'Ocorreu um erro inesperado. Tente novamente.'): string {
  if (erro instanceof HttpErrorResponse) {
    const apiError = erro.error as ApiError | undefined;
    if (apiError?.message) {
      if (apiError.details?.length) {
        return `${apiError.message} (${apiError.details.join('; ')})`;
      }
      return apiError.message;
    }
    if (erro.status === 0) {
      return 'Não foi possível conectar à API. Verifique sua conexão.';
    }
  }
  return fallback;
}

/** Mapeia `details` no formato "campo: mensagem" para um dicionário campo → mensagem. */
export function mapearErrosPorCampo(erro: unknown): Record<string, string> {
  const resultado: Record<string, string> = {};
  if (erro instanceof HttpErrorResponse) {
    const apiError = erro.error as ApiError | undefined;
    for (const detalhe of apiError?.details ?? []) {
      const [campo, ...resto] = detalhe.split(':');
      if (campo && resto.length) {
        resultado[campo.trim()] = resto.join(':').trim();
      }
    }
  }
  return resultado;
}
