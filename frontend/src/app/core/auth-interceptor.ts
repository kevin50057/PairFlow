import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, from, switchMap, throwError } from 'rxjs';
import { Auth } from './auth';

const TOKEN_KEY = 'pf_token';

/** Attaches JWT to every request; on 401, attempts a silent token refresh then retries once. */
export const authInterceptor: HttpInterceptorFn = (req: HttpRequest<unknown>, next: HttpHandlerFn) => {
  const auth = inject(Auth);

  const withToken = (r: HttpRequest<unknown>) => {
    const token = localStorage.getItem(TOKEN_KEY);
    return token ? r.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : r;
  };

  // Skip refresh/auth endpoints to avoid infinite loops
  if (req.url.includes('/auth/refresh') || req.url.includes('/auth/login') || req.url.includes('/auth/register')) {
    return next(req);
  }

  return next(withToken(req)).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status !== 401) return throwError(() => err);
      return from(auth.refreshTokens()).pipe(
        switchMap(refreshed => {
          if (!refreshed) return throwError(() => err);
          return next(withToken(req));
        }),
      );
    }),
  );
};
