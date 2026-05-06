import { HttpInterceptorFn } from '@angular/common/http';

import { createCorrelationId } from '@app/shared/utils/create-correlation-id';

export const correlationIdInterceptor: HttpInterceptorFn = (request, next) =>
  next(
    request.clone({
      setHeaders: {
        'X-Correlation-ID': createCorrelationId(),
      },
    }),
  );
