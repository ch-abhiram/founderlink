import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { StartupService } from '../../../core/services/startup.service';
import { InvestmentService } from '../../../core/services/investment.service';
import { TeamService } from '../../../core/services/team.service';
import { MessagingService } from '../../../core/services/messaging.service';
import { AuthService } from '../../../core/services/auth.service';
import { Startup, StartupDocument } from '../../../core/models/startup.model';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { catchError, of } from 'rxjs';

@Component({
  selector: 'app-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, ToastModule],
  providers: [MessageService],
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.scss']
})
export class DetailComponent implements OnInit {
  startup: Startup | null = null;
  loading = true;
  activeTab: 'overview'|'team'|'documents'|'updates' = 'overview';
  isFollowing = false;
  isFounder = false;
  isInvestor = false;
  canManageStartup = false;
  currentEmail = '';
  role = '';

  investAmount: number | null = null;
  showInvestModal = false;
  investLoading = false;

  teamMembers: any[] = [];
  documents: any[] = [];
  updates: any[] = [];
  inviteActionId: number | null = null;
  messageActionEmail = '';

  constructor(
    private route: ActivatedRoute, private router: Router,
    private startupSvc: StartupService, private investSvc: InvestmentService,
    private teamSvc: TeamService, private msgSvc: MessagingService,
    private authSvc: AuthService, private msg: MessageService
  ) {}

  ngOnInit() {
    this.currentEmail = this.authSvc.getEmail() || '';
    this.role = this.authSvc.getRole() || '';
    this.isInvestor = this.role === 'ROLE_INVESTOR';

    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.startupSvc.getById(id).subscribe({
      next: s => {
        this.startup = s;
        this.isFounder = s.founderEmail === this.currentEmail;
        this.canManageStartup = this.isFounder;
        this.loading = false;
        this.loadExtras(id);
        this.checkFollowing(id);
      },
      error: () => { this.loading = false; this.router.navigate(['/startups']); }
    });
  }

  loadExtras(id: number) {
    this.startupSvc.getUpdates(id).pipe(catchError(() => of([]))).subscribe(u => this.updates = u);
    if (this.authSvc.isAuthenticated()) {
      this.startupSvc.getDocuments(id).pipe(catchError(() => of([]))).subscribe(d => this.documents = d);
    } else {
      this.documents = [];
    }
    this.teamSvc.getTeamForStartup(id).pipe(catchError(() => of([]))).subscribe(t => {
      this.teamMembers = t;
      this.canManageStartup = this.isFounder || this.teamMembers.some(member =>
        member.memberEmail === this.currentEmail &&
        member.status === 'ACCEPTED' &&
        ['OWNER', 'ADMIN'].includes((member.permissionLevel || '').toUpperCase())
      );
    });
  }

  checkFollowing(id: number) {
    if (!this.authSvc.isAuthenticated()) {
      this.isFollowing = false;
      return;
    }
    this.startupSvc.getFollowers(id).pipe(catchError(() => of([] as string[]))).subscribe(f => {
      this.isFollowing = f.includes(this.currentEmail);
    });
  }

  toggleFollow() {
    if (!this.startup) return;
    const req$ = this.isFollowing ? this.startupSvc.unfollow(this.startup.id!) : this.startupSvc.follow(this.startup.id!);
    req$.subscribe({ next: () => { this.isFollowing = !this.isFollowing; }, error: () => {} });
  }

  canRespondToInvite(member: any): boolean {
    return member?.memberEmail === this.currentEmail && member?.status === 'PENDING';
  }

  canMessageTeamMember(member: any): boolean {
    return this.canManageStartup &&
      member?.memberEmail &&
      member.memberEmail !== this.currentEmail &&
      member.status === 'ACCEPTED';
  }

  messageTeamMember(member: any) {
    if (!this.startup?.id || !this.canMessageTeamMember(member)) return;
    this.messageActionEmail = member.memberEmail;
    this.msgSvc.sendMessage({
      startupId: this.startup.id,
      participantEmail: member.memberEmail,
      content: `Hi, let's coordinate on ${this.startup.name}.`
    }).subscribe({
      next: () => {
        this.messageActionEmail = '';
        this.router.navigate(['/messages']);
      },
      error: err => {
        this.messageActionEmail = '';
        this.msg.add({severity:'error', summary:'Message failed', detail: err?.error?.message || 'Unable to open conversation.'});
      }
    });
  }

  respondToInvite(member: any, status: 'ACCEPTED' | 'REJECTED') {
    if (!member?.id || this.inviteActionId === member.id) return;
    this.inviteActionId = member.id;
    this.teamSvc.updateInviteStatus(member.id, status).subscribe({
      next: updated => {
        const finalize = () => {
          this.inviteActionId = null;
          this.teamMembers = this.teamMembers.map(item => item.id === updated.id ? updated : item);
          this.canManageStartup = this.isFounder || this.teamMembers.some(item =>
            item.memberEmail === this.currentEmail &&
            item.status === 'ACCEPTED' &&
            ['OWNER', 'ADMIN'].includes((item.permissionLevel || '').toUpperCase())
          );
          this.msg.add({
            severity: 'success',
            summary: status === 'ACCEPTED' ? 'Invite accepted' : 'Invite declined',
            detail: status === 'ACCEPTED' ? 'You have joined the startup team.' : 'The invite has been declined.'
          });
        };

        if (status === 'ACCEPTED' && updated.role === 'COFOUNDER') {
          this.authSvc.refresh().subscribe({ next: finalize, error: finalize });
          return;
        }

        finalize();
      },
      error: err => {
        this.inviteActionId = null;
        this.msg.add({severity:'error', summary:'Error', detail: err?.error?.message || 'Unable to update invite.'});
      }
    });
  }

  openInvestModal() { this.showInvestModal = true; this.investAmount = null; }
  closeInvestModal() { this.showInvestModal = false; }

  proceedWithInvestment() {
    if (!this.investAmount || this.investAmount <= 0) { this.msg.add({severity:'warn', summary:'Enter amount'}); return; }
    this.investLoading = true;
    this.investSvc.createInvestment({ startupId: this.startup!.id, amount: this.investAmount }).subscribe({
      next: () => {
        this.investLoading = false;
        this.closeInvestModal();
        this.msg.add({
          severity: 'success',
          summary: 'Investment submitted',
          detail: 'Your request is pending admin verification.'
        });
      },
      error: err => { this.investLoading = false; this.msg.add({severity:'error', summary:'Error', detail: err?.error?.message || 'Investment failed.'}); }
    });
  }

  messageFounder() {
    this.msgSvc.sendMessage({ startupId: this.startup!.id, content: "Hello, I'm interested in your startup!" }).subscribe({
      next: () => this.router.navigate(['/messages']),
      error: () => this.router.navigate(['/messages'])
    });
  }

  get progressPct(): number {
    if (!this.startup?.targetAmount || !this.startup?.raisedAmount) return 0;
    return Math.min(100, Math.round((this.startup.raisedAmount / this.startup.targetAmount) * 100));
  }

  formatAmount(n: number): string {
    if (!n) return '—';
    if (n >= 1e6) return `$${(n/1e6).toFixed(1)}M`;
    if (n >= 1e3) return `$${(n/1e3).toFixed(0)}K`;
    return `$${n}`;
  }

  openDocument(document: StartupDocument) {
    if (!document.url) return;
    if (!this.startupSvc.isUploadedDocument(document)) {
      window.open(document.url, '_blank');
      return;
    }

    const viewer = window.open('', '_blank');
    this.startupSvc.downloadDocument(document).subscribe({
      next: blob => {
        const blobUrl = URL.createObjectURL(blob);
        if (viewer) {
          viewer.location.href = blobUrl;
        } else {
          window.open(blobUrl, '_blank');
        }
        setTimeout(() => URL.revokeObjectURL(blobUrl), 60000);
      },
      error: () => {
        viewer?.close();
        this.msg.add({severity:'error', summary:'Document missing', detail:'The saved file was not found on the server. Please upload it again.'});
      }
    });
  }
}
