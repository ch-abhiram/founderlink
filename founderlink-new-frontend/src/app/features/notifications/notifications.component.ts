import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../core/services/notification.service';
import { Notification } from '../../core/models/notification.model';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  template: `
<div class="page-container">
  <div class="page-header animate-fade-up">
    <div><h1 class="section-title">Notifications</h1><p class="section-subtitle">Stay updated on what matters.</p></div>
    <button class="btn-secondary" (click)="markAllAsRead()" *ngIf="unread > 0"><i class="pi pi-check-square"></i> Mark all read</button>
  </div>
  <div *ngIf="loading" class="stack">
    <div class="skeleton" style="height:72px;border-radius:14px" *ngFor="let i of [1,2,3,4,5]"></div>
  </div>
  <div class="empty-state" *ngIf="!loading && notifications.length === 0">
    <div class="empty-icon"><i class="pi pi-bell"></i></div>
    <h3>All caught up!</h3><p>No notifications right now. Check back later.</p>
  </div>
  <div class="notif-list animate-fade-up stagger" *ngIf="!loading && notifications.length > 0">
    <div class="notif-item fl-card" *ngFor="let n of notifications" [class.unread]="!n.read" (click)="markRead(n)">
      <div class="notif-dot" *ngIf="!n.read"></div>
      <div class="notif-icon"><i class="pi" [ngClass]="iconFor(n.type)"></i></div>
      <div class="notif-body">
        <div class="notif-title">{{ n.title || n.type }}</div>
        <div class="notif-msg">{{ n.message }}</div>
        <div class="notif-time">{{ n.createdAt | date:'medium' }}</div>
      </div>
    </div>
  </div>
</div>`,
  styles: [`
    .page-header { display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:24px }
    .stack { display:flex;flex-direction:column;gap:10px }
    .notif-list { display:flex;flex-direction:column;gap:8px }
    .notif-item { display:flex;align-items:flex-start;gap:14px;padding:16px 18px;position:relative;cursor:pointer;transition:border-color var(--transition-fast) }
    .notif-item.unread { border-color:rgba(99,102,241,0.3);background:rgba(99,102,241,0.04) }
    .notif-dot { position:absolute;top:18px;left:-4px;width:8px;height:8px;border-radius:50%;background:var(--accent-primary) }
    .notif-icon { width:38px;height:38px;border-radius:10px;background:rgba(99,102,241,0.1);border:1px solid rgba(99,102,241,0.2);display:flex;align-items:center;justify-content:center;color:var(--accent-primary);font-size:1rem;flex-shrink:0 }
    .notif-body { flex:1 }
    .notif-title { font-weight:600;font-size:0.9rem;margin-bottom:2px }
    .notif-msg { font-size:0.82rem;color:var(--text-secondary);margin-bottom:4px }
    .notif-time { font-size:0.72rem;color:var(--text-muted) }
  `]
})
export class NotificationsComponent implements OnInit {
  notifications: Notification[] = [];
  loading = true;
  get unread(): number { return this.notifications.filter(n => !n.read).length; }

  constructor(private notifSvc: NotificationService) {}

  ngOnInit() {
    this.notifSvc.getNotifications(false).subscribe({
      next: n => { this.notifications = n; this.loading = false; },
      error: () => this.loading = false
    });
  }

  markRead(n: Notification) {
    if (n.read) return;
    this.notifSvc.markAsRead(n.id).subscribe({ next: () => n.read = true, error: () => {} });
  }

  markAllAsRead() {
    this.notifSvc.markAllAsRead().subscribe({ next: () => this.notifications.forEach(n => n.read = true), error: () => {} });
  }

  iconFor(type: string): string {
    const map: Record<string,string> = {
      INVESTMENT: 'pi-dollar', FOLLOW: 'pi-heart', MESSAGE: 'pi-comments',
      STARTUP_APPROVED: 'pi-check-circle', STARTUP_REJECTED: 'pi-times-circle', UPDATE: 'pi-bell'
    };
    return map[type] || 'pi-bell';
  }
}
