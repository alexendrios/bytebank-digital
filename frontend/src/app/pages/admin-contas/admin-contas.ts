import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { ContaService } from '../../services/conta.service';
import { UsuarioService } from '../../services/usuario.service';
import { Conta } from '../../models/conta.model';
import { Usuario } from '../../models/usuario.model';
import { Loading } from '../../shared/components/loading/loading';
import { ConfirmDialog } from '../../shared/components/confirm-dialog/confirm-dialog';
import { extrairMensagemErro } from '../../shared/utils/api-error.util';

@Component({
  selector: 'bb-admin-contas',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CurrencyPipe,
    RouterLink,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule,
    Loading
  ],
  template: `
    <div class="bb-page">
      <div class="bb-page-header">
        <div>
          <span class="bb-page-header__eyebrow">Administração</span>
          <h1>Contas</h1>
        </div>
        <button mat-flat-button color="primary" (click)="novaConta()">
          <mat-icon>add</mat-icon>
          Nova conta
        </button>
      </div>

      @if (mostrarFormulario()) {
        <div class="bb-card" style="max-width: 480px; margin-bottom: 24px;">
          <h3>{{ idEmEdicao() ? 'Editar conta' : 'Nova conta' }}</h3>
          <form class="bb-form bb-full-width" [formGroup]="form" (ngSubmit)="salvar()">
            <mat-form-field appearance="outline">
              <mat-label>Agência</mat-label>
              <input matInput formControlName="agencia" placeholder="0001" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Titular</mat-label>
              <mat-select formControlName="usuarioId">
                @for (u of usuarios(); track u.id) {
                  <mat-option [value]="u.id">{{ u.nome }} ({{ u.email }})</mat-option>
                }
              </mat-select>
            </mat-form-field>

            @if (erro()) {
              <p style="color: var(--bb-terracotta-600); font-size: 13px;">{{ erro() }}</p>
            }

            <div class="bb-form-actions">
              <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || salvando()">
                {{ salvando() ? 'Salvando…' : 'Salvar' }}
              </button>
              <button mat-button type="button" (click)="cancelar()">Cancelar</button>
            </div>
          </form>
        </div>
      }

      @if (carregando()) {
        <bb-loading label="Carregando contas…"></bb-loading>
      } @else {
        <div class="bb-card" style="padding: 0;">
          <table mat-table [dataSource]="contas()" style="width: 100%;">
            <ng-container matColumnDef="numero">
              <th mat-header-cell *matHeaderCellDef>Conta</th>
              <td mat-cell *matCellDef="let c">
                <a [routerLink]="['/app/contas', c.id]">{{ c.numero }}</a>
              </td>
            </ng-container>
            <ng-container matColumnDef="agencia">
              <th mat-header-cell *matHeaderCellDef>Agência</th>
              <td mat-cell *matCellDef="let c">{{ c.agencia }}</td>
            </ng-container>
            <ng-container matColumnDef="titular">
              <th mat-header-cell *matHeaderCellDef>Titular</th>
              <td mat-cell *matCellDef="let c">{{ c.usuarioNome }}</td>
            </ng-container>
            <ng-container matColumnDef="saldo">
              <th mat-header-cell *matHeaderCellDef style="text-align: right;">Saldo</th>
              <td mat-cell *matCellDef="let c" style="text-align: right;">
                <span class="bb-money bb-money--positive">{{ c.saldo | currency: 'BRL' }}</span>
              </td>
            </ng-container>
            <ng-container matColumnDef="acoes">
              <th mat-header-cell *matHeaderCellDef></th>
              <td mat-cell *matCellDef="let c" style="text-align: right;">
                <button mat-icon-button (click)="editar(c)" aria-label="Editar">
                  <mat-icon>edit</mat-icon>
                </button>
                <button mat-icon-button (click)="remover(c)" aria-label="Remover">
                  <mat-icon>delete</mat-icon>
                </button>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="colunas"></tr>
            <tr mat-row *matRowDef="let row; columns: colunas;"></tr>
          </table>

          @if (contas().length === 0) {
            <div class="bb-empty-state">Nenhuma conta cadastrada.</div>
          }
        </div>
      }
    </div>
  `
})
export class AdminContas implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly contaService = inject(ContaService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly colunas = ['numero', 'agencia', 'titular', 'saldo', 'acoes'];
  readonly contas = signal<Conta[]>([]);
  readonly usuarios = signal<Usuario[]>([]);
  readonly carregando = signal(true);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly mostrarFormulario = signal(false);
  readonly idEmEdicao = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    agencia: ['', Validators.required],
    usuarioId: ['', Validators.required]
  });

  ngOnInit(): void {
    this.carregar();
    this.usuarioService.listar().subscribe({ next: (usuarios) => this.usuarios.set(usuarios) });
  }

  private carregar(): void {
    this.carregando.set(true);
    this.contaService
      .listar()
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (contas) => this.contas.set(contas),
        error: (err) => this.snackBar.open(extrairMensagemErro(err), 'Fechar', { duration: 5000, panelClass: 'bb-snackbar-error' })
      });
  }

  novaConta(): void {
    this.idEmEdicao.set(null);
    this.form.reset({ agencia: '', usuarioId: '' });
    this.erro.set(null);
    this.mostrarFormulario.set(true);
  }

  editar(conta: Conta): void {
    this.idEmEdicao.set(conta.id);
    this.form.reset({ agencia: conta.agencia, usuarioId: conta.usuarioId });
    this.erro.set(null);
    this.mostrarFormulario.set(true);
  }

  cancelar(): void {
    this.mostrarFormulario.set(false);
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.erro.set(null);
    this.salvando.set(true);

    const payload = this.form.getRawValue();
    const id = this.idEmEdicao();
    const chamada = id ? this.contaService.atualizar(id, payload) : this.contaService.criar(payload);

    chamada.pipe(finalize(() => this.salvando.set(false))).subscribe({
      next: () => {
        this.mostrarFormulario.set(false);
        this.snackBar.open(id ? 'Conta atualizada.' : 'Conta criada.', 'Fechar', {
          duration: 4000,
          panelClass: 'bb-snackbar-success'
        });
        this.carregar();
      },
      error: (err) => this.erro.set(extrairMensagemErro(err, 'Não foi possível salvar a conta.'))
    });
  }

  remover(conta: Conta): void {
    const ref = this.dialog.open(ConfirmDialog, {
      data: {
        titulo: 'Remover conta',
        mensagem: `Tem certeza que deseja remover a conta ${conta.numero}? Essa ação não pode ser desfeita.`,
        confirmarLabel: 'Remover',
        destrutivo: true
      }
    });

    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) return;
      this.contaService.remover(conta.id).subscribe({
        next: () => {
          this.snackBar.open('Conta removida.', 'Fechar', { duration: 4000, panelClass: 'bb-snackbar-success' });
          this.carregar();
        },
        error: (err) => this.snackBar.open(extrairMensagemErro(err), 'Fechar', { duration: 5000, panelClass: 'bb-snackbar-error' })
      });
    });
  }
}
