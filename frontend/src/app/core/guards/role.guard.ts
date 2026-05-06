import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

import { UserRole } from '@app/shared/models/user-role.model';
import { AuthStateService } from '../state/auth-state.service';

export function roleGuard(allowedRoles: readonly UserRole[]): CanActivateFn {
  return () => {
    const authState = inject(AuthStateService);

    if (authState.hasAnyRole(allowedRoles)) {
      return true;
    }

    return inject(Router).createUrlTree(['/dashboard']);
  };
}
