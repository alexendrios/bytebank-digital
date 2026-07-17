import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { finalize } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { extrairMensagemErro } from '../../shared/utils/api-error.util';

@Component({
  selector: 'bb-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule],
  template: `
    <div class="bb-card">
      <span class="bb-page-header__eyebrow">Acesso à conta</span>
      <h1>Entrar</h1>
      <p class="bb-muted" style="margin-bottom: 20px;">Use suas credenciais para acessar o ByteBank Digital.</p>

      <form class="bb-form bb-full-width" [formGroup]="form" (ngSubmit)="enviar()" data-cy="form-login">
        <mat-form-field appearance="outline">
          <mat-label>E-mail</mat-label>
          <input matInput type="email" formControlName="email" autocomplete="email" data-cy="input-email" />
          @if (form.controls.email.hasError('required') && form.controls.email.touched) {
            <mat-error>E-mail é obrigatório</mat-error>
          }
          @if (form.controls.email.hasError('email') && form.controls.email.touched) {
            <mat-error>E-mail inválido</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Senha</mat-label>
          <input matInput [type]="mostrarSenha() ? 'text' : 'password'" formControlName="senha" autocomplete="current-password" data-cy="input-senha" />
          <button mat-icon-button matSuffix type="button" (click)="mostrarSenha.set(!mostrarSenha())">
            <mat-icon>{{ mostrarSenha() ? 'visibility_off' : 'visibility' }}</mat-icon>
          </button>
          @if (form.controls.senha.hasError('required') && form.controls.senha.touched) {
            <mat-error>Senha é obrigatória</mat-error>
          }
        </mat-form-field>

        @if (erro()) {
          <p class="bb-muted" style="color: var(--bb-terracotta-600); font-size: 13px;" data-cy="erro-login">{{ erro() }}</p>
        }

        <div class="bb-form-actions">
          <button mat-flat-button color="primary" type="submit" class="bb-full-width" [disabled]="form.invalid || carregando()" data-cy="botao-entrar">
            {{ carregando() ? 'Entrando…' : 'Entrar' }}
          </button>
        </div>
      </form>

      <p class="bb-muted" style="margin-top: 20px; text-align: center;">
        Ainda não tem conta? <a routerLink="/cadastro">Cadastre-se</a>
      </p>
    </div>
  `
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly mostrarSenha = signal(false);
  readonly carregando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', Validators.required]
  });

  enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.erro.set(null);
    this.carregando.set(true);

    this.authService
      .login(this.form.getRawValue())
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: () => this.router.navigate(['/app/dashboard']),
        error: (err) => this.erro.set(extrairMensagemErro(err, 'E-mail ou senha inválidos.'))
      });
  }
}
