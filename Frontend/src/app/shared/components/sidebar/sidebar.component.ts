import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CommonModule } from '@angular/common';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  roles: string[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './sidebar.component.html',
  styles: ``
})
export class SidebarComponent implements OnInit {
  role: string | null = null;
  navItems: NavItem[] = [
    { label: 'Dashboard', icon: 'pi-th-large', route: '/founder/dashboard', roles: ['ROLE_FOUNDER', 'ROLE_COFOUNDER'] },
    { label: 'My Startups', icon: 'pi-briefcase', route: '/founder/my-startups', roles: ['ROLE_FOUNDER', 'ROLE_COFOUNDER'] },
    
    { label: 'Dashboard', icon: 'pi-th-large', route: '/investor/dashboard', roles: ['ROLE_INVESTOR'] },
    { label: 'My Investments', icon: 'pi-chart-line', route: '/investor/my-investments', roles: ['ROLE_INVESTOR'] },
    
    { label: 'Pending Startups', icon: 'pi-clock', route: '/admin/pending-startups', roles: ['ROLE_ADMIN'] },
    { label: 'All Users', icon: 'pi-users', route: '/admin/users', roles: ['ROLE_ADMIN'] },
    
    { label: 'Messages', icon: 'pi-envelope', route: '/messages', roles: ['ROLE_FOUNDER', 'ROLE_COFOUNDER', 'ROLE_INVESTOR'] },
    { label: 'Profile', icon: 'pi-user', route: '/profile', roles: ['ROLE_FOUNDER', 'ROLE_COFOUNDER', 'ROLE_INVESTOR', 'ROLE_ADMIN'] }
  ];

  filteredNavItems: NavItem[] = [];

  constructor(private authService: AuthService) {}

  ngOnInit() {
    this.role = this.authService.getRole();
    if (this.role) {
      this.filteredNavItems = this.navItems.filter(item => item.roles.includes(this.role!));
    }
  }
}
