import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { StartupService } from '../../../core/services/startup.service';
import { AuthService } from '../../../core/services/auth.service';
import { Startup } from '../../../core/models/startup.model';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';

@Component({
  selector: 'app-my-startups',
  standalone: true,
  imports: [CommonModule, RouterLink, ToastModule, ConfirmDialogModule],
  providers: [MessageService, ConfirmationService],
  templateUrl: './my-startups.component.html',
  styleUrls: ['./my-startups.component.scss']
})
export class MyStartupsComponent implements OnInit {
  loading = true;
  startups: Startup[] = [];
  email = '';

  constructor(private startupSvc: StartupService, private authSvc: AuthService,
              private msg: MessageService, private confirm: ConfirmationService) {}

  ngOnInit() {
    this.email = this.authSvc.getEmail() || '';
    this.load();
  }

  load() {
    this.loading = true;
    this.startupSvc.search({}, 0, 100).subscribe({
      next: r => { this.startups = (r.content||[]).filter((s:Startup)=>s.founderEmail===this.email); this.loading = false; },
      error: () => this.loading = false
    });
  }

  deleteStartup(s: Startup) {
    this.confirm.confirm({
      message: `Delete "${s.name}"? This cannot be undone.`,
      accept: () => {
        this.startupSvc.delete(s.id!).subscribe({
          next: () => { this.msg.add({severity:'success', summary:'Deleted'}); this.load(); },
          error: () => this.msg.add({severity:'error', summary:'Error', detail:'Delete failed.'})
        });
      }
    });
  }

  formatAmount(n: number): string {
    if (!n) return '—';
    if (n >= 1e6) return `$${(n/1e6).toFixed(1)}M`;
    if (n >= 1e3) return `$${(n/1e3).toFixed(0)}K`;
    return `$${n}`;
  }
}
