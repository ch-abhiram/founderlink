import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { TeamService } from '../../../core/services/team.service';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-team-manage',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, ToastModule],
  providers: [MessageService],
  template: `
<p-toast></p-toast>
<div class="page-container">
  <div class="page-header animate-fade-up">
    <div>
      <button class="btn-ghost" [routerLink]="['/founder/my-startups']"><i class="pi pi-arrow-left"></i> Back</button>
      <h1 class="section-title" style="margin-top:10px">Team Management</h1>
      <p class="section-subtitle">Invite co-founders and manage equity allocation.</p>
    </div>
  </div>
  <!-- Invite form -->
  <div class="invite-card fl-card animate-fade-up">
    <div class="card-head"><i class="pi pi-user-plus"></i> Invite a Team Member</div>
    <div class="invite-row">
      <div class="field flex1"><label class="fl-label">Email Address</label><input class="fl-input" type="email" [(ngModel)]="invite.memberEmail" placeholder="cofounder@example.com" /></div>
      <div class="field w160"><label class="fl-label">Role</label><select class="fl-select" [(ngModel)]="invite.role"><option value="COFOUNDER">Co-Founder</option><option value="ADVISOR">Advisor</option><option value="EMPLOYEE">Employee</option></select></div>
      <div class="field w120"><label class="fl-label">Equity (%)</label><input class="fl-input" type="number" [(ngModel)]="invite.equityShare" placeholder="5" /></div>
      <div class="field"><label class="fl-label">&nbsp;</label><button class="btn-primary" (click)="sendInvite()" [disabled]="inviting"><span *ngIf="!inviting"><i class="pi pi-paper-plane"></i> Invite</span><span *ngIf="inviting">Sending…</span></button></div>
    </div>
  </div>
  <!-- Team list -->
  <div class="section animate-fade-up">
    <div class="section-title" style="margin-bottom:16px">Current Team ({{ members.length }})</div>
    <div *ngIf="loading" class="skeleton" style="height:200px;border-radius:18px"></div>
    <div class="empty-state" *ngIf="!loading && members.length === 0"><div class="empty-icon"><i class="pi pi-users"></i></div><h3>No team members yet</h3><p>Invite co-founders above to grow your team.</p></div>
    <div class="team-table glass" *ngIf="!loading && members.length > 0">
      <table class="fl-table">
        <thead><tr><th>Member</th><th>Role</th><th>Equity</th><th>Status</th><th>Actions</th></tr></thead>
        <tbody>
          <tr *ngFor="let m of members">
            <td><div class="member-cell"><div class="m-av">{{ m.memberEmail?.charAt(0)?.toUpperCase() }}</div>{{ m.memberEmail }}</div></td>
            <td>{{ m.role }}</td>
            <td>{{ m.equityShare }}%</td>
            <td><span class="badge" [ngClass]="{'badge-success':m.status==='ACCEPTED','badge-pending':m.status==='PENDING','badge-rejected':m.status==='REJECTED'}">{{ m.status }}</span></td>
            <td><button class="btn-danger-sm" (click)="removeMember(m)"><i class="pi pi-trash"></i></button></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</div>`,
  styles: [`
    .page-header{margin-bottom:24px}
    .invite-card{padding:22px;margin-bottom:24px}
    .card-head{display:flex;align-items:center;gap:8px;font-weight:700;font-size:0.95rem;margin-bottom:18px;padding-bottom:14px;border-bottom:1px solid var(--border-subtle);color:var(--text-primary);i{color:var(--accent-primary)}}
    .invite-row{display:flex;gap:14px;align-items:flex-end;flex-wrap:wrap}
    .field{display:flex;flex-direction:column}
    .flex1{flex:1;min-width:200px}
    .w160{width:160px}
    .w120{width:120px}
    .section{margin-bottom:24px}
    .team-table{border-radius:var(--radius-xl);overflow:hidden}
    .member-cell{display:flex;align-items:center;gap:10px}
    .m-av{width:30px;height:30px;border-radius:8px;background:linear-gradient(135deg,var(--accent-secondary),var(--accent-primary));display:flex;align-items:center;justify-content:center;font-weight:700;color:#fff;font-size:0.75rem;flex-shrink:0}
    .btn-danger-sm{padding:6px 10px;background:rgba(244,63,94,0.08);border:1px solid rgba(244,63,94,0.2);border-radius:var(--radius-sm);color:var(--accent-rose);cursor:pointer;transition:all var(--transition-fast);&:hover{background:rgba(244,63,94,0.18)}}
  `]
})
export class TeamManageComponent implements OnInit {
  startupId!: number;
  members: any[] = [];
  loading = true;
  inviting = false;
  invite = { memberEmail: '', role: 'COFOUNDER', equityShare: null as number|null };

  constructor(private teamSvc: TeamService, private route: ActivatedRoute, private msg: MessageService) {}

  ngOnInit() {
    this.startupId = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
  }

  load() {
    this.loading = true;
    this.teamSvc.getTeamForStartup(this.startupId).subscribe({
      next: m => { this.members = m; this.loading = false; },
      error: () => this.loading = false
    });
  }

  sendInvite() {
    if (!this.invite.memberEmail) { this.msg.add({ severity: 'warn', summary: 'Enter email' }); return; }
    this.inviting = true;
    this.teamSvc.inviteMember({ startupId: this.startupId, ...this.invite }).subscribe({
      next: () => { this.inviting = false; this.invite = { memberEmail: '', role: 'COFOUNDER', equityShare: null }; this.msg.add({ severity: 'success', summary: 'Invite sent!' }); this.load(); },
      error: err => { this.inviting = false; this.msg.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Failed.' }); }
    });
  }

  removeMember(m: any) {
    this.teamSvc.removeMember(m.id).subscribe({
      next: () => { this.members = this.members.filter(x => x.id !== m.id); this.msg.add({ severity: 'success', summary: 'Member removed.' }); },
      error: () => this.msg.add({ severity: 'error', summary: 'Remove failed.' })
    });
  }
}
