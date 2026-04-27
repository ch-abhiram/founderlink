import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { InvestmentService } from '../../../core/services/investment.service';
import { Investment } from '../../../core/models/investment.model';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format.pipe';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';

@Component({
  selector: 'app-investments-received',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyFormatPipe, TimeAgoPipe],
  templateUrl: './investments-received.component.html',
  styles: ``
})
export class InvestmentsReceivedComponent implements OnInit {
  startupId!: number;
  investments: Investment[] = [];
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private invService: InvestmentService
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      this.startupId = Number(params.get('id'));
      this.loadInvestments();
    });
  }

  loadInvestments() {
    this.loading = true;
    this.invService.getInvestmentsForStartup(this.startupId).subscribe({
      next: (res) => {
        this.investments = res.sort((a,b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

}
