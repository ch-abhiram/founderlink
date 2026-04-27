import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { StartupService } from '../../../core/services/startup.service';
import { InvestmentService } from '../../../core/services/investment.service';
import { Startup } from '../../../core/models/startup.model';
import { Investment } from '../../../core/models/investment.model';
import { forkJoin, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-founder-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class FounderDashboardComponent implements OnInit {
  loading = true;
  email = '';
  firstName = '';
  startups: Startup[] = [];
  investments: Investment[] = [];

  get totalRaised(): number { return this.investments.filter(i=>i.status==='APPROVED' || i.status==='COMPLETED').reduce((s,i)=>s+(i.amount||0),0); }
  get pendingCount(): number { return this.investments.filter(i=>i.status==='PENDING').length; }
  get followerCount(): number { return this.startups.reduce((s,st)=> s+(st.followersCount||0),0); }

  constructor(private authService: AuthService, private startupSvc: StartupService, private investSvc: InvestmentService) {}

  ngOnInit() {
    this.email = this.authService.getEmail() || '';
    this.firstName = this.email.split('@')[0];

    this.startupSvc.search({ founderEmail: this.email }, 0, 100).pipe(
      catchError(()=>of({content:[]})),
      switchMap(startups => {
        this.startups = (startups.content || []).filter((s:Startup) => s.founderEmail === this.email);
        const investmentRequests = this.startups
          .filter(s => !!s.id)
          .map(s => this.investSvc.getInvestmentsForStartup(s.id!).pipe(catchError(()=>of([]))));
        return investmentRequests.length ? forkJoin(investmentRequests) : of([]);
      })
    ).subscribe(investmentGroups => {
      this.investments = (investmentGroups as Investment[][]).flat();
      this.loading = false;
    });
  }

  formatCurrency(n: number): string {
    if (n >= 1e6) return `$${(n/1e6).toFixed(1)}M`;
    if (n >= 1e3) return `$${(n/1e3).toFixed(0)}K`;
    return `$${n}`;
  }
}
