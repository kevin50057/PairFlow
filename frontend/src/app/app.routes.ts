import { Routes } from '@angular/router';
import { coupleGuard, onboardingGuard } from './core/guards';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'home' },
  { path: 'login', loadComponent: () => import('./pages/auth/login').then((m) => m.LoginPage) },
  { path: 'register', loadComponent: () => import('./pages/auth/register').then((m) => m.RegisterPage) },
  {
    path: 'onboarding',
    canActivate: [onboardingGuard],
    loadComponent: () => import('./pages/auth/onboarding').then((m) => m.OnboardingPage),
  },
  {
    path: '',
    canActivate: [coupleGuard],
    loadComponent: () => import('./pages/shell/shell').then((m) => m.ShellPage),
    children: [
      { path: 'home', loadComponent: () => import('./pages/home/home').then((m) => m.HomePage) },
      { path: 'todos', loadComponent: () => import('./pages/todos/todos').then((m) => m.TodosPage) },
      { path: 'todos/:id', loadComponent: () => import('./pages/todos/todo-detail').then((m) => m.TodoDetailPage) },
      { path: 'calendar', loadComponent: () => import('./pages/calendar/calendar').then((m) => m.CalendarPage) },
      { path: 'memories', loadComponent: () => import('./pages/memories/memories').then((m) => m.MemoriesPage) },
      { path: 'us', loadComponent: () => import('./pages/us/us').then((m) => m.UsPage) },
      { path: 'us/mood', loadComponent: () => import('./pages/us/mood').then((m) => m.MoodPage) },
      { path: 'us/notes', loadComponent: () => import('./pages/us/notes').then((m) => m.NotesPage) },
      { path: 'us/questions', loadComponent: () => import('./pages/us/questions').then((m) => m.QuestionsPage) },
      { path: 'us/wishlist', loadComponent: () => import('./pages/us/wishlist').then((m) => m.WishlistPage) },
      { path: 'us/finance', loadComponent: () => import('./pages/us/finance').then((m) => m.FinancePage) },
      { path: 'us/dates', loadComponent: () => import('./pages/us/dates').then((m) => m.DatesPage) },
      { path: 'us/repair', loadComponent: () => import('./pages/us/repair').then((m) => m.RepairPage) },
      { path: 'us/ai', loadComponent: () => import('./pages/us/ai').then((m) => m.AiPage) },
      { path: 'us/notifications', loadComponent: () => import('./pages/us/notifications').then((m) => m.NotificationsPage) },
      { path: 'us/settings', loadComponent: () => import('./pages/us/settings').then((m) => m.SettingsPage) },
    ],
  },
  { path: '**', redirectTo: 'home' },
];
