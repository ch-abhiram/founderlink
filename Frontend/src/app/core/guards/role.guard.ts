import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const expectedRoles: string[] = route.data?.['roles'] || [];
  const currentRole = authService.getRole();

  if (currentRole && expectedRoles.includes(currentRole)) {
    return true;
  }

  // Redirect based on role if no match
  if (currentRole === 'ROLE_FOUNDER' || currentRole === 'ROLE_COFOUNDER') {
    return router.createUrlTree(['/founder/dashboard']);
  } else if (currentRole === 'ROLE_INVESTOR') {
    return router.createUrlTree(['/investor/dashboard']);
  } else if (currentRole === 'ROLE_ADMIN') {
    return router.createUrlTree(['/admin/pending-startups']);
  }
  
  return router.createUrlTree(['/login']);
};
