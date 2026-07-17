import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { SessionService } from '../../core/services/session.service';

interface ItemMenu {
  label: string;
  icon: string;
  rota: string;
}

@Component({
  selector: 'bb-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, MatListModule, MatIconModule, MatDividerModule],
  template: `
    <div class="bb-sidebar">
      <div class="bb-sidebar__brand">
        <span class="bb-sidebar__brand-mark">BB</span>
        <span class="bb-sidebar__brand-name">ByteBank<br />Digital</span>
      </div>

      <mat-nav-list class="bb-sidebar__nav">
        @for (item of itensCliente; track item.rota) {
          <a mat-list-item [routerLink]="item.rota" routerLinkActive="bb-sidebar__link--ativo" [attr.data-cy]="'nav-' + item.rota.split('/').pop()">
            <mat-icon matListItemIcon>{{ item.icon }}</mat-icon>
            <span matListItemTitle>{{ item.label }}</span>
          </a>
        }

        @if (session.isAdmin()) {
          <mat-divider class="bb-sidebar__divider"></mat-divider>
          <span class="bb-sidebar__section-label">Administração</span>
          @for (item of itensAdmin; track item.rota) {
            <a mat-list-item [routerLink]="item.rota" routerLinkActive="bb-sidebar__link--ativo" [attr.data-cy]="'nav-' + item.rota.split('/').pop()">
              <mat-icon matListItemIcon>{{ item.icon }}</mat-icon>
              <span matListItemTitle>{{ item.label }}</span>
            </a>
          }
        }
      </mat-nav-list>
    </div>
  `,
  styles: [`
    .bb-sidebar {
      height: 100%;
      display: flex;
      flex-direction: column;
      background: var(--bb-navy-800);
      color: #fff;
      width: 240px;
    }

    .bb-sidebar__brand {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 24px 20px;
    }

    .bb-sidebar__brand-mark {
      font-family: var(--bb-font-mono);
      font-weight: 700;
      background: var(--bb-emerald-500);
      color: var(--bb-navy-900);
      width: 36px;
      height: 36px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 13px;
      flex-shrink: 0;
    }

    .bb-sidebar__brand-name {
      font-family: var(--bb-font-display);
      font-weight: 600;
      line-height: 1.1;
      font-size: 14px;
    }

    .bb-sidebar__nav {
      padding-top: 8px;
      flex: 1;
    }

    .bb-sidebar__nav a {
      color: rgba(255, 255, 255, 0.75) !important;
    }

    .bb-sidebar__link--ativo {
      background: rgba(255, 255, 255, 0.08) !important;
      color: #fff !important;
    }

    .bb-sidebar__link--ativo mat-icon {
      color: var(--bb-emerald-500) !important;
    }

    .bb-sidebar__divider {
      margin: 12px 0;
      background-color: rgba(255, 255, 255, 0.12) !important;
    }

    .bb-sidebar__section-label {
      display: block;
      padding: 4px 20px 8px;
      font-family: var(--bb-font-mono);
      font-size: 11px;
      letter-spacing: 0.08em;
      text-transform: uppercase;
      color: rgba(255, 255, 255, 0.45);
    }
  `]
})
export class Sidebar {
  readonly session = inject(SessionService);

  readonly itensCliente: ItemMenu[] = [
    { label: 'Dashboard', icon: 'space_dashboard', rota: '/app/dashboard' },
    { label: 'Depositar/Sacar', icon: 'account_balance_wallet', rota: '/app/movimentar' },
    { label: 'Transferir', icon: 'sync_alt', rota: '/app/transferencia' },
    { label: 'Meu perfil', icon: 'person', rota: '/app/perfil' }
  ];

  readonly itensAdmin: ItemMenu[] = [
    { label: 'Usuários', icon: 'group', rota: '/app/admin/usuarios' },
    { label: 'Contas', icon: 'account_balance', rota: '/app/admin/contas' }
  ];
}
