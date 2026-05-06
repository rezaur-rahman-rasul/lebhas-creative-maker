import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { normalizeHttpError } from '../api/http-error';
import { NotificationStateService } from '../state/notification-state.service';

export const errorInterceptor: HttpInterceptorFn = (request, next) => {
  const notifications = inject(NotificationStateService);

  return next(request).pipe(
    catchError((error: unknown) => {
      const normalized = normalizeHttpError(error);
      notifications.error('Request failed', normalized.message);
      return throwError(() => error);
    }),
  );
};
