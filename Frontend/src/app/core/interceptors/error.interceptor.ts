import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MessageService } from 'primeng/api';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const messageService = inject(MessageService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'Something went wrong';
      
      if (error.error && error.error.message) {
        errorMessage = error.error.message;
      } else if (error.message) {
        errorMessage = error.message;
      }

      // We skip 401 here to let authInterceptor handle it quietly if it can
      if (error.status !== 401) {
        messageService.add({ severity: 'error', summary: 'Error', detail: errorMessage, life: 5000 });
      }

      return throwError(() => error);
    })
  );
};
