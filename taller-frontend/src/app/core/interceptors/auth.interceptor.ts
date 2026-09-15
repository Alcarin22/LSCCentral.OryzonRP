import { HttpInterceptorFn } from '@angular/common/http';

import { environment } from '../../../environments/environment';

export const authInterceptor: HttpInterceptorFn = (
  request,
  next
) => {

  const token =
    sessionStorage.getItem('auth_token');

  const esPeticionBackend =
    request.url.startsWith(
      environment.backendUrl
    );

  if (!token || !esPeticionBackend) {
    return next(request);
  }

  const requestAutenticada =
    request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });

  return next(requestAutenticada);
};