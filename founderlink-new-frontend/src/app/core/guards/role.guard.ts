import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const allowed: string[] = route.data['roles'] || [];
  const role = auth.getRole() || '';
  if (allowed.includes(role)) return true;
  const redirects: Record<string,string> = {
    ROLE_FOUNDER: '/founder/dashboard', ROLE_COFOUNDER: '/founder/dashboard',
    ROLE_INVESTOR: '/investor/dashboard', ROLE_ADMIN: '/admin/pending-startups'
  };
  return router.createUrlTree([redirects[role] || '/login']);
};
