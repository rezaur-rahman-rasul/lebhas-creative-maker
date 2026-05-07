import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

import { CurrentUserStore } from '../auth/current-user.store';

export const authGuard: CanActivateFn = (_route, state) => {
  const authState = inject(CurrentUserStore);

  if (authState.isAuthenticated()) {
    return true;
  }

  return inject(Router).createUrlTree(['/login'], {
    queryParams: {
      returnUrl: state.url,
    },
  });
};
