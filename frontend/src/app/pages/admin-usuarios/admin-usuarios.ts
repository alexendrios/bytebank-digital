import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { UsuarioService } from '../../services/usuario.service';
import { Usuario } from '../../models/usuario.model';
import { SessionService } from '../../core/services/session.service';
import { Loading } from '../../shared/components/loading/loading';
import { ConfirmDialog } from '../../shared/components/confirm-dialog/confirm-dialog';
import { extrairMensagemErro } from '../../shared/utils/api-error.util';

@Component({
  selector: 'bb-admin-usuarios',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    DatePipe,
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
          <h1>Usuários</h1>
        </div>
        <button mat-flat-button color="primary" (click)="novoUsuario()">
          <mat-icon>add</mat-icon>
          Novo usuário
        </button>
      </div>

      @if (mostrarFormulario()) {
        <div class="bb-card" style="max-width: 480px; margin-bottom: 24px;">
          <h3>{{ idEmEdicao() ? 'Editar usuário' : 'Novo usuário' }}</h3>
          <form class="bb-form bb-full-width" [formGroup]="form" (ngSubmit)="salvar()">
            <mat-form-field appearance="outline">
              <mat-label>Nome</mat-label>
              <input matInput formControlName="nome" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>E-mail</mat-label>
              <input matInput type="email" formControlName="email" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>{{ idEmEdicao() ? 'Nova senha (opcional)' : 'Senha' }}</mat-label>
              <input matInput type="password" formControlName="senha" />
            </mat-form-field>
            <mat-form-field appearance="outline">
              <mat-label>Perfil</mat-label>
              <mat-select formControlName="perfil">
                <mat-option value="CLIENTE">Cliente</mat-option>
                <mat-option value="ADMIN">Admin</mat-option>
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
        <bb-loading label="Carregando usuários…"></bb-loading>
      } @else {
        <div class="bb-card" style="padding: 0;">
          <table mat-table [dataSource]="usuarios()" style="width: 100%;">
            <ng-container matColumnDef="nome">
              <th mat-header-cell *matHeaderCellDef>Nome</th>
              <td mat-cell *matCellDef="let u">{{ u.nome }}</td>
            </ng-container>
            <ng-container matColumnDef="email">
              <th mat-header-cell *matHeaderCellDef>E-mail</th>
              <td mat-cell *matCellDef="let u">{{ u.email }}</td>
            </ng-container>
            <ng-container matColumnDef="perfil">
              <th mat-header-cell *matHeaderCellDef>Perfil</th>
              <td mat-cell *matCellDef="let u">
                <span class="bb-status-chip" [class.bb-status-chip--admin]="u.perfil === 'ADMIN'" [class.bb-status-chip--cliente]="u.perfil === 'CLIENTE'">
                  {{ u.perfil }}
                </span>
              </td>
            </ng-container>
            <ng-container matColumnDef="dataCadastro">
              <th mat-header-cell *matHeaderCellDef>Cadastro</th>
              <td mat-cell *matCellDef="let u">{{ u.dataCadastro | date: 'dd/MM/yyyy' }}</td>
            </ng-container>
            <ng-container matColumnDef="acoes">
              <th mat-header-cell *matHeaderCellDef></th>
              <td mat-cell *matCellDef="let u" style="text-align: right;">
                <button mat-icon-button (click)="editar(u)" aria-label="Editar">
                  <mat-icon>edit</mat-icon>
                </button>
                <button mat-icon-button (click)="remover(u)" aria-label="Remover" [disabled]="u.id === session.sessao()?.id">
                  <mat-icon>delete</mat-icon>
                </button>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="colunas"></tr>
            <tr mat-row *matRowDef="let row; columns: colunas;"></tr>
          </table>

          @if (usuarios().length === 0) {
            <div class="bb-empty-state">Nenhum usuário cadastrado.</div>
          }
        </div>
      }
    </div>
  `
})
export class AdminUsuarios implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly usuarioService = inject(UsuarioService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  readonly session = inject(SessionService);

  readonly colunas = ['nome', 'email', 'perfil', 'dataCadastro', 'acoes'];
  readonly usuarios = signal<Usuario[]>([]);
  readonly carregando = signal(true);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly mostrarFormulario = signal(false);
  readonly idEmEdicao = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    senha: [''],
    perfil: ['CLIENTE' as 'CLIENTE' | 'ADMIN', Validators.required]
  });

  ngOnInit(): void {
    this.carregar();
  }

  private carregar(): void {
    this.carregando.set(true);
    this.usuarioService
      .listar()
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (usuarios) => this.usuarios.set(usuarios),
        error: (err) => this.snackBar.open(extrairMensagemErro(err), 'Fechar', { duration: 5000, panelClass: 'bb-snackbar-error' })
      });
  }

  novoUsuario(): void {
    this.idEmEdicao.set(null);
    this.form.reset({ nome: '', email: '', senha: '', perfil: 'CLIENTE' });
    this.form.controls.senha.setValidators([Validators.required, Validators.minLength(8)]);
    this.form.controls.senha.updateValueAndValidity();
    this.erro.set(null);
    this.mostrarFormulario.set(true);
  }

  editar(usuario: Usuario): void {
    this.idEmEdicao.set(usuario.id);
    this.form.reset({ nome: usuario.nome, email: usuario.email, senha: '', perfil: usuario.perfil });
    this.form.controls.senha.setValidators([Validators.minLength(8)]);
    this.form.controls.senha.updateValueAndValidity();
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

    const { nome, email, senha, perfil } = this.form.getRawValue();
    const payload = { nome, email, perfil, senha: senha || undefined };

    this.erro.set(null);
    this.salvando.set(true);

    const id = this.idEmEdicao();
    const chamada = id ? this.usuarioService.atualizar(id, payload) : this.usuarioService.criar(payload);

    chamada.pipe(finalize(() => this.salvando.set(false))).subscribe({
      next: () => {
        this.mostrarFormulario.set(false);
        this.snackBar.open(id ? 'Usuário atualizado.' : 'Usuário criado.', 'Fechar', {
          duration: 4000,
          panelClass: 'bb-snackbar-success'
        });
        this.carregar();
      },
      error: (err) => this.erro.set(extrairMensagemErro(err, 'Não foi possível salvar o usuário.'))
    });
  }

  remover(usuario: Usuario): void {
    const ref = this.dialog.open(ConfirmDialog, {
      data: {
        titulo: 'Remover usuário',
        mensagem: `Tem certeza que deseja remover "${usuario.nome}"? Essa ação não pode ser desfeita.`,
        confirmarLabel: 'Remover',
        destrutivo: true
      }
    });

    ref.afterClosed().subscribe((confirmado) => {
      if (!confirmado) return;
      this.usuarioService.remover(usuario.id).subscribe({
        next: () => {
          this.snackBar.open('Usuário removido.', 'Fechar', { duration: 4000, panelClass: 'bb-snackbar-success' });
          this.carregar();
        },
        error: (err) => this.snackBar.open(extrairMensagemErro(err), 'Fechar', { duration: 5000, panelClass: 'bb-snackbar-error' })
      });
    });
  }
}
