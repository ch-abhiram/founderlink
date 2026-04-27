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

  constructor(private authService: AuthService, private notifService: NotificationService) {}

  ngOnInit() {
    this.subs.push(this.authService.isAuthenticated$.subscribe(v => {
      this.isAuthenticated = v;
      if (v) this.startPolling();
    }));
  }

  startPolling() {
    const poll$ = interval(environment.notificationPollIntervalMs).pipe(
      startWith(0),
      switchMap(() => this.notifService.getNotifications(true))
    ).subscribe(ns => this.unreadCount = ns.length);
    this.subs.push(poll$);
  }

  onToggle() { this.toggleSidebar.emit(); }
  ngOnDestroy() { this.subs.forEach(s => s.unsubscribe()); }
}
