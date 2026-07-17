import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Conta, ContaRequest, ValorOperacaoRequest } from '../models/conta.model';
import { Movimentacao, Page } from '../models/movimentacao.model';

@Injectable({ providedIn: 'root' })
export class ContaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/contas`;

  /** ADMIN vê todas as contas; CLIENTE vê só as próprias (filtro é do backend). */
  listar(): Observable<Conta[]> {
    return this.http.get<Conta[]>(this.baseUrl);
  }

  buscarPorId(id: string): Observable<Conta> {
    return this.http.get<Conta>(`${this.baseUrl}/${id}`);
  }

  criar(request: ContaRequest): Observable<Conta> {
    return this.http.post<Conta>(this.baseUrl, request);
  }

  atualizar(id: string, request: ContaRequest): Observable<Conta> {
    return this.http.put<Conta>(`${this.baseUrl}/${id}`, request);
  }

  remover(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  depositar(id: string, valor: number): Observable<Conta> {
    const body: ValorOperacaoRequest = { valor };
    return this.http.post<Conta>(`${this.baseUrl}/${id}/deposito`, body);
  }

  sacar(id: string, valor: number): Observable<Conta> {
    const body: ValorOperacaoRequest = { valor };
    return this.http.post<Conta>(`${this.baseUrl}/${id}/saque`, body);
  }

  extrato(id: string, page = 0, size = 20): Observable<Page<Movimentacao>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<Movimentacao>>(`${this.baseUrl}/${id}/extrato`, { params });
  }
}
