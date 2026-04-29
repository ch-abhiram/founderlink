import { Component, OnInit, OnDestroy, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { Subscription, interval } from 'rxjs';
import { switchMap, startWith } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit, OnDestroy {
  @Output() toggleSidebar = new EventEmitter<void>();
  unreadCount = 0;
  isAuthenticated = false;
  private subs: Subscription[] = [];
  private pollSub?: Subscription;

  constructor(private authService: AuthService, private notifService: NotificationService) {}

  ngOnInit() {
    this.subs.push(this.authService.isAuthenticated$.subscribe(v => {
      this.isAuthenticated = v;
      if (!v) {
        this.unreadCount = 0;
        this.pollSub?.unsubscribe();
        this.pollSub = undefined;
        return;
      }
      this.startPolling();
    }));
  }

  startPolling() {
    this.pollSub?.unsubscribe();
    this.pollSub = interval(environment.notificationPollIntervalMs).pipe(
      startWith(0),
      switchMap(() => this.notifService.getNotifications(true))
    ).subscribe(ns => this.unreadCount = ns.length);
  }

  onToggle() { this.toggleSidebar.emit(); }
  ngOnDestroy() { this.pollSub?.unsubscribe(); this.subs.forEach(s => s.unsubscribe()); }
}
