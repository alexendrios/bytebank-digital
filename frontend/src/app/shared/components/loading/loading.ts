import { Component, Input } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'bb-loading',
  standalone: true,
  imports: [MatProgressSpinnerModule],
  template: `
    <div class="bb-loading">
      <mat-spinner [diameter]="diameter"></mat-spinner>
      @if (label) {
        <span class="bb-loading__label">{{ label }}</span>
      }
    </div>
  `,
  styles: [`
    .bb-loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 12px;
      padding: 48px 0;
      color: var(--bb-text-muted);
      font-family: var(--bb-font-mono);
      font-size: 13px;
    }
  `]
})
export class Loading {
  @Input() diameter = 36;
  @Input() label = '';
}
