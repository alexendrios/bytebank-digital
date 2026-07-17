import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { ContaService } from '../../services/conta.service';
import { Movimentacao, Page } from '../../models/movimentacao.model';
import { TipoMovimentacaoPipe } from '../../shared/pipes/tipo-movimentacao.pipe';
import { Loading } from '../../shared/components/loading/loading';
import { extrairMensagemErro } from '../../shared/utils/api-error.util';

@Component({
  selector: 'bb-extrato',
  standalone: true,
  imports: [CurrencyPipe, DatePipe, RouterLink, MatTableModule, MatPaginatorModule, TipoMovimentacaoPipe, Loading],
  template: `
    <div class="bb-page">
      <div class="bb-page-header">
        <div>
          <span class="bb-page-header__eyebrow">Histórico de movimentações</span>
          <h1>Extrato</h1>
        </div>
      </div>

      @if (carregando()) {
        <bb-loading label="Carregando extrato…"></bb-loading>
      } @else if (erro()) {
        <div class="bb-card bb-empty-state">{{ erro() }}</div>
      } @else if (pagina() && pagina()!.content.length === 0) {
        <div class="bb-card bb-empty-state">Nenhuma movimentação registrada ainda.</div>
      } @else if (pagina(); as p) {
        <div class="bb-card" style="padding: 0;">
          <table mat-table [dataSource]="p.content" style="width: 100%;">
            <ng-container matColumnDef="data">
              <th mat-header-cell *matHeaderCellDef>Data</th>
              <td mat-cell *matCellDef="let mov">{{ mov.data | date: 'dd/MM/yyyy HH:mm' }}</td>
            </ng-container>

            <ng-container matColumnDef="tipo">
              <th mat-header-cell *matHeaderCellDef>Tipo</th>
              <td mat-cell *matCellDef="let mov">
                <span
                  class="bb-status-chip"
                  [class.bb-status-chip--deposito]="mov.tipo === 'DEPOSITO'"
                  [class.bb-status-chip--saque]="mov.tipo === 'SAQUE'"
                  [class.bb-status-chip--pix]="mov.tipo === 'PIX'"
                >{{ mov.tipo | tipoMovimentacao }}</span>
              </td>
            </ng-container>

            <ng-container matColumnDef="descricao">
              <th mat-header-cell *matHeaderCellDef>Descrição</th>
              <td mat-cell *matCellDef="let mov">{{ mov.descricao }}</td>
            </ng-container>

            <ng-container matColumnDef="valor">
              <th mat-header-cell *matHeaderCellDef style="text-align: right;">Valor</th>
              <td mat-cell *matCellDef="let mov" style="text-align: right;">
                <span class="bb-money" [class.bb-money--positive]="mov.tipo === 'DEPOSITO'" [class.bb-money--negative]="mov.tipo !== 'DEPOSITO'">
                  {{ mov.tipo === 'DEPOSITO' ? '+' : '-' }}{{ mov.valor | currency: 'BRL' }}
                </span>
              </td>
            </ng-container>

            <ng-container matColumnDef="saldoAtual">
              <th mat-header-cell *matHeaderCellDef style="text-align: right;">Saldo após</th>
              <td mat-cell *matCellDef="let mov" style="text-align: right;">
                <span class="bb-money">{{ mov.saldoAtual | currency: 'BRL' }}</span>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="colunas"></tr>
            <tr mat-row *matRowDef="let row; columns: colunas;"></tr>
          </table>

          <mat-paginator
            [length]="p.totalElements"
            [pageSize]="p.size"
            [pageIndex]="p.number"
            [pageSizeOptions]="[10, 20, 50]"
            (page)="mudarPagina($event)"
          ></mat-paginator>
        </div>
      }

      <a routerLink="/app/contas/{{ contaId }}" class="bb-muted" style="display: inline-block; margin-top: 16px; font-size: 13px;">
        ← Voltar para a conta
      </a>
    </div>
  `
})
export class Extrato implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly contaService = inject(ContaService);
  private readonly snackBar = inject(MatSnackBar);

  contaId = '';
  readonly colunas = ['data', 'tipo', 'descricao', 'valor', 'saldoAtual'];
  readonly pagina = signal<Page<Movimentacao> | null>(null);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);

  ngOnInit(): void {
    this.contaId = this.route.snapshot.paramMap.get('id')!;
    this.carregar(0, 20);
  }

  mudarPagina(event: PageEvent): void {
    this.carregar(event.pageIndex, event.pageSize);
  }

  private carregar(page: number, size: number): void {
    this.carregando.set(true);
    this.contaService
      .extrato(this.contaId, page, size)
      .pipe(finalize(() => this.carregando.set(false)))
      .subscribe({
        next: (pagina) => this.pagina.set(pagina),
        error: (err) => {
          const mensagem = extrairMensagemErro(err, 'Não foi possível carregar o extrato.');
          this.erro.set(mensagem);
          this.snackBar.open(mensagem, 'Fechar', { duration: 5000, panelClass: 'bb-snackbar-error' });
        }
      });
  }
}
