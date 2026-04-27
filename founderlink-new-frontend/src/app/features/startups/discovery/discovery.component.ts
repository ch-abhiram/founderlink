import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { StartupService } from '../../../core/services/startup.service';
import { Startup } from '../../../core/models/startup.model';

@Component({
  selector: 'app-discovery',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './discovery.component.html',
  styleUrls: ['./discovery.component.scss']
})
export class DiscoveryComponent implements OnInit {
  startups: Startup[] = [];
  loading = true;
  total = 0; page = 0; size = 12; totalPages = 0;
  filters = { category: '', stage: '', currentRound: '', search: '' };
  categories = ['','FinTech','HealthTech','EdTech','E-Commerce','SaaS','DeepTech','AI','CleanTech','Consumer','Other'];
  rounds = ['','Pre-Seed','Seed','Series A','Series B','Series C','Growth'];
  stages = ['','Idea','MVP','Early Traction','Growth','Scale'];

  constructor(private startupSvc: StartupService) {}

  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    const f: any = {};
    f.status = 'OPEN';
    if (this.filters.category) f.category = this.filters.category;
    if (this.filters.stage) f.stage = this.filters.stage;
    if (this.filters.currentRound) f.currentRound = this.filters.currentRound;
    this.startupSvc.search(f, this.page, this.size).subscribe({
      next: r => { this.startups = r.content || []; this.total = r.totalElements || 0; this.totalPages = r.totalPages || 1; this.loading = false; },
      error: () => this.loading = false
    });
  }

  applyFilters() { this.page = 0; this.load(); }
  clearFilters() { this.filters = { category: '', stage: '', currentRound: '', search: '' }; this.applyFilters(); }
  prevPage() { if (this.page > 0) { this.page--; this.load(); } }
  nextPage() { if (this.page < this.totalPages-1) { this.page++; this.load(); } }

  get filtered(): Startup[] {
    if (!this.filters.search) return this.startups;
    const q = this.filters.search.toLowerCase();
    return this.startups.filter(s => s.name?.toLowerCase().includes(q) || s.description?.toLowerCase().includes(q));
  }

  formatAmount(n: number): string {
    if (!n) return '—';
    if (n >= 1e6) return `$${(n/1e6).toFixed(1)}M`;
    if (n >= 1e3) return `$${(n/1e3).toFixed(0)}K`;
    return `$${n}`;
  }

  progressPct(s: Startup): number {
    if (!s.targetAmount || !s.raisedAmount) return 0;
    return Math.min(100, Math.round((s.raisedAmount/s.targetAmount)*100));
  }
}
