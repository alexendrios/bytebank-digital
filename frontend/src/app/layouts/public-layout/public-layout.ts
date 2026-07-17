import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'bb-public-layout',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <div class="bb-public">
      <div class="bb-public__brand">
        <span class="bb-public__brand-mark">BB</span>
        <div>
          <div class="bb-public__brand-name">ByteBank Digital</div>
          <div class="bb-public__brand-tagline">Sistema bancário de estudo — Design Patterns, Spring Boot &amp; Angular</div>
        </div>
      </div>
      <div class="bb-public__content">
        <router-outlet></router-outlet>
      </div>
    </div>
  `,
  styles: [`
    .bb-public {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background: var(--bb-navy-900);
      background-image: radial-gradient(circle at 20% 20%, var(--bb-navy-700) 0%, var(--bb-navy-900) 55%);
      padding: 24px;
    }

    .bb-public__brand {
      display: flex;
      align-items: center;
      gap: 14px;
      margin-bottom: 28px;
      max-width: 420px;
    }

    .bb-public__brand-mark {
      font-family: var(--bb-font-mono);
      font-weight: 700;
      background: var(--bb-emerald-500);
      color: var(--bb-navy-900);
      width: 44px;
      height: 44px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 15px;
      flex-shrink: 0;
    }

    .bb-public__brand-name {
      font-family: var(--bb-font-display);
      font-weight: 700;
      font-size: 20px;
      color: #fff;
    }

    .bb-public__brand-tagline {
      font-size: 12.5px;
      color: rgba(255, 255, 255, 0.55);
      margin-top: 2px;
    }

    .bb-public__content {
      width: 100%;
      max-width: 420px;
    }
  `]
})
export class PublicLayout {}
