import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { NotificationService } from '../../../core/services/notification.service';
import { environment } from '../../../../environments/environment';
import { Notification } from '../../../core/models/notification.model';
import { BadgeModule } from 'primeng/badge';
import { OverlayPanelModule } from 'primeng/overlaypanel';
import { TimeAgoPipe } from '../../pipes/time-ago.pipe';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule, RouterLink, BadgeModule, OverlayPanelModule, TimeAgoPipe],
  templateUrl: './notification-bell.component.html',
  styles: ``
})
export class NotificationBellComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  unreadCount = 0;
  private pollSub?: Subscription;

  constructor(
    private notifService: NotificationService,
    private router: Router
  ) {}

  ngOnInit() {
    this.fetchNotifications();
    this.pollSub = interval(environment.notificationPollIntervalMs).subscribe(() => {
      this.fetchNotifications();
    });
  }

  ngOnDestroy() {
    if (this.pollSub) this.pollSub.unsubscribe();
  }

  fetchNotifications() {
    this.notifService.getNotifications(true).subscribe(data => {
      this.notifications = data;
      this.unreadCount = data.length;
    });
  }

  markAsRead(notif: Notification, overlay: any) {
    this.notifService.markAsRead(notif.id).subscribe(() => {
      this.fetchNotifications();
      overlay.hide();
      this.router.navigate(['/notifications']);
    });
  }

  markAllAsRead(overlay: any) {
    this.notifService.markAllAsRead().subscribe(() => {
      this.fetchNotifications();
      overlay.hide();
    });
  }
}
