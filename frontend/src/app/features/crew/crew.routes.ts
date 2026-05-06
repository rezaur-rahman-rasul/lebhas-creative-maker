import { Routes } from '@angular/router';

export const CREW_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./crew-home.component').then((m) => m.CrewHomeComponent),
  },
];
