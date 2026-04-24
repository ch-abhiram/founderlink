import { Routes } from '@angular/router';
import { AppLayoutComponent } from './layouts/app-layout/app-layout.component';
import { AuthLayoutComponent } from './layouts/auth-layout/auth-layout.component';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  {
    path: '',
    component: AppLayoutComponent,
    children: [
      { path: '', loadComponent: () => import('./features/landing/landing.component').then(m => m.LandingComponent) },
      { path: 'startups', loadComponent: () => import('./features/startups/discovery/discovery.component').then(m => m.DiscoveryComponent) },
      { path: 'startups/:id', loadComponent: () => import('./features/startups/detail/detail.component').then(m => m.DetailComponent) },
      // Protected generic routes
      { path: 'profile', canActivate: [authGuard], loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent) },
      { path: 'messages', canActivate: [authGuard], loadComponent: () => import('./features/messaging/inbox/inbox.component').then(m => m.InboxComponent) },
      { path: 'notifications', canActivate: [authGuard], loadComponent: () => import('./features/notifications/notifications.component').then(m => m.NotificationsComponent) },
      
      // Founder routes
      { 
        path: 'founder', 
        canActivate: [authGuard, roleGuard], 
        data: { roles: ['ROLE_FOUNDER', 'ROLE_COFOUNDER'] },
        children: [
          { path: 'dashboard', loadComponent: () => import('./features/founder/dashboard/dashboard.component').then(m => m.DashboardComponent) },
          { path: 'my-startups', loadComponent: () => import('./features/founder/my-startups/my-startups.component').then(m => m.MyStartupsComponent) },
          { path: 'startups/new', loadComponent: () => import('./features/founder/startup-manage/startup-manage.component').then(m => m.StartupManageComponent) },
          { path: 'startups/:id/edit', loadComponent: () => import('./features/founder/startup-manage/startup-manage.component').then(m => m.StartupManageComponent) },
          { path: 'startups/:id/team', loadComponent: () => import('./features/founder/team-manage/team-manage.component').then(m => m.TeamManageComponent) },
          { path: 'startups/:id/investments', loadComponent: () => import('./features/founder/investments-received/investments-received.component').then(m => m.InvestmentsReceivedComponent) },
          { path: 'startups/:id/updates', loadComponent: () => import('./features/founder/updates/updates.component').then(m => m.UpdatesComponent) },
          { path: 'startups/:id/documents', loadComponent: () => import('./features/founder/documents/documents.component').then(m => m.DocumentsComponent) },
        ]
      },

      // Investor routes
      {
        path: 'investor',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['ROLE_INVESTOR'] },
        children: [
          { path: 'dashboard', loadComponent: () => import('./features/investor/dashboard/dashboard.component').then(m => m.DashboardComponent) },
          { path: 'my-investments', loadComponent: () => import('./features/investor/my-investments/my-investments.component').then(m => m.MyInvestmentsComponent) },
        ]
      },

      // Admin routes
      {
        path: 'admin',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['ROLE_ADMIN'] },
        children: [
          { path: 'pending-startups', loadComponent: () => import('./features/admin/pending-startups/pending-startups.component').then(m => m.PendingStartupsComponent) },
          { path: 'users', loadComponent: () => import('./features/admin/users/users.component').then(m => m.UsersComponent) },
          { path: 'investments', loadComponent: () => import('./features/admin/investments/investments.component').then(m => m.InvestmentsComponent) }
        ]
      }
    ]
  },
  {
    path: '',
    component: AuthLayoutComponent,
    children: [
      { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
      { path: 'register', loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent) },
      { path: 'verify-otp', loadComponent: () => import('./features/auth/verify-otp/verify-otp.component').then(m => m.VerifyOtpComponent) }
    ]
  },
  { path: '**', redirectTo: '' }
];
