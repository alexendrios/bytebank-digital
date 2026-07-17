import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { finalize } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { extrairMensagemErro } from '../../shared/utils/api-error.util';

@Component({
  selector: 'bb-cadastro',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule],
  template: `
    <div class="bb-card">
      <span class="bb-page-header__eyebrow">Novo acesso</span>
      <h1>Criar conta</h1>
      <p class="bb-muted" style="margin-bottom: 20px;">
        Seu acesso é criado com perfil Cliente. Contas bancárias são criadas pela administração.
      </p>

      <form class="bb-form bb-full-width" [formGroup]="form" (ngSubmit)="enviar()">
        <mat-form-field appearance="outline">
          <mat-label>Nome completo</mat-label>
          <input matInput formControlName="nome" autocomplete="name" />
          @if (form.controls.nome.hasError('required') && form.controls.nome.touched) {
            <mat-error>Nome é obrigatório</mat-error>
          }
          @if (form.controls.nome.hasError('minlength') && form.controls.nome.touched) {
            <mat-error>Nome deve ter ao menos 3 caracteres</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>E-mail</mat-label>
          <input matInput type="email" formControlName="email" autocomplete="email" />
          @if (form.controls.email.hasError('required') && form.controls.email.touched) {
            <mat-error>E-mail é obrigatório</mat-error>
          }
          @if (form.controls.email.hasError('email') && form.controls.email.touched) {
            <mat-error>E-mail inválido</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Senha</mat-label>
          <input matInput type="password" formControlName="senha" autocomplete="new-password" />
          @if (form.controls.senha.hasError('required') && form.controls.senha.touched) {
            <mat-error>Senha é obrigatória</mat-error>
          }
          @if (form.controls.senha.hasError('minlength') && form.controls.senha.touched) {
            <mat-error>Senha deve ter no mínimo 8 caracteres</mat-error>
          }
        </mat-form-field>

        @if (erro()) {
          <p class="bb-muted" style="color: var(--bb-terracotta-600); font-size: 13px;">{{ erro() }}</p>
        }

        <div class="bb-form-actions">
          <button mat-flat-button color="primary" type="submit" class="bb-full-width" [disabled]="form.invalid || carregando()">
            {{ carregando() ? 'Criando…' : 'Criar conta' }}
          </button>
        </div>
      </form>

      <p class="bb-muted" style="margin-top: 20px; text-align: center;">
        Já tem acesso? <a routerLink="/login">Entrar</a>
      </p>
    </div>
  `
})
export class Cadastro {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly carregando = signal(false);
  readonly erro = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(150)]],
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required, Validators.minLength(8)]]
  });

  enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.erro.set(null);
    this.carregando.set(true);

    this.authService
      .registrar(this.form.getRawValue())
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: () => this.router.navigate(['/app/dashboard']),
        error: (err) => this.erro.set(extrairMensagemErro(err, 'Não foi possível criar sua conta.'))
      });
  }
}
