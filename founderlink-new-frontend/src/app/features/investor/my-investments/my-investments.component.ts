import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { InvestmentService } from '../../../core/services/investment.service';
import { Investment } from '../../../core/models/investment.model';

@Component({
  selector: 'app-my-investments',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
<div class="page-container">
  <div class="page-header animate-fade-up">
    <div><h1 class="section-title">My Investments</h1><p class="section-subtitle">Full history of your investment activity.</p></div>
    <a routerLink="/startups" class="btn-primary"><i class="pi pi-plus"></i> New Investment</a>
  </div>
  <div *ngIf="loading" class="skeleton" style="height:400px;border-radius:18px"></div>
  <div *ngIf="!loading" class="table-card glass animate-fade-up">
    <div class="empty-state" *ngIf="investments.length === 0">
      <div class="empty-icon"><i class="pi pi-chart-bar"></i></div><h3>No investments yet</h3><p>Browse startups and start investing.</p>
    </div>
    <table class="fl-table" *ngIf="investments.length > 0">
      <thead><tr><th>Startup</th><th>Amount</th><th>Equity</th><th>Status</th><th>Date</th><th>Receipt</th></tr></thead>
      <tbody>
        <tr *ngFor="let inv of investments">
          <td><a [routerLink]="['/startups', inv.startupId]" style="color:var(--accent-primary);text-decoration:none;font-weight:600">{{ inv.startupName || 'Startup #' + inv.startupId }}</a></td>
          <td><strong style="font-family:var(--font-display)">{{ fmt(inv.amount) }}</strong></td>
          <td>{{ inv.equityPercentage ? inv.equityPercentage + '%' : '—' }}</td>
          <td><span class="badge" [ngClass]="{'badge-success':inv.status==='APPROVED' || inv.status==='COMPLETED','badge-pending':inv.status==='PENDING','badge-rejected':inv.status==='REJECTED' || inv.status==='FAILED'}">{{ inv.status }}</span></td>
          <td style="color:var(--text-muted);font-size:0.82rem">{{ inv.createdAt | date:'mediumDate' }}</td>
          <td><button class="btn-ghost" style="font-size:0.8rem" (click)="download(inv)"><i class="pi pi-download"></i></button></td>
        </tr>
      </tbody>
    </table>
  </div>
</div>`,
  styles: [`.page-header{display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:28px}.table-card{border-radius:var(--radius-xl);overflow:hidden}`]
})
export class MyInvestmentsComponent implements OnInit {
  loading = true; investments: Investment[] = [];
  constructor(private investSvc: InvestmentService) {}
  ngOnInit() { this.investSvc.getMyInvestments().subscribe({ next: r => { this.investments = r; this.loading = false; }, error: () => this.loading = false }); }
  fmt(n: number): string { if (n>=1e6) return `$${(n/1e6).toFixed(1)}M`; if (n>=1e3) return `$${(n/1e3).toFixed(0)}K`; return `$${n}`; }
  download(inv: Investment) {
    const text = `FounderLink Investment Receipt\n${'='.repeat(40)}\nStartup: ${inv.startupName || inv.startupId}\nAmount: ${this.fmt(inv.amount)}\nStatus: ${inv.status}\nDate: ${inv.createdAt}\nInvestor: ${inv.investorEmail}\n`;
    const blob = new Blob([text], { type: 'text/plain' });
    const a = document.createElement('a'); a.href = URL.createObjectURL(blob);
    a.download = `receipt_${inv.id}.txt`; a.click();
  }
}
