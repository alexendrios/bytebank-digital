import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { ContaService } from '../../services/conta.service';
import { TransferenciaService } from '../../services/transferencia.service';
import { Conta } from '../../models/conta.model';
import { extrairMensagemErro } from '../../shared/utils/api-error.util';

@Component({
  selector: 'bb-transferencia',
  standalone: true,
  imports: [ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  template: `
    <div class="bb-page">
      <div class="bb-page-header">
        <div>
          <span class="bb-page-header__eyebrow">Movimentação entre contas</span>
          <h1>Transferir</h1>
        </div>
      </div>

      <div class="bb-card" style="max-width: 480px;">
        <form class="bb-form bb-full-width" [formGroup]="form" (ngSubmit)="enviar()" data-cy="form-transferencia">
          <mat-form-field appearance="outline">
            <mat-label>Conta de origem</mat-label>
            <mat-select formControlName="contaOrigemId" data-cy="select-conta-origem">
              @for (conta of minhasContas(); track conta.id) {
                <mat-option [value]="conta.id">Conta {{ conta.numero }} · Ag. {{ conta.agencia }}</mat-option>
              }
            </mat-select>
            @if (form.controls.contaOrigemId.hasError('required') && form.controls.contaOrigemId.touched) {
              <mat-error>Selecione a conta de origem</mat-error>
            }
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>ID da conta de destino</mat-label>
            <input matInput formControlName="contaDestinoId" placeholder="UUID da conta destinatária" data-cy="input-conta-destino" />
            <mat-hint>
              A API identifica a conta destino pelo ID (UUID), não pelo número — peça esse dado a quem vai receber.
            </mat-hint>
            @if (form.controls.contaDestinoId.hasError('required') && form.controls.contaDestinoId.touched) {
              <mat-error>Informe a conta de destino</mat-error>
            }
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Valor</mat-label>
            <span matTextPrefix>R$&nbsp;</span>
            <input matInput type="number" step="0.01" min="0.01" formControlName="valor" data-cy="input-valor-transferencia" />
            @if (form.controls.valor.hasError('required') && form.controls.valor.touched) {
              <mat-error>Informe um valor</mat-error>
            }
            @if (form.controls.valor.hasError('min') && form.controls.valor.touched) {
              <mat-error>Valor deve ser maior que zero</mat-error>
            }
          </mat-form-field>

          @if (erro()) {
            <p style="color: var(--bb-terracotta-600); font-size: 13px;" data-cy="erro-transferencia">{{ erro() }}</p>
          }

          <div class="bb-form-actions">
            <button mat-flat-button color="primary" type="submit" class="bb-full-width" [disabled]="form.invalid || processando()" data-cy="botao-confirmar-transferencia">
              {{ processando() ? 'Transferindo…' : 'Confirmar transferência' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `
})
export class TransferenciaPage implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly contaService = inject(ContaService);
  private readonly transferenciaService = inject(TransferenciaService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly minhasContas = signal<Conta[]>([]);
  readonly processando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    contaOrigemId: ['', Validators.required],
    contaDestinoId: ['', Validators.required],
    valor: [null as unknown as number, [Validators.required, Validators.min(0.01)]]
  });

  ngOnInit(): void {
    this.contaService.listar().subscribe({
      next: (contas) => {
        this.minhasContas.set(contas);
        if (contas.length === 1) {
          this.form.patchValue({ contaOrigemId: contas[0].id });
        }
      }
    });
  }

  enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.erro.set(null);
    this.processando.set(true);

    this.transferenciaService
      .transferir(this.form.getRawValue())
      .pipe(finalize(() => this.processando.set(false)))
      .subscribe({
        next: () => {
          this.snackBar.open('Transferência realizada com sucesso.', 'Fechar', {
            duration: 4000,
            panelClass: 'bb-snackbar-success'
          });
          this.router.navigate(['/app/dashboard']);
        },
        error: (err) => this.erro.set(extrairMensagemErro(err, 'Não foi possível concluir a transferência.'))
      });
  }
}
