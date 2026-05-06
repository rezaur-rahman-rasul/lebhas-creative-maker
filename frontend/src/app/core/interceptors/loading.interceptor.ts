import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs';

import { LoadingStateService } from '../state/loading-state.service';

export const loadingInterceptor: HttpInterceptorFn = (request, next) => {
  const loading = inject(LoadingStateService);
  loading.start();

  return next(request).pipe(finalize(() => loading.stop()));
};
