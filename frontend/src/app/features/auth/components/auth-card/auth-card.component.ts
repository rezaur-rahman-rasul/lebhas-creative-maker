import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-auth-card',
  standalone: true,
  template: `
    <section class="overflow-hidden rounded-lg border border-border bg-white shadow-soft">
      <div class="h-1 bg-brand-600"></div>
      <div class="p-6 sm:p-8"><ng-content /></div>
    </section>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuthCardComponent {}
