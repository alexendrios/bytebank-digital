import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Transferencia, TransferenciaRequest } from '../models/transferencia.model';

@Injectable({ providedIn: 'root' })
export class TransferenciaService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/transferencias`;

  transferir(request: TransferenciaRequest): Observable<Transferencia> {
    return this.http.post<Transferencia>(this.baseUrl, request);
  }
}
