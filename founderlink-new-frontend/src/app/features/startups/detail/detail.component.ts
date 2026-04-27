import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { StartupService } from '../../../core/services/startup.service';
import { InvestmentService } from '../../../core/services/investment.service';
import { TeamService } from '../../../core/services/team.service';
import { MessagingService } from '../../../core/services/messaging.service';
import { AuthService } from '../../../core/services/auth.service';
import { Startup } from '../../../core/models/startup.model';
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
  currentEmail = '';
  role = '';

  investAmount: number | null = null;
  showInvestModal = false;
  investLoading = false;

  teamMembers: any[] = [];
  documents: any[] = [];
  updates: any[] = [];

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
        this.loading = false;
        this.loadExtras(id);
        this.checkFollowing(id);
      },
      error: () => { this.loading = false; this.router.navigate(['/startups']); }
    });
  }

  loadExtras(id: number) {
    this.startupSvc.getUpdates(id).pipe(catchError(() => of([]))).subscribe(u => this.updates = u);
    this.startupSvc.getDocuments(id).pipe(catchError(() => of([]))).subscribe(d => this.documents = d);
    this.teamSvc.getTeamForStartup(id).pipe(catchError(() => of([]))).subscribe(t => this.teamMembers = t);
  }

  checkFollowing(id: number) {
    this.startupSvc.getFollowers(id).pipe(catchError(() => of([] as string[]))).subscribe(f => {
      this.isFollowing = f.includes(this.currentEmail);
    });
  }

  toggleFollow() {
    if (!this.startup) return;
    const req$ = this.isFollowing ? this.startupSvc.unfollow(this.startup.id!) : this.startupSvc.follow(this.startup.id!);
    req$.subscribe({ next: () => { this.isFollowing = !this.isFollowing; }, error: () => {} });
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
}
