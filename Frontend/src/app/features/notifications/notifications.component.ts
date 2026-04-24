import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../core/services/notification.service';
import { Notification } from '../../core/models/notification.model';
import { TimeAgoPipe } from '../../shared/pipes/time-ago.pipe';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, TimeAgoPipe, ButtonModule],
  templateUrl: './notifications.component.html',
  styles: ``
})
export class NotificationsComponent implements OnInit {
  notifications: Notification[] = [];
  loading = true;

  constructor(private notifService: NotificationService) {}

  ngOnInit() {
    this.loadNotifications();
  }

  loadNotifications() {
    this.loading = true;
    this.notifService.getNotifications().subscribe(n => {
      this.notifications = n;
      this.loading = false;
    });
  }

  markAllAsRead() {
    this.notifService.markAllAsRead().subscribe(() => {
      this.loadNotifications();
    });
  }

  getIcon(type: string) {
    if (type === 'INVESTMENT') return 'pi-dollar text-emerald-500 bg-emerald-500/10';
    if (type === 'TEAM') return 'pi-users text-primary bg-primary/10';
    return 'pi-info-circle text-info bg-info/10';
  }
}
