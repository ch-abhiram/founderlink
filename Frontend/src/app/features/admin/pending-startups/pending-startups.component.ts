import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { StartupService } from '../../../core/services/startup.service';
import { Startup } from '../../../core/models/startup.model';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format.pipe';
import { TimeAgoPipe } from '../../../shared/pipes/time-ago.pipe';
import { ButtonModule } from 'primeng/button';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-pending-startups',
  standalone: true,
  imports: [CommonModule, RouterLink, CurrencyFormatPipe, TimeAgoPipe, ButtonModule],
  templateUrl: './pending-startups.component.html',
  styles: ``
})
export class PendingStartupsComponent implements OnInit {
  startups: Startup[] = [];
  loading = true;

  constructor(
    private startupService: StartupService,
    private msg: MessageService
  ) {}

  ngOnInit() {
    this.loadStartups();
  }

  loadStartups() {
    this.loading = true;
    this.startupService.search({status: 'PENDING'}, 0, 100).subscribe({
      next: (res) => {
        this.startups = res.content.filter(s => s.status === 'PENDING').sort((a,b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  approve(id: number) {
    this.startupService.approve(id).subscribe(() => {
      this.msg.add({severity:'success', summary:'Approved', detail:'Startup is now properly Open for investment.'});
      this.loadStartups();
    });
  }

  reject(id: number) {
    this.startupService.reject(id).subscribe(() => {
      this.msg.add({severity:'info', summary:'Rejected', detail:'Startup rejected.'});
      this.loadStartups();
    });
  }
}
