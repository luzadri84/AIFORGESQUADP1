import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { AuthService } from './auth.service';
import { isOwnApi } from './api-path';
export const authInterceptor: HttpInterceptorFn=(request,next) => {
  // Only this application's relative API paths receive credentials; never absolute or protocol-relative URLs.
  if (!isOwnApi(request.url)) return next(request);
  return next(request.clone({setHeaders:inject(AuthService).headers()}));
};
