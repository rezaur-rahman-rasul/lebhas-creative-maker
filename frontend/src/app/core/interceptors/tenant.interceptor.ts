import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { AuthStateService } from '../state/auth-state.service';

export const tenantInterceptor: HttpInterceptorFn = (request, next) => {
  const workspaceId = inject(AuthStateService).workspaceId();

  if (!workspaceId) {
    return next(request);
  }

  return next(
    request.clone({
      setHeaders: {
        'X-Workspace-ID': workspaceId,
      },
    }),
  );
};
