import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { Subscription } from 'rxjs';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles: string[];
  badge?: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit, OnDestroy {
  @Input() collapsed = false;
  @Input() mobileOpen = false;

  role: string | null = null;
  email: string | null = null;
  private sub!: Subscription;

  allNavItems: NavItem[] = [
    // Founder
    { label: 'Dashboard',        icon: 'pi-th-large',      route: '/founder/dashboard',          roles: ['ROLE_FOUNDER','ROLE_COFOUNDER'] },
    { label: 'My Startups',      icon: 'pi-briefcase',     route: '/founder/my-startups',        roles: ['ROLE_FOUNDER','ROLE_COFOUNDER'] },
    { label: 'Create Startup',   icon: 'pi-plus-circle',   route: '/founder/startups/new',       roles: ['ROLE_FOUNDER'] },
    // Investor
    { label: 'Dashboard',        icon: 'pi-th-large',      route: '/investor/dashboard',         roles: ['ROLE_INVESTOR'] },
    { label: 'My Investments',   icon: 'pi-chart-bar',     route: '/investor/my-investments',    roles: ['ROLE_INVESTOR'] },
    // Admin
    { label: 'Pending Startups', icon: 'pi-clock',         route: '/admin/pending-startups',     roles: ['ROLE_ADMIN'] },
    { label: 'All Users',        icon: 'pi-users',         route: '/admin/users',                roles: ['ROLE_ADMIN'] },
    { label: 'Investments Log',  icon: 'pi-dollar',        route: '/admin/investments',          roles: ['ROLE_ADMIN'] },
    // Shared
    { label: 'Discover',         icon: 'pi-compass',       route: '/startups',                   roles: ['ROLE_FOUNDER','ROLE_COFOUNDER','ROLE_INVESTOR','ROLE_ADMIN'] },
    { label: 'Messages',         icon: 'pi-comments',      route: '/messages',                   roles: ['ROLE_FOUNDER','ROLE_COFOUNDER','ROLE_INVESTOR'] },
    { label: 'Notifications',    icon: 'pi-bell',          route: '/notifications',              roles: ['ROLE_FOUNDER','ROLE_COFOUNDER','ROLE_INVESTOR','ROLE_ADMIN'] },
    { label: 'Profile',          icon: 'pi-user',          route: '/profile',                    roles: ['ROLE_FOUNDER','ROLE_COFOUNDER','ROLE_INVESTOR','ROLE_ADMIN'] },
  ];

  get navItems(): NavItem[] {
    if (!this.role) return [];
    return this.allNavItems.filter(n => n.roles.includes(this.role!));
  }

  get roleLabel(): string {
    const map: Record<string,string> = {
      ROLE_FOUNDER: 'Founder', ROLE_COFOUNDER: 'Co-Founder',
      ROLE_INVESTOR: 'Investor', ROLE_ADMIN: 'Administrator'
    };
    return map[this.role || ''] || '';
  }

  get avatarInitials(): string {
    return (this.email || 'U').substring(0, 2).toUpperCase();
  }

  constructor(private authService: AuthService) {}

  ngOnInit() {
    this.sub = this.authService.role$.subscribe(r => {
      this.role = r;
      this.email = this.authService.getEmail();
    });
  }

  ngOnDestroy() { this.sub?.unsubscribe(); }

  logout() { this.authService.logout(); }
}
