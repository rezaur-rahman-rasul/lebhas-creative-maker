import { Routes } from '@angular/router';

import { RoleSectionPageComponent } from '@app/features/shared/role-section-page/role-section-page.component';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./admin-home.component').then((m) => m.AdminHomeComponent),
  },
  {
    path: 'team',
    component: RoleSectionPageComponent,
    data: {
      section: {
        eyebrow: 'Workspace',
        title: 'Team',
        description: 'Team access, invitations, and role visibility sit behind the admin boundary.',
        badgeLabel: 'ADMIN',
        badgeTone: 'brand',
        emptyIcon: 'users',
        emptyTitle: 'Team management foundation',
        emptyDescription: 'Invitation and user-management workflows can expand from this route.',
        highlights: [
          { title: 'Crew invitations', description: 'Invite acceptance flow is already wired into auth.' },
          { title: 'RBAC visibility', description: 'Admin and crew navigation stays scoped to workspace access.' },
          { title: 'Session awareness', description: 'Current user state is tracked with signals and refresh support.' },
        ],
      },
    },
  },
  {
    path: 'brand-profile',
    component: RoleSectionPageComponent,
    data: {
      section: {
        eyebrow: 'Workspace',
        title: 'Brand Profile',
        description: 'Brand context will plug into the authenticated workspace shell here later.',
        badgeLabel: 'ADMIN',
        badgeTone: 'brand',
        emptyIcon: 'badge-check',
        emptyTitle: 'Brand profile placeholder',
        emptyDescription: 'Brand setup stays intentionally outside the Day 2 auth scope.',
        highlights: [
          { title: 'Workspace identity', description: 'Tenant headers already travel with protected API calls.' },
          { title: 'Scoped access', description: 'Admin users remain locked to their active workspace.' },
          { title: 'Future readiness', description: 'This route is ready for future business domain UI.' },
        ],
      },
    },
  },
  {
    path: 'assets',
    component: RoleSectionPageComponent,
    data: {
      section: {
        eyebrow: 'Workspace',
        title: 'Assets',
        description: 'Asset operations will connect later without changing the access shell.',
        badgeLabel: 'ADMIN',
        badgeTone: 'brand',
        emptyIcon: 'image',
        emptyTitle: 'Assets are out of scope',
        emptyDescription: 'Day 2 leaves upload and media workflows for later implementation days.',
        highlights: [
          { title: 'Protected access', description: 'Assets routes already sit behind auth and workspace guards.' },
          { title: 'Tenant awareness', description: 'Workspace context is ready to scope future media APIs.' },
          { title: 'Clean expansion path', description: 'The route surface is prepared without extra placeholder logic.' },
        ],
      },
    },
  },
  {
    path: 'creatives',
    component: RoleSectionPageComponent,
    data: {
      section: {
        eyebrow: 'Workspace',
        title: 'Creatives',
        description: 'Creative generation UI remains a future module, but the workspace shell is ready.',
        badgeLabel: 'ADMIN',
        badgeTone: 'brand',
        emptyIcon: 'wand-sparkles',
        emptyTitle: 'Creative generation comes later',
        emptyDescription: 'Day 2 stops at auth, RBAC, and tenant foundations.',
        highlights: [
          { title: 'Role protection', description: 'Only permitted roles can reach creative routes.' },
          { title: 'Session continuity', description: 'Refresh token handling is already prepared for longer sessions.' },
          { title: 'Workspace scope', description: 'Tenant-aware headers are already attached to requests.' },
        ],
      },
    },
  },
  {
    path: 'billing',
    component: RoleSectionPageComponent,
    data: {
      section: {
        eyebrow: 'Workspace',
        title: 'Billing',
        description: 'Billing remains a future module, but navigation and route ownership are defined.',
        badgeLabel: 'ADMIN',
        badgeTone: 'brand',
        emptyIcon: 'credit-card',
        emptyTitle: 'Billing placeholder',
        emptyDescription: 'Payment and billing UI are explicitly out of scope for Day 2.',
        highlights: [
          { title: 'Access shell', description: 'Admin-only navigation keeps workspace billing isolated.' },
          { title: 'Session policy', description: 'Logout and refresh token flows are already wired.' },
          { title: 'Future integration', description: 'The route can accept billing UI later without auth rewrites.' },
        ],
      },
    },
  },
];
