import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { NotificationStateService } from '@app/core/state/notification-state.service';
import { IconComponent } from '../icon/icon.component';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [IconComponent],
  template: `
    <div class="fixed right-4 top-4 z-50 flex w-[min(92vw,24rem)] flex-col gap-3">
      @for (notification of notifications.notifications(); track notification.id) {
        <div class="rounded-lg border border-border bg-white p-4 shadow-soft">
          <div class="flex gap-3">
            <div [class]="iconWrap(notification.tone)">
              <app-icon [name]="iconName(notification.tone)" [size]="18" />
            </div>
            <div class="min-w-0 flex-1">
              <p class="text-sm font-semibold text-ink">{{ notification.title }}</p>
              @if (notification.message) {
                <p class="mt-1 text-sm leading-5 text-muted">{{ notification.message }}</p>
              }
            </div>
            <button
              type="button"
              class="rounded-md p-1 text-slate-400 hover:bg-slate-100 hover:text-ink"
              (click)="notifications.dismiss(notification.id)"
              aria-label="Dismiss notification"
            >
              <app-icon name="x" [size]="16" />
            </button>
          </div>
        </div>
      }
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ToastComponent {
  protected readonly notifications = inject(NotificationStateService);

  protected iconName(tone: string): string {
    return tone === 'success' ? 'circle-check' : tone === 'error' ? 'circle-alert' : 'info';
  }

  protected iconWrap(tone: string): string {
    const toneClass =
      tone === 'success'
        ? 'bg-brand-50 text-brand-700'
        : tone === 'error'
          ? 'bg-red-50 text-red-700'
          : 'bg-blue-50 text-blue-700';
    return `grid h-8 w-8 shrink-0 place-items-center rounded-full ${toneClass}`;
  }
}
