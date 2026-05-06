import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

import { AuthStateService } from '../state/auth-state.service';

export const authGuard: CanActivateFn = (_route, state) => {
  const authState = inject(AuthStateService);

  if (authState.isAuthenticated()) {
    return true;
  }

  return inject(Router).createUrlTree(['/login'], {
    queryParams: {
      returnUrl: state.url,
    },
  });
};
