import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap, throwError, BehaviorSubject, filter, take } from 'rxjs';

const refreshing$ = new BehaviorSubject<boolean>(false);

export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  const authSvc = inject(AuthService);
  const token = authSvc.getAccessToken();
  const authed = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(authed).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status !== 401) return throwError(() => err);

      if (refreshing$.getValue()) {
        return refreshing$.pipe(
          filter(v => !v), take(1),
          switchMap(() => {
            const t = authSvc.getAccessToken();
            return next(req.clone({ setHeaders: { Authorization: `Bearer ${t}` } }));
          })
        );
      }

      refreshing$.next(true);
      return authSvc.refresh().pipe(
        switchMap(res => {
          refreshing$.next(false);
          return next(req.clone({ setHeaders: { Authorization: `Bearer ${res.accessToken}` } }));
        }),
        catchError(e => {
          refreshing$.next(false);
          authSvc.clearTokens();
          return throwError(() => e);
        })
      );
    })
  );
};
