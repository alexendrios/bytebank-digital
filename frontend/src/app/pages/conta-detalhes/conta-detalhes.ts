import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { ContaService } from '../../services/conta.service';
import { Conta } from '../../models/conta.model';
import { Loading } from '../../shared/components/loading/loading';
import { extrairMensagemErro } from '../../shared/utils/api-error.util';

@Component({
  selector: 'bb-conta-detalhes',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, RouterLink, MatButtonModule, MatIconModule, Loading],
  template: `
    <div class="bb-page">
      @if (carregando()) {
        <bb-loading label="Carregando conta…"></bb-loading>
      } @else if (erro()) {
        <div class="bb-card bb-empty-state">{{ erro() }}</div>
      } @else if (conta(); as c) {
        <div class="bb-page-header">
          <div>
            <span class="bb-page-header__eyebrow">Agência {{ c.agencia }} · Conta {{ c.numero }}</span>
            <h1>{{ c.usuarioNome }}</h1>
          </div>
        </div>

        <div class="bb-card" style="margin-bottom: 20px;">
          <span class="bb-page-header__eyebrow">Saldo disponível</span>
          <div class="bb-money bb-money--positive" style="font-size: 36px; font-weight: 600;" data-cy="saldo-conta">
            {{ c.saldo | currency: 'BRL' }}
          </div>
          <span class="bb-muted" style="font-size: 12.5px;">Conta aberta em {{ c.dataCriacao | date: 'dd/MM/yyyy' }}</span>
        </div>

        <div class="bb-grid bb-grid--3">
          <a mat-flat-button color="primary" [routerLink]="['/app/contas', c.id, 'movimentar']" [queryParams]="{ acao: 'deposito' }" data-cy="link-depositar">
            <mat-icon>add_circle</mat-icon>
            Depositar
          </a>
          <a mat-stroked-button [routerLink]="['/app/contas', c.id, 'movimentar']" [queryParams]="{ acao: 'saque' }" data-cy="link-sacar">
            <mat-icon>remove_circle</mat-icon>
            Sacar
          </a>
          <a mat-stroked-button [routerLink]="['/app/contas', c.id, 'extrato']">
            <mat-icon>receipt_long</mat-icon>
            Ver extrato
          </a>
        </div>
      }
    </div>
  `
})
export class ContaDetalhes implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly contaService = inject(ContaService);
  private readonly snackBar = inject(MatSnackBar);

  readonly conta = signal<Conta | null>(null);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.contaService
      .buscarPorId(id)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (conta) => this.conta.set(conta),
        error: (err) => {
          const mensagem = extrairMensagemErro(err, 'Não foi possível carregar esta conta.');
          this.erro.set(mensagem);
          this.snackBar.open(mensagem, 'Fechar', { duration: 5000, panelClass: 'bb-snackbar-error' });
        }
      });
  }
}
