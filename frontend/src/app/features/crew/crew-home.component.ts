import { ChangeDetectionStrategy, Component } from '@angular/core';

import { BadgeComponent } from '@app/shared/components/badge/badge.component';
import { CardComponent } from '@app/shared/components/card/card.component';
import { EmptyStateComponent } from '@app/shared/components/empty-state/empty-state.component';
import { PageHeaderComponent } from '@app/shared/components/page-header/page-header.component';

@Component({
  selector: 'app-crew-home',
  standalone: true,
  imports: [BadgeComponent, CardComponent, EmptyStateComponent, PageHeaderComponent],
  template: `
    <app-page-header
      eyebrow="Team"
      title="Crew workspace"
      description="Scoped team member foundation for assigned creative work."
    >
      <app-badge tone="blue">CREW</app-badge>
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
        icon="users"
        title="Assigned work"
        description="Task queues, uploads, and review surfaces are prepared as future feature areas."
      />
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CrewHomeComponent {
  protected readonly sections = [
    { title: 'Scoped access', description: 'Crew routes inherit tenant and role guard foundation.' },
    { title: 'Creative tasks', description: 'Task boundaries are ready for future domain work.' },
    { title: 'Collaboration', description: 'Team collaboration shell is prepared.' },
  ];
}
