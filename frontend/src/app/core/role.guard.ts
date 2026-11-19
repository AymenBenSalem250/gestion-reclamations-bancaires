import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from './auth.service';

export const roleGuard: CanActivateFn = (route) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  const expectedRoles: string[] = route.data['roles'] || [];
  const userRoles: string[] = authService.getRoles();

  // No roles stored → treat as unauthenticated, force login
  if (!userRoles || userRoles.length === 0) {
  router.navigate(['/login']);
  return false;
  }

  const hasRole = expectedRoles.some(role =>
  userRoles.some(userRole =>
    userRole === role || userRole === `ROLE_${role}`
   )
  );

  if (!hasRole) {
    // Prevent infinite loop: only redirect to /dashboard if not already targeting it
    if (route.routeConfig?.path !== 'dashboard') {
      router.navigate(['/dashboard']);
    } else {
      // User has a token but no role that grants dashboard access → logout
      authService.logout();
    }
    return false;
  }

  return true;
};

