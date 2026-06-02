import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { CoupleStore } from './couple';

export const authGuard: CanActivateFn = () => {
  if (localStorage.getItem('pf_token')) return true;
  return inject(Router).createUrlTree(['/login']);
};

/** Requires both a session and an active couple; otherwise routes to onboarding. */
export const coupleGuard: CanActivateFn = async () => {
  const store = inject(CoupleStore);
  const router = inject(Router);
  if (!localStorage.getItem('pf_token')) return router.createUrlTree(['/login']);
  const couple = store.couple() ?? (await store.load());
  return couple ? true : router.createUrlTree(['/onboarding']);
};

/** The inverse: only reachable while NOT yet paired. */
export const onboardingGuard: CanActivateFn = async () => {
  const store = inject(CoupleStore);
  const router = inject(Router);
  if (!localStorage.getItem('pf_token')) return router.createUrlTree(['/login']);
  const couple = store.couple() ?? (await store.load());
  return couple ? router.createUrlTree(['/home']) : true;
};
