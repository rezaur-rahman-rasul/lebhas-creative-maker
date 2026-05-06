import { ChangeDetectionStrategy, Component, input } from '@angular/core';

import { InitialsPipe } from '../../pipes/initials.pipe';

@Component({
  selector: 'app-avatar',
  standalone: true,
  imports: [InitialsPipe],
  template: `
    <div
      class="grid h-9 w-9 place-items-center rounded-full bg-brand-50 text-sm font-semibold text-brand-700 ring-1 ring-brand-100"
      [attr.aria-label]="name()"
    >
      {{ name() | initials }}
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AvatarComponent {
  readonly name = input('User');
}
