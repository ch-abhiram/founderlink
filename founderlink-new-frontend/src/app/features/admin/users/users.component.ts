import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../../core/services/user.service';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule, ToastModule],
  providers: [MessageService],
  template: `
<p-toast></p-toast>
<div class="page-container">
  <div class="page-header animate-fade-up">
    <div><h1 class="section-title">All Users</h1><p class="section-subtitle">Manage platform users and roles.</p></div>
    <div class="search-wrap"><i class="pi pi-search"></i><input class="fl-input" type="text" [(ngModel)]="search" placeholder="Search by email…" /></div>
  </div>
  <div *ngIf="loading" class="skeleton" style="height:400px;border-radius:18px"></div>
  <div class="table-card glass animate-fade-up" *ngIf="!loading">
    <table class="fl-table">
      <thead><tr><th>Email</th><th>Role</th><th>Change Role</th></tr></thead>
      <tbody>
        <tr *ngFor="let u of filtered">
          <td><div class="user-row"><div class="user-av">{{ u.email?.charAt(0)?.toUpperCase() }}</div>{{ u.email }}</div></td>
          <td><span class="role-tag" [attr.data-role]="u.role">{{ u.role }}</span></td>
          <td>
            <div class="role-change">
              <select class="fl-select sm" [(ngModel)]="u._newRole">
                <option *ngFor="let r of roles" [value]="r.value">{{ r.label }}</option>
              </select>
              <button class="btn-ghost sm" (click)="changeRole(u)" [disabled]="u._newRole === u.role"><i class="pi pi-check"></i></button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</div>`,
  styles: [`
    .page-header{display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:24px}
    .search-wrap{position:relative;display:flex;align-items:center;i{position:absolute;left:12px;color:var(--text-muted);font-size:0.9rem}.fl-input{padding-left:36px;min-width:240px}}
    .table-card{border-radius:var(--radius-xl);overflow:hidden}
    .user-row{display:flex;align-items:center;gap:10px}
    .user-av{width:30px;height:30px;border-radius:8px;background:linear-gradient(135deg,var(--accent-secondary),var(--accent-primary));display:flex;align-items:center;justify-content:center;font-weight:700;color:#fff;font-size:0.75rem;flex-shrink:0}
    .role-tag{padding:3px 10px;border-radius:999px;font-size:0.72rem;font-weight:600;letter-spacing:0.04em;background:rgba(99,102,241,0.1);border:1px solid rgba(99,102,241,0.2);color:var(--accent-primary)}
    .role-change{display:flex;gap:8px;align-items:center}
    .fl-select.sm{min-width:150px;padding:7px 10px;font-size:0.82rem}
    .btn-ghost.sm{padding:7px 10px;font-size:0.85rem}
  `]
})
export class AdminUsersComponent implements OnInit {
  users: any[] = [];
  loading = true;
  search = '';
  roles = [
    { value: 'ROLE_FOUNDER', label: 'Founder' },
    { value: 'ROLE_COFOUNDER', label: 'Co-Founder' },
    { value: 'ROLE_INVESTOR', label: 'Investor' },
    { value: 'ROLE_ADMIN', label: 'Admin' }
  ];

  get filtered() {
    if (!this.search) return this.users;
    return this.users.filter(u => u.email?.toLowerCase().includes(this.search.toLowerCase()));
  }

  constructor(private userSvc: UserService, private msg: MessageService) {}

  ngOnInit() {
    this.userSvc.getAllUsers().subscribe({
      next: u => { this.users = u.map((x: any) => ({ ...x, _newRole: x.role })); this.loading = false; },
      error: () => this.loading = false
    });
  }

  changeRole(u: any) {
    this.userSvc.changeRole(u.email, u._newRole).subscribe({
      next: () => { u.role = u._newRole; this.msg.add({ severity: 'success', summary: 'Role updated', detail: `${u.email} is now ${u._newRole}` }); },
      error: err => this.msg.add({ severity: 'error', summary: 'Error', detail: err?.error?.message || 'Failed.' })
    });
  }
}
