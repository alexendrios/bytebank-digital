import { Component, computed, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';
import { Sidebar } from '../../components/sidebar/sidebar';
import { Navbar } from '../../components/navbar/navbar';

@Component({
  selector: 'bb-private-layout',
  standalone: true,
  imports: [RouterOutlet, MatSidenavModule, Sidebar, Navbar],
  template: `
    <mat-sidenav-container class="bb-shell">
      <mat-sidenav
        #sidenav
        [mode]="mobile() ? 'over' : 'side'"
        [opened]="!mobile()"
        [fixedInViewport]="mobile()"
        class="bb-shell__sidenav"
      >
        <bb-sidebar></bb-sidebar>
      </mat-sidenav>

      <mat-sidenav-content>
        <bb-navbar (toggleSidenav)="sidenav.toggle()"></bb-navbar>
        <main class="bb-shell__main">
          <router-outlet></router-outlet>
        </main>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .bb-shell {
      height: 100vh;
    }

    .bb-shell__sidenav {
      border-right: none !important;
    }

    .bb-shell__main {
      min-height: calc(100vh - 64px);
    }
  `]
})
export class PrivateLayout {
  private readonly breakpointObserver = inject(BreakpointObserver);

  private readonly mobile$ = this.breakpointObserver
    .observe('(max-width: 899px)')
    .pipe(map((result) => result.matches));

  readonly mobile = toSignal(this.mobile$, { initialValue: false });
}
