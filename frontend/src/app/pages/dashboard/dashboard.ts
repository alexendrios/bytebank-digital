import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { ContaService } from '../../services/conta.service';
import { Conta } from '../../models/conta.model';
import { SessionService } from '../../core/services/session.service';
import { Loading } from '../../shared/components/loading/loading';
import { extrairMensagemErro } from '../../shared/utils/api-error.util';

@Component({
  selector: 'bb-dashboard',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, RouterLink, MatButtonModule, MatIconModule, Loading],
  template: `
    <div class="bb-page">
      <div class="bb-page-header">
        <div>
          <span class="bb-page-header__eyebrow">{{ session.isAdmin() ? 'Todas as contas' : 'Minhas contas' }}</span>
          <h1>Olá, {{ primeiroNome() }}</h1>
        </div>
        @if (!session.isAdmin()) {
          <a mat-flat-button color="primary" routerLink="/app/transferencia">
            <mat-icon>sync_alt</mat-icon>
            Nova transferência
          </a>
        }
      </div>

      @if (carregando()) {
        <bb-loading label="Carregando contas…"></bb-loading>
      } @else if (erro()) {
        <div class="bb-card bb-empty-state">{{ erro() }}</div>
      } @else if (contas().length === 0) {
        <div class="bb-card bb-empty-state">
          <mat-icon style="font-size: 40px; width: 40px; height: 40px; color: var(--bb-text-muted);">account_balance_wallet</mat-icon>
          <p>{{ session.isAdmin() ? 'Nenhuma conta cadastrada ainda.' : 'Você ainda não tem contas. Fale com a administração para abrir a sua.' }}</p>
        </div>
      } @else {
        @if (!session.isAdmin() && saldoTotal() !== null) {
          <div class="bb-card" style="margin-bottom: 20px;">
            <span class="bb-page-header__eyebrow">Saldo total</span>
            <div class="bb-money bb-money--positive" style="font-size: 32px; font-weight: 600;">
              {{ saldoTotal() | currency: 'BRL' }}
            </div>
          </div>
        }

        <div class="bb-grid bb-grid--2">
          @for (conta of contas(); track conta.id) {
            <a [routerLink]="['/app/contas', conta.id]" class="bb-card bb-account-card" [attr.data-cy]="'conta-card-' + conta.numero">
              <div class="bb-account-card__header">
                <span class="bb-page-header__eyebrow">Ag. {{ conta.agencia }} · Conta {{ conta.numero }}</span>
                @if (session.isAdmin()) {
                  <span class="bb-muted" style="font-size: 13px;">{{ conta.usuarioNome }}</span>
                }
              </div>
              <div class="bb-money bb-money--positive" style="font-size: 26px; font-weight: 600; margin: 8px 0;">
                {{ conta.saldo | currency: 'BRL' }}
              </div>
              <span class="bb-muted" style="font-size: 12px;">Aberta em {{ conta.dataCriacao | date: 'dd/MM/yyyy' }}</span>
            </a>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .bb-account-card {
      display: block;
      text-decoration: none;
      color: inherit;
      transition: box-shadow 0.15s ease, transform 0.15s ease;
    }
    .bb-account-card:hover {
      box-shadow: 0 2px 4px rgba(13, 24, 38, 0.08), 0 12px 28px rgba(13, 24, 38, 0.1);
      transform: translateY(-1px);
    }
    .bb-account-card__header {
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      gap: 8px;
    }
  `]
})
export class Dashboard implements OnInit {
  private readonly contaService = inject(ContaService);
  private readonly snackBar = inject(MatSnackBar);
  readonly session = inject(SessionService);

  readonly contas = signal<Conta[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);

  ngOnInit(): void {
    this.contaService
      .listar()
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (contas) => this.contas.set(contas),
        error: (err) => {
          const mensagem = extrairMensagemErro(err, 'Não foi possível carregar suas contas.');
          this.erro.set(mensagem);
          this.snackBar.open(mensagem, 'Fechar', { duration: 5000, panelClass: 'bb-snackbar-error' });
        }
      });
  }

  saldoTotal(): number | null {
    if (this.contas().length === 0) return null;
    return this.contas().reduce((total, conta) => total + conta.saldo, 0);
  }

  primeiroNome(): string {
    const nome = this.session.sessao()?.nome ?? this.session.sessao()?.email ?? '';
    return nome.split(' ')[0];
  }
}
