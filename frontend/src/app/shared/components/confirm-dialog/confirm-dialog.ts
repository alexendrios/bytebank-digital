import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

export interface ConfirmDialogData {
  titulo: string;
  mensagem: string;
  confirmarLabel?: string;
  cancelarLabel?: string;
  destrutivo?: boolean;
}

@Component({
  selector: 'bb-confirm-dialog',
  standalone: true,
  imports: [MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>{{ data.titulo }}</h2>
    <mat-dialog-content>{{ data.mensagem }}</mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="ref.close(false)">{{ data.cancelarLabel ?? 'Cancelar' }}</button>
      <button
        mat-flat-button
        [color]="data.destrutivo ? 'warn' : 'primary'"
        (click)="ref.close(true)"
      >
        {{ data.confirmarLabel ?? 'Confirmar' }}
      </button>
    </mat-dialog-actions>
  `
})
export class ConfirmDialog {
  constructor(
    public ref: MatDialogRef<ConfirmDialog, boolean>,
    @Inject(MAT_DIALOG_DATA) public data: ConfirmDialogData
  ) {}
}
