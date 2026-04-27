import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { StartupService } from '../../../core/services/startup.service';
import { Startup } from '../../../core/models/startup.model';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-pending-startups',
  standalone: true,
  imports: [CommonModule, RouterLink, ToastModule],
  providers: [MessageService],
  templateUrl: './pending-startups.component.html',
  styleUrls: ['./pending-startups.component.scss']
})
export class PendingStartupsComponent implements OnInit {
  startups: Startup[] = [];
  loading = true;
  processing: Record<number, boolean> = {};

  constructor(private startupSvc: StartupService, private msg: MessageService) {}

  ngOnInit() { this.load(); }

  load() {
    this.loading = true;
    this.startupSvc.getPending().subscribe({
      next: s => { this.startups = s; this.loading = false; },
      error: () => this.loading = false
    });
  }

  approve(s: Startup) {
    this.processing[s.id!] = true;
    this.startupSvc.approve(s.id!).subscribe({
      next: () => { this.msg.add({ severity: 'success', summary: 'Approved', detail: `${s.name} is now live.` }); this.startups = this.startups.filter(x => x.id !== s.id); this.processing[s.id!] = false; },
      error: err => { this.msg.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Action failed.' }); this.processing[s.id!] = false; }
    });
  }

  reject(s: Startup) {
    this.processing[s.id!] = true;
    this.startupSvc.reject(s.id!).subscribe({
      next: () => { this.msg.add({ severity: 'info', summary: 'Rejected', detail: `${s.name} has been rejected.` }); this.startups = this.startups.filter(x => x.id !== s.id); this.processing[s.id!] = false; },
      error: err => { this.msg.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Action failed.' }); this.processing[s.id!] = false; }
    });
  }

  formatAmount(n: number): string {
    if (!n) return '—';
    if (n >= 1e6) return `$${(n/1e6).toFixed(1)}M`;
    if (n >= 1e3) return `$${(n/1e3).toFixed(0)}K`;
    return `$${n}`;
  }
}
