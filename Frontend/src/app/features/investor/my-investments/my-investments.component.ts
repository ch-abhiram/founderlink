import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { InvestmentService } from '../../../core/services/investment.service';
import { Investment } from '../../../core/models/investment.model';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format.pipe';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';

@Component({
  selector: 'app-my-investments',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyFormatPipe, TimeAgoPipe, ButtonModule, TagModule],
  templateUrl: './my-investments.component.html',
  styles: ``
})
export class MyInvestmentsComponent implements OnInit {
  investments: Investment[] = [];
  loading = true;

  constructor(
    private invService: InvestmentService
  ) {}

  ngOnInit() {
    this.invService.getMyInvestments().subscribe({
      next: (res) => {
        this.investments = res.sort((a,b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  downloadReceipt(investment: Investment) {
    const content = [
      'FounderLink Investment Receipt',
      `Receipt ID: INV-${investment.id.toString().padStart(6, '0')}`,
      `Startup: ${investment.startupName || `Startup ${investment.startupId}`}`,
      `Investor: ${investment.investorEmail}`,
      `Founder: ${investment.founderEmail || 'Unknown'}`,
      `Amount: ${investment.amount}`,
      `Status: ${investment.status}`,
      `Created At: ${investment.createdAt}`
    ].join('\n');

    const blob = new Blob([content], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `founderlink-receipt-${investment.id}.txt`;
    link.click();
    URL.revokeObjectURL(url);
  }
}
