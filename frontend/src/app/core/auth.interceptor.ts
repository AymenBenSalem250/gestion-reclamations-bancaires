import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const authService = inject(AuthService);

  // ✅ Récupération correcte du token
  const token = authService.getToken();

  let authReq = req;

  // ✅ Ajouter Authorization seulement si token existe
  if (token && token !== 'null' && token !== 'undefined') {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {

      // ✅ Si token expiré ou invalide → logout
      if (error.status === 401) {
        authService.logout();
      }

      return throwError(() => error);
    })
  );
};