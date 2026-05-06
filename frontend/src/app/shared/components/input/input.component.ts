import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

import { IconComponent } from '../icon/icon.component';

@Component({
  selector: 'app-input',
  standalone: true,
  imports: [IconComponent],
  template: `
    <label class="block">
      @if (label()) {
        <span class="mb-2 block text-sm font-medium text-ink">{{ label() }}</span>
      }

      <span class="relative block">
        @if (icon()) {
          <app-icon
            [name]="icon()!"
            className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
            [size]="17"
          />
        }

        <input
          [type]="type()"
          [value]="value()"
          [placeholder]="placeholder()"
          [autocomplete]="autocomplete()"
          [disabled]="disabled()"
          [class]="inputClasses()"
          (input)="valueChange.emit($any($event.target).value)"
        />
      </span>

      @if (error()) {
        <span class="mt-2 block text-sm text-alert-600">{{ error() }}</span>
      }
    </label>
  `,
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
