import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { StartupService } from '../../../core/services/startup.service';
import { AuthService } from '../../../core/services/auth.service';
import { TeamService } from '../../../core/services/team.service';
import { Startup } from '../../../core/models/startup.model';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

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
  role = '';

  constructor(private startupSvc: StartupService, private authSvc: AuthService, private teamSvc: TeamService,
              private msg: MessageService, private confirm: ConfirmationService) {}

  ngOnInit() {
    this.email = this.authSvc.getEmail() || '';
    this.role = this.authSvc.getRole() || '';
    this.load();
  }

  load() {
    this.loading = true;
    forkJoin({
      startups: this.startupSvc.search({}, 0, 100).pipe(catchError(() => of({ content: [] }))),
      invites: this.teamSvc.getMyInvites().pipe(catchError(() => of([])))
    }).subscribe({
      next: ({ startups, invites }) => {
        const managedStartupIds = new Set(
          invites
            .filter((invite: any) => invite.status === 'ACCEPTED' && ['OWNER', 'ADMIN'].includes((invite.permissionLevel || '').toUpperCase()))
            .map((invite: any) => invite.startupId)
        );
        this.startups = (startups.content || []).filter((s: Startup) => s.founderEmail === this.email || managedStartupIds.has(s.id));
        this.loading = false;
      },
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

  isOwner(s: Startup): boolean {
    return s.founderEmail === this.email;
  }

  formatAmount(n: number): string {
    if (!n) return '—';
    if (n >= 1e6) return `$${(n/1e6).toFixed(1)}M`;
    if (n >= 1e3) return `$${(n/1e3).toFixed(0)}K`;
    return `$${n}`;
  }
}
