import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { SessionService } from '../../core/services/session.service';
import { UsuarioService } from '../../services/usuario.service';
import { extrairMensagemErro } from '../../shared/utils/api-error.util';

@Component({
  selector: 'bb-perfil',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, MatFormFieldModule, MatInputModule, MatButtonModule],
  template: `
    <div class="bb-page">
      <div class="bb-page-header">
        <div>
          <span class="bb-page-header__eyebrow">Sua conta de acesso</span>
          <h1>Meu perfil</h1>
        </div>
        <span
          class="bb-status-chip"
          [class.bb-status-chip--admin]="session.isAdmin()"
          [class.bb-status-chip--cliente]="!session.isAdmin()"
        >{{ session.perfil() }}</span>
      </div>

      <div class="bb-card" style="max-width: 480px;">
        @if (session.isAdmin() && session.sessao()?.id) {
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
              <mat-label>Nova senha (opcional)</mat-label>
              <input matInput type="password" formControlName="senha" placeholder="Deixe em branco para manter a atual" />
            </mat-form-field>

            @if (erro()) {
              <p style="color: var(--bb-terracotta-600); font-size: 13px;">{{ erro() }}</p>
            }
            @if (sucesso()) {
              <p style="color: var(--bb-emerald-600); font-size: 13px;">Perfil atualizado com sucesso.</p>
            }

            <div class="bb-form-actions">
              <button mat-flat-button color="primary" type="submit" [disabled]="form.invalid || salvando()">
                {{ salvando() ? 'Salvando…' : 'Salvar alterações' }}
              </button>
            </div>
          </form>
        } @else {
          <dl class="bb-perfil-dl">
            <dt class="bb-muted">Nome</dt>
            <dd>{{ session.sessao()?.nome ?? '—' }}</dd>
            <dt class="bb-muted">E-mail</dt>
            <dd>{{ session.sessao()?.email }}</dd>
          </dl>
          <p class="bb-muted" style="font-size: 13px; margin-top: 16px;">
            A API ainda não expõe um endpoint de autoatualização de perfil para o perfil Cliente
            (<code>PUT /usuarios/{{ '{' }}id{{ '}' }}</code> é restrito a ADMIN — ver <code>docs/07-frontend.md</code>).
            Para alterar seus dados, contate a administração.
          </p>
        }
      </div>
    </div>
  `,
  styles: [`
    .bb-perfil-dl {
      display: grid;
      grid-template-columns: auto 1fr;
      gap: 6px 16px;
      margin: 0;
    }
    .bb-perfil-dl dt { font-size: 13px; }
    .bb-perfil-dl dd { margin: 0; font-weight: 500; }
  `]
})
export class Perfil {
  private readonly fb = inject(FormBuilder);
  private readonly usuarioService = inject(UsuarioService);
  private readonly snackBar = inject(MatSnackBar);
  readonly session = inject(SessionService);

  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly sucesso = signal(false);

  readonly form = this.fb.nonNullable.group({
    nome: [this.session.sessao()?.nome ?? '', [Validators.required, Validators.minLength(3)]],
    email: [this.session.sessao()?.email ?? '', [Validators.required, Validators.email]],
    senha: ['']
  });

  salvar(): void {
    const id = this.session.sessao()?.id;
    if (this.form.invalid || !id) return;

    this.erro.set(null);
    this.sucesso.set(false);
    this.salvando.set(true);

    const { nome, email, senha } = this.form.getRawValue();

    this.usuarioService
      .atualizar(id, { nome, email, senha: senha || undefined, perfil: 'ADMIN' })
      .pipe(finalize(() => this.salvando.set(false)))
      .subscribe({
        next: (usuario) => {
          this.session.atualizarDadosUsuario({ nome: usuario.nome, email: usuario.email });
          this.sucesso.set(true);
          this.snackBar.open('Perfil atualizado.', 'Fechar', { duration: 4000, panelClass: 'bb-snackbar-success' });
        },
        error: (err) => this.erro.set(extrairMensagemErro(err, 'Não foi possível atualizar seu perfil.'))
      });
  }
}
