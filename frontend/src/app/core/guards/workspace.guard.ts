import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

import { CurrentUserStore } from '@app/core/auth/current-user.store';
import { NotificationStateService } from '@app/core/state/notification-state.service';

export const workspaceGuard: CanActivateFn = () => {
  const authStore = inject(CurrentUserStore);

  if (authStore.currentRole() === 'MASTER') {
    return true;
  }

  if (authStore.activeWorkspaceId()) {
    return true;
  }

  inject(NotificationStateService).info(
    'Workspace required',
    'Select a workspace before opening this area.',
  );

  return inject(Router).createUrlTree(['/dashboard']);
};
