import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { InvestmentService } from '../../../core/services/investment.service';
import { StartupService } from '../../../core/services/startup.service';
import { UserService } from '../../../core/services/user.service';
import { Investment } from '../../../core/models/investment.model';
import { Startup } from '../../../core/models/startup.model';
import { catchError, of, forkJoin } from 'rxjs';

@Component({
  selector: 'app-investor-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class InvestorDashboardComponent implements OnInit {
  loading = true;
  firstName = '';
  investments: Investment[] = [];
  featured: Startup[] = [];

  get totalDeployed(): number { return this.investments.filter(i=>i.status==='APPROVED' || i.status==='COMPLETED').reduce((s,i)=>s+(i.amount||0),0); }
  get activeCount(): number { return this.investments.filter(i=>i.status==='APPROVED' || i.status==='COMPLETED').length; }
  get pendingCount(): number { return this.investments.filter(i=>i.status==='PENDING').length; }

  constructor(
    private authSvc: AuthService,
    private investSvc: InvestmentService,
    private startupSvc: StartupService,
    private userSvc: UserService
  ) {}

  ngOnInit() {
    const email = this.authSvc.getEmail() || 'Investor';
    this.firstName = email.split('@')[0];

    this.userSvc.getProfile().pipe(catchError(() => of(null))).subscribe(user => {
      const name = (user?.name || '').trim();
      if (name) {
        this.firstName = name.split(' ')[0];
      }
    });

    forkJoin({
      investments: this.investSvc.getMyInvestments().pipe(catchError(()=>of([]))),
      startups: this.startupSvc.search({ status: 'OPEN' }, 0, 6).pipe(catchError(()=>of({content:[]})))
    }).subscribe(({ investments, startups }) => {
      this.investments = investments;
      this.featured = startups.content || [];
      this.loading = false;
    });
  }

  formatAmount(n: number): string {
    if (!n) return '$0';
    if (n >= 1e6) return `$${(n/1e6).toFixed(1)}M`;
    if (n >= 1e3) return `$${(n/1e3).toFixed(0)}K`;
    return `$${n}`;
  }

  progressPct(s: Startup): number {
    if (!s.targetAmount || !s.raisedAmount) return 0;
    return Math.min(100, Math.round((s.raisedAmount/s.targetAmount)*100));
  }
}
