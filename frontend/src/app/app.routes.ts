import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { roleGuard } from './core/guards/role.guard';
import { workspaceGuard } from './core/guards/workspace.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'login',
  },
  {
    path: '',
    loadComponent: () =>
      import('./core/layout/public-layout/public-layout.component').then(
        (m) => m.PublicLayoutComponent,
      ),
    canActivate: [guestGuard],
    children: [
      {
        path: 'login',
        loadComponent: () =>
          import('./features/auth/pages/login/login-page.component').then(
            (m) => m.LoginPageComponent,
          ),
      },
      {
        path: 'register',
        loadComponent: () =>
          import('./features/auth/pages/register/register-page.component').then(
            (m) => m.RegisterPageComponent,
          ),
      },
      {
        path: 'invite/accept',
        loadComponent: () =>
          import('./features/auth/pages/invite-accept/invite-accept-page.component').then(
            (m) => m.InviteAcceptPageComponent,
          ),
      },
      {
        path: 'invite/accept/:token',
        loadComponent: () =>
          import('./features/auth/pages/invite-accept/invite-accept-page.component').then(
            (m) => m.InviteAcceptPageComponent,
          ),
      },
    ],
  },
  {
    path: '',
    loadComponent: () =>
      import('./core/layout/protected-layout/protected-layout.component').then(
        (m) => m.ProtectedLayoutComponent,
      ),
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadChildren: () =>
          import('./features/dashboard/dashboard.routes').then((m) => m.DASHBOARD_ROUTES),
      },
      {
        path: 'master',
        canActivate: [roleGuard(['MASTER'])],
        loadChildren: () => import('./features/master/master.routes').then((m) => m.MASTER_ROUTES),
      },
      {
        path: 'admin',
        canActivate: [workspaceGuard, roleGuard(['ADMIN', 'MASTER'])],
        loadChildren: () => import('./features/admin/admin.routes').then((m) => m.ADMIN_ROUTES),
      },
      {
        path: 'crew',
        canActivate: [workspaceGuard, roleGuard(['CREW', 'ADMIN', 'MASTER'])],
        loadChildren: () => import('./features/crew/crew.routes').then((m) => m.CREW_ROUTES),
      },
    ],
  },
  {
    path: '**',
    redirectTo: 'dashboard',
  },
];
