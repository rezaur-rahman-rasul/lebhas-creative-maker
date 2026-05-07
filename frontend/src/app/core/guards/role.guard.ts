import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

import { UserRole } from '@app/features/auth/models/user.models';
import { CurrentUserStore } from '../auth/current-user.store';

export function roleGuard(allowedRoles: readonly UserRole[]): CanActivateFn {
  return () => {
    const authState = inject(CurrentUserStore);

    if (authState.hasAnyRole(allowedRoles)) {
      return true;
    }

    return inject(Router).createUrlTree(['/dashboard']);
  };
}
