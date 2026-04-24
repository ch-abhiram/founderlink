import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Observable, throwError, catchError, switchMap } from 'rxjs';

let isRefreshing = false;

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> => {
  const authService = inject(AuthService);
  const skipAuthUrls = ['/auth/login', '/auth/register', '/auth/refresh', '/auth/verify', '/auth/verify-otp', '/auth/resend-otp'];
  const accessToken = authService.getAccessToken();
  const refreshToken = authService.getRefreshToken();
  const shouldSkip = skipAuthUrls.some(url => req.url.includes(url));
  let finalReq = req;

  if (!shouldSkip) {
    if (accessToken) {
      finalReq = req.clone({
        setHeaders: { Authorization: `Bearer ${accessToken}` }
      });
    }
  }

  return next(finalReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !shouldSkip && accessToken && refreshToken) {
        if (!isRefreshing) {
          isRefreshing = true;
          return authService.refresh().pipe(
            switchMap((res) => {
              isRefreshing = false;
              const refreshedReq = req.clone({
                setHeaders: { Authorization: `Bearer ${res.accessToken}` }
              });
              return next(refreshedReq);
            }),
            catchError((refreshErr) => {
              isRefreshing = false;
              return throwError(() => refreshErr);
            })
          );
        }
      }
      return throwError(() => error);
    })
  );
};
