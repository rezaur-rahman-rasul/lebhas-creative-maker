import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

import { CurrentUserStore } from '../auth/current-user.store';
import { defaultAuthenticatedRoute } from '../auth/permissions';

export const guestGuard: CanActivateFn = () => {
  const authState = inject(CurrentUserStore);

  if (!authState.isAuthenticated()) {
    return true;
  }

  return inject(Router).createUrlTree([defaultAuthenticatedRoute()]);
};
