import { Component, EventEmitter, Output, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { SessionService } from '../../core/services/session.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'bb-navbar',
  standalone: true,
  imports: [MatToolbarModule, MatButtonModule, MatIconModule, MatMenuModule, RouterLink],
  template: `
    <mat-toolbar class="bb-navbar">
      <button mat-icon-button class="bb-navbar__menu-btn" (click)="toggleSidenav.emit()">
        <mat-icon>menu</mat-icon>
      </button>

      <span class="bb-spacer"></span>

      <button mat-button [matMenuTriggerFor]="menu" class="bb-navbar__user">
        <span class="bb-navbar__user-name">{{ session.sessao()?.nome ?? session.sessao()?.email }}</span>
        <span
          class="bb-status-chip"
          [class.bb-status-chip--admin]="session.isAdmin()"
          [class.bb-status-chip--cliente]="!session.isAdmin()"
        >{{ session.perfil() }}</span>
        <mat-icon>expand_more</mat-icon>
      </button>
      <mat-menu #menu="matMenu">
        <button mat-menu-item routerLink="/app/perfil" (click)="menu.closed.emit(undefined)">
          <mat-icon>person</mat-icon>
          <span>Meu perfil</span>
        </button>
        <button mat-menu-item (click)="sair()">
          <mat-icon>logout</mat-icon>
          <span>Sair</span>
        </button>
      </mat-menu>
    </mat-toolbar>
  `,
  styles: [`
    .bb-navbar {
      background: var(--bb-surface);
      border-bottom: 1px solid var(--bb-border);
      color: var(--bb-text);
      position: sticky;
      top: 0;
      z-index: 10;
    }

    .bb-navbar__menu-btn {
      display: none;
    }

    .bb-navbar__user {
      display: flex;
      align-items: center;
      gap: 10px;
      text-transform: none;
    }

    .bb-navbar__user-name {
      font-weight: 500;
    }

    @media (max-width: 900px) {
      .bb-navbar__menu-btn {
        display: inline-flex;
      }
      .bb-navbar__user-name {
        display: none;
      }
    }
  `]
})
export class Navbar {
  @Output() toggleSidenav = new EventEmitter<void>();

  readonly session = inject(SessionService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  sair(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
