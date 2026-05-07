import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AuthStateService } from '@app/core/state/auth-state.service';
import { BadgeComponent } from '@app/shared/components/badge/badge.component';
import { ButtonComponent } from '@app/shared/components/button/button.component';
import { CardComponent } from '@app/shared/components/card/card.component';
import { EmptyStateComponent } from '@app/shared/components/empty-state/empty-state.component';
import { IconComponent } from '@app/shared/components/icon/icon.component';
import { PageHeaderComponent } from '@app/shared/components/page-header/page-header.component';

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
  template: `
    <app-page-header
      eyebrow="Workspace"
      title="Dashboard"
      [description]="description()"
    >
      <app-button variant="secondary" icon="languages">English / Bangla</app-button>
      <app-button icon="plus">New brief</app-button>
    </app-page-header>

    <section class="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
      @for (metric of metrics; track metric.label) {
        <app-card>
          <div class="flex items-start justify-between gap-3">
            <div>
              <p class="text-sm text-muted">{{ metric.label }}</p>
              <p class="mt-2 text-2xl font-semibold tracking-normal text-ink">{{ metric.value }}</p>
            </div>
            <div class="grid h-10 w-10 place-items-center rounded-lg bg-brand-50 text-brand-700">
              <app-icon [name]="metric.icon" [size]="20" />
            </div>
          </div>
        </app-card>
      }
    </section>

    <section class="mt-6 grid gap-4 xl:grid-cols-[1.25fr_0.75fr]">
      <app-card>
        <div class="flex items-center justify-between gap-3">
          <div>
            <h2 class="text-base font-semibold text-ink">Workspace readiness</h2>
            <p class="mt-1 text-sm text-muted">Day 2 authentication foundation checkpoints</p>
          </div>
          <app-badge tone="brand">Foundation</app-badge>
        </div>

        <div class="mt-5 grid gap-3">
          @for (item of readiness; track item.label) {
            <div class="flex items-center justify-between rounded-lg border border-border p-3">
              <div class="flex items-center gap-3">
                <div class="grid h-8 w-8 place-items-center rounded-full bg-brand-50 text-brand-700">
                  <app-icon name="circle-check" [size]="17" />
                </div>
                <span class="text-sm font-medium text-ink">{{ item.label }}</span>
              </div>
              <span class="text-xs font-medium text-muted">{{ item.state }}</span>
            </div>
          }
        </div>
      </app-card>

      <app-empty-state
        icon="sparkles"
        title="Creative pipeline"
        description="Campaign and creative generation surfaces are intentionally left for future implementation days."
      >
        <a [routerLink]="primaryRoute()">
          <app-button variant="secondary" icon="chevron-right">Open workspace shell</app-button>
        </a>
      </app-empty-state>
    </section>
  `,
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
