import { HttpInterceptorFn } from '@angular/common/http';

/** Attaches the stored JWT to every API request. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('pf_token');
  if (token) {
    req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
  }
  return next(req);
};
