import { ChangeDetectionStrategy, Component } from '@angular/core';

import { BadgeComponent } from '@app/shared/components/badge/badge.component';
import { CardComponent } from '@app/shared/components/card/card.component';
import { EmptyStateComponent } from '@app/shared/components/empty-state/empty-state.component';
import { PageHeaderComponent } from '@app/shared/components/page-header/page-header.component';

@Component({
  selector: 'app-master-home',
  standalone: true,
  imports: [BadgeComponent, CardComponent, EmptyStateComponent, PageHeaderComponent],
  template: `
    <app-page-header
      eyebrow="Platform"
      title="Master console"
      description="Platform-owner workspace foundation for global administration."
    >
      <app-badge tone="red">MASTER</app-badge>
    </app-page-header>

    <section class="mt-6 grid gap-4 lg:grid-cols-3">
      @for (item of sections; track item.title) {
        <app-card>
          <h2 class="text-base font-semibold text-ink">{{ item.title }}</h2>
          <p class="mt-2 text-sm leading-6 text-muted">{{ item.description }}</p>
        </app-card>
      }
    </section>

    <div class="mt-6">
      <app-empty-state
        icon="shield-check"
        title="Platform controls"
        description="Tenant administration, platform policy, and operational tooling are reserved for future implementation days."
      />
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MasterHomeComponent {
  protected readonly sections = [
    { title: 'Tenant registry', description: 'Workspace-level ownership boundary prepared.' },
    { title: 'System policy', description: 'RBAC and platform access surfaces prepared.' },
    { title: 'Operations', description: 'Health, logging, and deployment paths prepared.' },
  ];
}
