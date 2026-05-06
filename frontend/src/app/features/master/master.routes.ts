import { Routes } from '@angular/router';

export const MASTER_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./master-home.component').then((m) => m.MasterHomeComponent),
  },
];
