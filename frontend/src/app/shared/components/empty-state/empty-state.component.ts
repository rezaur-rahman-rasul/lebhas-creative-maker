import { ChangeDetectionStrategy, Component, input } from '@angular/core';

import { IconComponent } from '../icon/icon.component';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [IconComponent],
  template: `
    <div class="rounded-lg border border-dashed border-border bg-white p-8 text-center">
      <div class="mx-auto grid h-12 w-12 place-items-center rounded-full bg-brand-50 text-brand-700">
        <app-icon [name]="icon()" [size]="22" />
      </div>
      <h3 class="mt-4 text-base font-semibold text-ink">{{ title() }}</h3>
      <p class="mx-auto mt-2 max-w-md text-sm leading-6 text-muted">{{ description() }}</p>
      <div class="mt-5"><ng-content /></div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EmptyStateComponent {
  readonly icon = input('sparkles');
  readonly title = input.required<string>();
  readonly description = input.required<string>();
}
