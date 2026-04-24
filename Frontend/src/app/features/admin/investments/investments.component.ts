import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { InvestmentService } from '../../../core/services/investment.service';
import { Investment } from '../../../core/models/investment.model';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format.pipe';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';

@Component({
  selector: 'app-investments',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyFormatPipe, TimeAgoPipe],
  template: `
    <div class="max-w-6xl mx-auto flex flex-col gap-6">
      <div class="flex justify-between items-end border-b border-surface-border pb-6">
        <div>
          <h1 class="text-3xl font-bold text-text-primary">Admin Control Center</h1>
          <p class="text-text-muted mt-1">Review and action platform entities. Currently showing <span class="text-primary font-bold">Investments Log</span>.</p>
        </div>
      </div>

      <div class="bg-surface-card border border-surface-border rounded-xl flex">
        <div class="w-64 border-r border-surface-border hidden md:block">
          <div class="p-4 flex flex-col gap-2 font-medium">
            <a routerLink="/admin/pending-startups" class="p-3 hover:bg-surface text-text-muted hover:text-text-primary rounded-lg">Pending Startups</a>
            <a routerLink="/admin/users" class="p-3 hover:bg-surface text-text-muted hover:text-text-primary rounded-lg">Users Management</a>
            <a class="p-3 bg-primary/10 text-primary rounded-lg border-l-4 border-primary">Investments Log</a>
          </div>
        </div>

        <div class="flex-1 min-h-[500px] overflow-x-auto">
          <table class="w-full text-left text-sm text-text-primary">
            <thead class="bg-surface text-text-muted uppercase text-xs">
              <tr>
                <th class="px-6 py-4">Investment</th>
                <th class="px-6 py-4">Startup</th>
                <th class="px-6 py-4">Investor</th>
                <th class="px-6 py-4">Founder</th>
                <th class="px-6 py-4">Amount</th>
                <th class="px-6 py-4">Status</th>
                <th class="px-6 py-4">Created</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-surface-border">
              <tr *ngFor="let investment of investments" class="hover:bg-surface/50 transition-colors">
                <td class="px-6 py-4 font-mono text-xs text-text-muted">#INV-{{ investment.id.toString().padStart(6, '0') }}</td>
                <td class="px-6 py-4">{{ investment.startupName || ('Startup #' + investment.startupId) }}</td>
                <td class="px-6 py-4">{{ investment.investorEmail }}</td>
                <td class="px-6 py-4">{{ investment.founderEmail || 'Unknown' }}</td>
                <td class="px-6 py-4 font-semibold text-emerald-500">{{ investment.amount | currencyFormat }}</td>
                <td class="px-6 py-4">{{ investment.status }}</td>
                <td class="px-6 py-4 text-text-muted">{{ investment.createdAt | timeAgo }}</td>
              </tr>
              <tr *ngIf="loading">
                <td colspan="7" class="px-6 py-12 text-center text-text-muted">Loading investments...</td>
              </tr>
              <tr *ngIf="!loading && investments.length === 0">
                <td colspan="7" class="px-6 py-12 text-center text-text-muted">No investments recorded yet.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `
})
export class InvestmentsComponent implements OnInit {
  investments: Investment[] = [];
  loading = true;

  constructor(private investmentService: InvestmentService) {}

  ngOnInit() {
    this.investmentService.getAllInvestments().subscribe({
      next: (investments) => {
        this.investments = investments.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
