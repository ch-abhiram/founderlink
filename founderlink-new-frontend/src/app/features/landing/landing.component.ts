import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { catchError, of } from 'rxjs';
import { Startup } from '../../core/models/startup.model';
import { StartupService } from '../../core/services/startup.service';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss']
})
export class LandingComponent implements OnInit {
  featuredStartup?: Startup;
  stats: Array<{ value: string; label: string }> = [];

  constructor(private startupService: StartupService) {}

  ngOnInit(): void {
    this.startupService.search({ status: 'OPEN' }, 0, 100).pipe(
      catchError(() => of({ content: [], totalElements: 0 }))
    ).subscribe(result => {
      const startups: Startup[] = result.content || [];
      this.featuredStartup = this.pickFeaturedStartup(startups);

      if (!startups.length) {
        this.stats = [];
        return;
      }

      const totalRaised = startups.reduce((sum, startup) => sum + (startup.raisedAmount || 0), 0);
      const totalFollowers = startups.reduce((sum, startup) => sum + (startup.followersCount || 0), 0);
      const fundedCount = startups.filter(startup => (startup.raisedAmount || 0) > 0).length;

      this.stats = [
        { value: this.formatCompact(result.totalElements || startups.length), label: 'Open Startups' },
        { value: this.formatCurrency(totalRaised), label: 'Capital Raised' },
        { value: this.formatCompact(fundedCount), label: 'Funded Startups' },
        { value: this.formatCompact(totalFollowers), label: 'Startup Followers' },
      ];
    });
  }

  features = [
    { icon: 'pi-briefcase',  title: 'Curated Deal Flow',    desc: 'AI-filtered startup listings matched to your thesis - no noise, just signal.' },
    { icon: 'pi-shield',     title: 'Verified Founders',    desc: 'Every founder and co-founder is verified. Trust built into the infrastructure.' },
    { icon: 'pi-chart-line', title: 'Real-Time Tracking',   desc: 'Live portfolio dashboards, cap table snapshots, and investor updates in one place.' },
    { icon: 'pi-comments',   title: 'Direct Messaging',     desc: 'Founders and investors communicate directly - no intermediaries, no friction.' },
    { icon: 'pi-users',      title: 'Team Building',        desc: 'Invite co-founders, set equity terms, and manage your team from day one.' },
    { icon: 'pi-bell',       title: 'Smart Notifications',  desc: 'Get alerted when startups you follow raise rounds or post milestone updates.' },
  ];

  get featuredFundingPct(): number {
    if (!this.featuredStartup?.targetAmount || !this.featuredStartup?.raisedAmount) return 0;
    return Math.min(100, Math.round((this.featuredStartup.raisedAmount / this.featuredStartup.targetAmount) * 100));
  }

  formatAmount(amount = 0): string {
    return this.formatCurrency(amount);
  }

  private pickFeaturedStartup(startups: Startup[]): Startup | undefined {
    return [...startups].sort((a, b) => (b.raisedAmount || 0) - (a.raisedAmount || 0))[0];
  }

  private formatCurrency(amount = 0): string {
    if (amount >= 10000000) return `$${(amount / 1000000).toFixed(1).replace(/\.0$/, '')}M`;
    if (amount >= 10000) return `$${(amount / 1000).toFixed(1).replace(/\.0$/, '')}K`;
    return `$${amount.toLocaleString()}`;
  }

  private formatCompact(value = 0): string {
    if (value >= 1000000) return `${(value / 1000000).toFixed(1).replace(/\.0$/, '')}M`;
    if (value >= 1000) return `${(value / 1000).toFixed(1).replace(/\.0$/, '')}K`;
    return value.toLocaleString();
  }
}
