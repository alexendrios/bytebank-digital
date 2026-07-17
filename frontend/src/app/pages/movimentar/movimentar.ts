import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatTabsModule } from '@angular/material/tabs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { ContaService } from '../../services/conta.service';
import { Conta } from '../../models/conta.model';
import { Loading } from '../../shared/components/loading/loading';
import { extrairMensagemErro } from '../../shared/utils/api-error.util';

@Component({
  selector: 'bb-movimentar',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CurrencyPipe,
    RouterLink,
    MatTabsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    Loading
  ],
  template: `
    <div class="bb-page" data-cy="pagina-movimentar">
      @if (carregandoConta()) {
        <bb-loading label="Carregando conta…"></bb-loading>
      } @else if (erroCarregamento()) {
        <div class="bb-card bb-empty-state">{{ erroCarregamento() }}</div>
      } @else if (precisaEscolherConta()) {
        <div class="bb-page-header">
          <div>
            <span class="bb-page-header__eyebrow">Depósito e saque</span>
            <h1>Selecione uma conta</h1>
          </div>
        </div>
        <div class="bb-grid bb-grid--2">
          @for (c of contasDisponiveis(); track c.id) {
            <button
              type="button"
              (click)="selecionarConta(c)"
              class="bb-card bb-account-card"
              style="cursor: pointer; text-align: left; border: none; width: 100%;"
              [attr.data-cy]="'conta-opcao-' + c.numero"
            >
              <span class="bb-page-header__eyebrow">Ag. {{ c.agencia }} · Conta {{ c.numero }}</span>
              <div class="bb-money bb-money--positive" style="font-size: 22px; font-weight: 600; margin-top: 8px;">
                {{ c.saldo | currency: 'BRL' }}
              </div>
            </button>
          }
        </div>
      } @else if (conta(); as c) {
        <div class="bb-page-header">
          <div>
            <span class="bb-page-header__eyebrow">Conta {{ c.numero }} · Ag. {{ c.agencia }}</span>
            <h1>Movimentar</h1>
          </div>
        </div>

        <div class="bb-card" style="max-width: 480px;">
          <div style="margin-bottom: 16px;">
            <span class="bb-page-header__eyebrow">Saldo atual</span>
            <div class="bb-money bb-money--positive" style="font-size: 22px; font-weight: 600;" data-cy="saldo-atual">{{ c.saldo | currency: 'BRL' }}</div>
          </div>

          <mat-tab-group [selectedIndex]="abaSelecionada()" (selectedIndexChange)="abaSelecionada.set($event)">
            <mat-tab label="Depósito">
              <form class="bb-form bb-full-width" style="margin-top: 20px;" [formGroup]="formDeposito" (ngSubmit)="depositar()" data-cy="form-deposito">
                <mat-form-field appearance="outline">
                  <mat-label>Valor a depositar</mat-label>
                  <span matTextPrefix>R$&nbsp;</span>
                  <input matInput type="number" step="0.01" min="0.01" formControlName="valor" data-cy="input-valor-deposito" />
                  @if (formDeposito.controls.valor.hasError('required') && formDeposito.controls.valor.touched) {
                    <mat-error>Informe um valor</mat-error>
                  }
                  @if (formDeposito.controls.valor.hasError('min') && formDeposito.controls.valor.touched) {
                    <mat-error>Valor deve ser maior que zero</mat-error>
                  }
                </mat-form-field>
                <div class="bb-form-actions">
                  <button mat-flat-button color="primary" type="submit" class="bb-full-width" [disabled]="formDeposito.invalid || processando()" data-cy="botao-confirmar-deposito">
                    {{ processando() ? 'Processando…' : 'Confirmar depósito' }}
                  </button>
                </div>
              </form>
            </mat-tab>

            <mat-tab label="Saque">
              <form class="bb-form bb-full-width" style="margin-top: 20px;" [formGroup]="formSaque" (ngSubmit)="sacar()" data-cy="form-saque">
                <mat-form-field appearance="outline">
                  <mat-label>Valor a sacar</mat-label>
                  <span matTextPrefix>R$&nbsp;</span>
                  <input matInput type="number" step="0.01" min="0.01" formControlName="valor" data-cy="input-valor-saque" />
                  @if (formSaque.controls.valor.hasError('required') && formSaque.controls.valor.touched) {
                    <mat-error>Informe um valor</mat-error>
                  }
                  @if (formSaque.controls.valor.hasError('min') && formSaque.controls.valor.touched) {
                    <mat-error>Valor deve ser maior que zero</mat-error>
                  }
                </mat-form-field>
                <div class="bb-form-actions">
                  <button mat-flat-button color="primary" type="submit" class="bb-full-width" [disabled]="formSaque.invalid || processando()" data-cy="botao-confirmar-saque">
                    {{ processando() ? 'Processando…' : 'Confirmar saque' }}
                  </button>
                </div>
              </form>
            </mat-tab>
          </mat-tab-group>

          @if (erro()) {
            <p style="color: var(--bb-terracotta-600); font-size: 13px; margin-top: 12px;" data-cy="erro-operacao">{{ erro() }}</p>
          }
        </div>

        <a routerLink="/app/contas/{{ c.id }}" class="bb-muted" style="display: inline-block; margin-top: 16px; font-size: 13px;">
          ← Voltar para a conta
        </a>
      }
    </div>
  `
})
export class Movimentar implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly contaService = inject(ContaService);
  private readonly snackBar = inject(MatSnackBar);

  readonly conta = signal<Conta | null>(null);
  readonly carregandoConta = signal(true);
  readonly processando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly erroCarregamento = signal<string | null>(null);
  readonly abaSelecionada = signal(0);

  /** Preenchido apenas quando a rota é acessada sem :id (/app/movimentar) e o cliente tem mais de uma conta. */
  readonly contasDisponiveis = signal<Conta[]>([]);
  readonly precisaEscolherConta = signal(false);

  readonly formDeposito = this.fb.nonNullable.group({
    valor: [null as unknown as number, [Validators.required, Validators.min(0.01)]]
  });

  readonly formSaque = this.fb.nonNullable.group({
    valor: [null as unknown as number, [Validators.required, Validators.min(0.01)]]
  });

  ngOnInit(): void {
    const acao = this.route.snapshot.queryParamMap.get('acao');
    this.abaSelecionada.set(acao === 'saque' ? 1 : 0);

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.carregarConta(id);
      return;
    }

    // Rota /app/movimentar (sem id): descobre a(s) conta(s) do próprio cliente.
    this.contaService
      .listar()
      .pipe(finalize(() => this.carregandoConta.set(false)))
      .subscribe({
        next: (contas) => {
          if (contas.length === 0) {
            this.erroCarregamento.set('Você ainda não tem contas. Fale com a administração para abrir a sua.');
          } else if (contas.length === 1) {
            this.conta.set(contas[0]);
          } else {
            this.contasDisponiveis.set(contas);
            this.precisaEscolherConta.set(true);
          }
        },
        error: (err) => this.erroCarregamento.set(extrairMensagemErro(err, 'Não foi possível carregar suas contas.'))
      });
  }

  selecionarConta(c: Conta): void {
    this.precisaEscolherConta.set(false);
    this.conta.set(c);
  }

  private carregarConta(id: string): void {
    this.contaService
      .buscarPorId(id)
      .pipe(finalize(() => this.carregandoConta.set(false)))
      .subscribe({
        next: (conta) => this.conta.set(conta),
        error: (err) => this.erroCarregamento.set(extrairMensagemErro(err))
      });
  }

  depositar(): void {
    if (this.formDeposito.invalid || !this.conta()) return;
    this.executar(() =>
      this.contaService.depositar(this.conta()!.id, this.formDeposito.getRawValue().valor)
    );
  }

  sacar(): void {
    if (this.formSaque.invalid || !this.conta()) return;
    this.executar(() => this.contaService.sacar(this.conta()!.id, this.formSaque.getRawValue().valor));
  }

  private executar(chamada: () => ReturnType<ContaService['depositar']>): void {
    this.erro.set(null);
    this.processando.set(true);

    chamada()
      .pipe(finalize(() => this.processando.set(false)))
      .subscribe({
        next: (conta) => {
          this.snackBar.open('Operação realizada com sucesso.', 'Fechar', {
            duration: 4000,
            panelClass: 'bb-snackbar-success'
          });
          this.router.navigate(['/app/contas', conta.id]);
        },
        error: (err) => this.erro.set(extrairMensagemErro(err, 'Não foi possível concluir a operação.'))
      });
  }
}
