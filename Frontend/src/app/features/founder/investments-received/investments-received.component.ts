import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { InvestmentService } from '../../../core/services/investment.service';
import { Investment } from '../../../core/models/investment.model';
import { MessageService } from 'primeng/api';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format.pipe';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-investments-received',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyFormatPipe, TimeAgoPipe, ButtonModule],
  templateUrl: './investments-received.component.html',
  styles: ``
})
export class InvestmentsReceivedComponent implements OnInit {
  startupId!: number;
  investments: Investment[] = [];
  loading = true;

  constructor(
    private route: ActivatedRoute,
    private invService: InvestmentService,
    private msg: MessageService
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

  approve(id: number) {
    this.invService.updateStatus(id, 'APPROVED').subscribe(() => {
      this.msg.add({severity:'success', summary:'Approved', detail:'Investment request approved.'});
      this.loadInvestments();
    });
  }

  reject(id: number) {
    this.invService.updateStatus(id, 'REJECTED').subscribe(() => {
      this.msg.add({severity:'info', summary:'Rejected', detail:'Investment request rejected.'});
      this.loadInvestments();
    });
  }
}
