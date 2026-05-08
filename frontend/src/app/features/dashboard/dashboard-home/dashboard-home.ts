import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AuthStateService } from '@app/core/state/auth-state.service';
import { BadgeComponent } from '@app/shared/components/badge/badge';
import { ButtonComponent } from '@app/shared/components/button/button';
import { CardComponent } from '@app/shared/components/card/card';
import { EmptyStateComponent } from '@app/shared/components/empty-state/empty-state';
import { IconComponent } from '@app/shared/components/icon/icon';
import { PageHeaderComponent } from '@app/shared/components/page-header/page-header';

@Component({
  selector: 'app-dashboard-home',
  standalone: true,
  imports: [
    RouterLink,
    BadgeComponent,
    ButtonComponent,
    CardComponent,
    EmptyStateComponent,
    IconComponent,
    PageHeaderComponent,
  ],
  templateUrl: './dashboard-home.html',
  styleUrl: './dashboard-home.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardHomeComponent {
  private readonly auth = inject(AuthStateService);

  protected readonly description = computed(() => {
    const user = this.auth.currentUser();
    if (!user) {
      return 'Workspace shell is ready.';
    }

    if (!user.workspaceName) {
      return `Your ${user.role.toLowerCase()} session is active and ready for protected workflows.`;
    }

    return `${user.workspaceName} is ready for ${user.role.toLowerCase()} workflows.`;
  });

  protected readonly primaryRoute = computed(() => {
    const role = this.auth.currentRole();
    return role === 'MASTER' ? '/master' : role === 'CREW' ? '/crew' : '/admin';
  });

  protected readonly metrics = [
    { label: 'Credits ready', value: '0', icon: 'credit-card' },
    { label: 'Assets uploaded', value: '0', icon: 'image' },
    { label: 'Campaigns', value: '0', icon: 'folder-kanban' },
    { label: 'Team seats', value: '1', icon: 'users' },
  ];

  protected readonly readiness = [
    { label: 'Tenant context headers', state: 'Prepared' },
    { label: 'JWT interceptor chain', state: 'Prepared' },
    { label: 'Role-aware navigation', state: 'Prepared' },
    { label: 'Backend API envelope', state: 'Prepared' },
  ];
}
