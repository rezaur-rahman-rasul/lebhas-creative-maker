import { ChangeDetectionStrategy, Component } from '@angular/core';

import { BadgeComponent } from '@app/shared/components/badge/badge.component';
import { CardComponent } from '@app/shared/components/card/card.component';
import { EmptyStateComponent } from '@app/shared/components/empty-state/empty-state.component';
import { PageHeaderComponent } from '@app/shared/components/page-header/page-header.component';

@Component({
  selector: 'app-admin-home',
  standalone: true,
  imports: [BadgeComponent, CardComponent, EmptyStateComponent, PageHeaderComponent],
  template: `
    <app-page-header
      eyebrow="Workspace"
      title="Admin console"
      description="Workspace owner foundation for crews, assets, and campaign setup."
    >
      <app-badge tone="brand">ADMIN</app-badge>
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
        icon="building-2"
        title="Workspace management"
        description="Team, creative, and campaign workflows are intentionally outside the Day 1 foundation."
      />
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminHomeComponent {
  protected readonly sections = [
    { title: 'Crew access', description: 'Role-aware workspace navigation prepared.' },
    { title: 'Brand assets', description: 'Upload and storage integration points prepared.' },
    { title: 'Campaign shell', description: 'Campaign route boundaries prepared.' },
  ];
}
