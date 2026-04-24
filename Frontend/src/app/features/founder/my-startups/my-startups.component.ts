import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { StartupService } from '../../../core/services/startup.service';
import { AuthService } from '../../../core/services/auth.service';
import { Startup } from '../../../core/models/startup.model';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format.pipe';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ConfirmDialogModule } from 'primeng/confirmdialog';

@Component({
  selector: 'app-my-startups',
  standalone: true,
  imports: [CommonModule, RouterLink, TagModule, ButtonModule, CurrencyFormatPipe, ConfirmDialogModule],
  providers: [ConfirmationService],
  templateUrl: './my-startups.component.html',
  styles: ``
})
export class MyStartupsComponent implements OnInit {
  startups: Startup[] = [];
  loading = true;

  constructor(
    private startupService: StartupService,
    private authService: AuthService,
    private confirm: ConfirmationService,
    private msg: MessageService
  ) {}

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    const email = this.authService.getEmail();
    if (!email) return;

    this.startupService.search({}, 0, 100).subscribe({
      next: (res) => {
        this.startups = res.content.filter(s => s.founderEmail === email);
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  confirmDelete(id: number, event: Event) {
    this.confirm.confirm({
      target: event.target as EventTarget,
      message: 'Are you sure you want to delete this startup?',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.startupService.delete(id).subscribe({
          next: () => {
            this.msg.add({severity:'success', summary:'Deleted', detail:'Startup removed'});
            this.loadData();
          }
        });
      }
    });
  }
}
