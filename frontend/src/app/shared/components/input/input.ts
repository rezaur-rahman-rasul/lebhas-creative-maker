import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

import { IconComponent } from '../icon/icon';

@Component({
  selector: 'app-input',
  standalone: true,
  imports: [IconComponent],
  templateUrl: './input.html',
  styleUrl: './input.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InputComponent {
  readonly label = input('');
  readonly type = input('text');
  readonly placeholder = input('');
  readonly autocomplete = input('off');
  readonly value = input('');
  readonly icon = input<string | null>(null);
  readonly error = input('');
  readonly disabled = input(false);
  readonly valueChange = output<string>();

  protected inputClasses(): string {
    const iconPadding = this.icon() ? 'pl-10' : 'pl-3';
    return `${iconPadding} h-11 w-full rounded-md border border-border bg-white pr-3 text-sm text-ink shadow-sm outline-none transition placeholder:text-slate-400 focus:border-brand-500 focus:ring-2 focus:ring-brand-100 disabled:bg-slate-100`;
  }
}
