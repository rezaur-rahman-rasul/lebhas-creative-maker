import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { BadgeComponent } from '@app/shared/components/badge/badge.component';
import { CardComponent } from '@app/shared/components/card/card.component';
import { EmptyStateComponent } from '@app/shared/components/empty-state/empty-state.component';
import { PageHeaderComponent } from '@app/shared/components/page-header/page-header.component';

interface RoleSectionHighlight {
  readonly title: string;
  readonly description: string;
}

interface RoleSectionData {
  readonly eyebrow: string;
  readonly title: string;
  readonly description: string;
  readonly badgeLabel: string;
  readonly badgeTone: 'brand' | 'blue' | 'red' | 'neutral';
  readonly emptyIcon: string;
  readonly emptyTitle: string;
  readonly emptyDescription: string;
  readonly highlights: readonly RoleSectionHighlight[];
}

@Component({
  selector: 'app-role-section-page',
  standalone: true,
  imports: [BadgeComponent, CardComponent, EmptyStateComponent, PageHeaderComponent],
  template: `
    <app-page-header
      [eyebrow]="section.eyebrow"
      [title]="section.title"
      [description]="section.description"
    >
      <app-badge [tone]="section.badgeTone">{{ section.badgeLabel }}</app-badge>
    </app-page-header>

    <section class="mt-6 grid gap-4 lg:grid-cols-3">
      @for (item of section.highlights; track item.title) {
        <app-card>
          <h2 class="text-base font-semibold text-ink">{{ item.title }}</h2>
          <p class="mt-2 text-sm leading-6 text-muted">{{ item.description }}</p>
        </app-card>
      }
    </section>

    <div class="mt-6">
      <app-empty-state
        [icon]="section.emptyIcon"
        [title]="section.emptyTitle"
        [description]="section.emptyDescription"
      />
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RoleSectionPageComponent {
  private readonly route = inject(ActivatedRoute);

  protected readonly section = this.route.snapshot.data['section'] as RoleSectionData;
}
