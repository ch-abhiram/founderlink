import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { InvestmentService } from '../../../core/services/investment.service';
import { Investment } from '../../../core/models/investment.model';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-admin-investments',
  standalone: true,
  imports: [CommonModule, RouterLink, ToastModule],
  providers: [MessageService],
  template: `
<p-toast></p-toast>
<div class="page-container">
  <div class="page-header animate-fade-up">
    <div><h1 class="section-title">Investment Verification</h1><p class="section-subtitle">Approve investor commitments before they appear to founders.</p></div>
    <div class="summary-pills">
      <div class="pill green"><i class="pi pi-dollar"></i> Total: {{ fmt(total) }}</div>
      <div class="pill indigo"><i class="pi pi-list"></i> {{ investments.length }} records</div>
    </div>
  </div>
  <div *ngIf="loading" class="skeleton" style="height:500px;border-radius:18px"></div>
  <div class="table-card glass animate-fade-up" *ngIf="!loading">
    <table class="fl-table">
      <thead><tr><th>#</th><th>Investor</th><th>Startup</th><th>Amount</th><th>Status</th><th>Date</th><th>Actions</th></tr></thead>
      <tbody>
        <tr *ngFor="let inv of investments; let i = index">
          <td style="color:var(--text-muted);font-size:0.8rem">{{ i+1 }}</td>
          <td><div class="email-cell">{{ inv.investorEmail }}</div></td>
          <td><a [routerLink]="['/startups', inv.startupId]" style="color:var(--accent-primary);text-decoration:none;font-weight:600">{{ inv.startupName || '#'+inv.startupId }}</a></td>
          <td><strong style="font-family:var(--font-display)">{{ fmt(inv.amount) }}</strong></td>
          <td><span class="badge" [ngClass]="{'badge-success':inv.status==='APPROVED' || inv.status==='COMPLETED','badge-pending':inv.status==='PENDING','badge-rejected':inv.status==='REJECTED' || inv.status==='FAILED'}">{{ inv.status }}</span></td>
          <td style="color:var(--text-muted);font-size:0.82rem">{{ inv.createdAt | date:'mediumDate' }}</td>
          <td>
            <div class="actions" *ngIf="inv.status === 'PENDING'">
              <button class="btn-approve" (click)="verify(inv, 'APPROVED')">Approve</button>
              <button class="btn-reject" (click)="verify(inv, 'REJECTED')">Reject</button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</div>`,
  styles: [`
    .page-header{display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:24px}
    .summary-pills{display:flex;gap:10px}
    .pill{display:flex;align-items:center;gap:6px;padding:7px 14px;border-radius:999px;font-size:0.82rem;font-weight:600}
    .pill.green{background:rgba(16,185,129,0.1);border:1px solid rgba(16,185,129,0.25);color:var(--accent-green)}
    .pill.indigo{background:rgba(99,102,241,0.1);border:1px solid rgba(99,102,241,0.2);color:var(--accent-primary)}
    .table-card{border-radius:var(--radius-xl);overflow:hidden}
    .email-cell{font-size:0.82rem;max-width:180px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
    .actions{display:flex;gap:8px}
    .btn-approve,.btn-reject{padding:6px 10px;border-radius:var(--radius-sm);font-size:0.78rem;font-weight:700;border:1px solid;cursor:pointer;background:transparent}
    .btn-approve{color:var(--accent-green);border-color:rgba(16,185,129,0.35)}
    .btn-reject{color:var(--accent-rose);border-color:rgba(244,63,94,0.35)}
  `]
})
export class AdminInvestmentsComponent implements OnInit {
  investments: Investment[] = [];
  loading = true;
  get total(): number { return this.investments.filter(i=>i.status==='APPROVED' || i.status==='COMPLETED').reduce((s,i)=>s+(i.amount||0),0); }
  constructor(private investSvc: InvestmentService, private msg: MessageService) {}
  ngOnInit() { this.load(); }
  load() { this.investSvc.getAllInvestments().subscribe({ next: r => { this.investments = r; this.loading = false; }, error: () => this.loading = false }); }
  verify(inv: Investment, status: 'APPROVED' | 'REJECTED') {
    this.investSvc.updateStatus(inv.id!, status).subscribe({
      next: updated => {
        Object.assign(inv, updated);
        this.msg.add({ severity: status === 'APPROVED' ? 'success' : 'info', summary: `Investment ${status.toLowerCase()}` });
      },
      error: err => this.msg.add({ severity: 'error', summary: 'Action failed', detail: err?.error?.message || 'Unable to update investment.' })
    });
  }
  fmt(n: number): string { if (n>=1e6) return `$${(n/1e6).toFixed(1)}M`; if (n>=1e3) return `$${(n/1e3).toFixed(0)}K`; return `$${n}`; }
}
