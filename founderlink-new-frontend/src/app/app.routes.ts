import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { AppLayoutComponent } from './layouts/app-layout/app-layout.component';

export const routes: Routes = [
  // Public
  { path: '', loadComponent: () => import('./features/landing/landing.component').then(m => m.LandingComponent) },
  { path: 'login',      loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
  { path: 'register',   loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent) },
  { path: 'verify-otp', loadComponent: () => import('./features/auth/verify-otp/verify-otp.component').then(m => m.VerifyOtpComponent) },
  { path: 'forgot-password', loadComponent: () => import('./features/auth/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent) },
  { path: 'reset-password', loadComponent: () => import('./features/auth/reset-password/reset-password.component').then(m => m.ResetPasswordComponent) },

  // App shell (authenticated)
  {
    path: '',
    component: AppLayoutComponent,
    canActivate: [authGuard],
    children: [
      // Founder
      { path: 'founder/dashboard',           canActivate: [roleGuard], data: { roles: ['ROLE_FOUNDER','ROLE_COFOUNDER'] }, loadComponent: () => import('./features/founder/dashboard/dashboard.component').then(m => m.FounderDashboardComponent) },
      { path: 'founder/my-startups',         canActivate: [roleGuard], data: { roles: ['ROLE_FOUNDER','ROLE_COFOUNDER'] }, loadComponent: () => import('./features/founder/my-startups/my-startups.component').then(m => m.MyStartupsComponent) },
      { path: 'founder/startups/new',        canActivate: [roleGuard], data: { roles: ['ROLE_FOUNDER'] },                  loadComponent: () => import('./features/founder/startup-manage/startup-manage.component').then(m => m.StartupManageComponent) },
      { path: 'founder/startups/:id/edit',   canActivate: [roleGuard], data: { roles: ['ROLE_FOUNDER','ROLE_COFOUNDER'] }, loadComponent: () => import('./features/founder/startup-manage/startup-manage.component').then(m => m.StartupManageComponent) },
      { path: 'founder/startups/:id/team',   canActivate: [roleGuard], data: { roles: ['ROLE_FOUNDER','ROLE_COFOUNDER'] }, loadComponent: () => import('./features/founder/team-manage/team-manage.component').then(m => m.TeamManageComponent) },
      { path: 'founder/startups/:id/updates', canActivate: [roleGuard], data: { roles: ['ROLE_FOUNDER','ROLE_COFOUNDER'] }, loadComponent: () => import('./features/founder/updates/updates.component').then(m => m.UpdatesComponent) },
      { path: 'founder/startups/:id/documents', canActivate: [roleGuard], data: { roles: ['ROLE_FOUNDER','ROLE_COFOUNDER'] }, loadComponent: () => import('./features/founder/documents/documents.component').then(m => m.DocumentsComponent) },

      // Investor
      { path: 'investor/dashboard',          canActivate: [roleGuard], data: { roles: ['ROLE_INVESTOR'] }, loadComponent: () => import('./features/investor/dashboard/dashboard.component').then(m => m.InvestorDashboardComponent) },
      { path: 'investor/my-investments',     canActivate: [roleGuard], data: { roles: ['ROLE_INVESTOR'] }, loadComponent: () => import('./features/investor/my-investments/my-investments.component').then(m => m.MyInvestmentsComponent) },

      // Admin
      { path: 'admin/pending-startups',      canActivate: [roleGuard], data: { roles: ['ROLE_ADMIN'] }, loadComponent: () => import('./features/admin/pending-startups/pending-startups.component').then(m => m.PendingStartupsComponent) },
      { path: 'admin/users',                 canActivate: [roleGuard], data: { roles: ['ROLE_ADMIN'] }, loadComponent: () => import('./features/admin/users/users.component').then(m => m.AdminUsersComponent) },
      { path: 'admin/investments',           canActivate: [roleGuard], data: { roles: ['ROLE_ADMIN'] }, loadComponent: () => import('./features/admin/investments/investments.component').then(m => m.AdminInvestmentsComponent) },

      // Shared
      { path: 'startups',       loadComponent: () => import('./features/startups/discovery/discovery.component').then(m => m.DiscoveryComponent) },
      { path: 'startups/:id',   loadComponent: () => import('./features/startups/detail/detail.component').then(m => m.DetailComponent) },
      { path: 'messages',       loadComponent: () => import('./features/messaging/inbox/inbox.component').then(m => m.InboxComponent) },
      { path: 'notifications',  loadComponent: () => import('./features/notifications/notifications.component').then(m => m.NotificationsComponent) },
      { path: 'profile',        loadComponent: () => import('./features/profile/profile.component').then(m => m.ProfileComponent) },
    ]
  },

  // 404
  { path: '**', loadComponent: () => import('./features/not-found/not-found.component').then(m => m.NotFoundComponent) }
];
