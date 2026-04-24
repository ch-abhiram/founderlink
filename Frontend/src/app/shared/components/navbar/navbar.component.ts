import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CommonModule } from '@angular/common';
import { NotificationBellComponent } from '../notification-bell/notification-bell.component';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, CommonModule, NotificationBellComponent],
  templateUrl: './navbar.component.html',
  styles: ``
})
export class NavbarComponent implements OnInit {
  isDark = true;
  isAuthenticated = false;

  constructor(private authService: AuthService) {}

  ngOnInit() {
    this.isAuthenticated = this.authService.isAuthenticated();
    this.initTheme();
  }

  toggleTheme() {
    this.isDark = !this.isDark;
    localStorage.setItem('founderlink_theme', this.isDark ? 'dark' : 'light');
    this.applyTheme();
  }

  private initTheme() {
    const saved = localStorage.getItem('founderlink_theme');
    if (saved) {
      this.isDark = saved === 'dark';
    }
    this.applyTheme();
  }

  private applyTheme() {
    if (this.isDark) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }

  logout() {
    this.authService.logout();
  }
}
