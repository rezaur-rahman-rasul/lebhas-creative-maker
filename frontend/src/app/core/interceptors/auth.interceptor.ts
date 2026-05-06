import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { AuthStateService } from '../state/auth-state.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = inject(AuthStateService).accessToken();

  if (!token) {
    return next(request);
  }

  return next(
    request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    }),
  );
};
