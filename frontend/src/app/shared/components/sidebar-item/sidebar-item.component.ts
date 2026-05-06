import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { IconComponent } from '../icon/icon.component';

@Component({
  selector: 'app-sidebar-item',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, IconComponent],
  template: `
    <a
      [routerLink]="route()"
      routerLinkActive="bg-brand-50 text-brand-700"
      [routerLinkActiveOptions]="{ exact: false }"
      class="group flex h-10 items-center gap-3 rounded-md px-3 text-sm font-medium text-slate-600 transition hover:bg-slate-100 hover:text-ink"
    >
      <app-icon [name]="icon()" [size]="18" />
      @if (!compact()) {
        <span class="truncate">{{ label() }}</span>
      }
    </a>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SidebarItemComponent {
  readonly label = input.required<string>();
  readonly icon = input.required<string>();
  readonly route = input.required<string>();
  readonly compact = input(false);
}
