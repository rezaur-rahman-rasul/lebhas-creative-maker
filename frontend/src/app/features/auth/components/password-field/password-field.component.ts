import { ChangeDetectionStrategy, Component, computed, input, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

import { IconComponent } from '@app/shared/components/icon/icon.component';

@Component({
  selector: 'app-password-field',
  standalone: true,
  imports: [ReactiveFormsModule, IconComponent],
  template: `
    <label class="block">
      <span class="mb-2 block text-sm font-medium text-ink">{{ label() }}</span>
      <span class="relative block">
        <app-icon
          name="lock-keyhole"
          className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
          [size]="17"
        />
        <input
          [type]="revealed() ? 'text' : 'password'"
          [formControl]="control()"
          [placeholder]="placeholder()"
          [autocomplete]="autocomplete()"
          class="h-11 w-full rounded-md border border-border bg-white pl-10 pr-11 text-sm text-ink shadow-sm outline-none transition placeholder:text-slate-400 focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
        />
        <button
          type="button"
          class="absolute right-3 top-1/2 -translate-y-1/2 rounded-md p-1 text-slate-400 transition hover:bg-slate-100 hover:text-ink"
          (click)="revealed.update((value) => !value)"
          [attr.aria-label]="revealed() ? 'Hide password' : 'Show password'"
        >
          <app-icon [name]="revealed() ? 'eye-off' : 'eye'" [size]="17" />
        </button>
      </span>

      @if (errorMessage()) {
        <span class="mt-2 block text-sm text-alert-600">{{ errorMessage() }}</span>
      }
    </label>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PasswordFieldComponent {
  readonly label = input.required<string>();
  readonly placeholder = input('Enter password');
  readonly autocomplete = input('current-password');
  readonly control = input.required<FormControl<string>>();
  readonly submitted = input(false);
  readonly externalError = input<string | null>(null);

  protected readonly revealed = signal(false);

  protected readonly errorMessage = computed(() => {
    const providedError = this.externalError();
    if (providedError) {
      return providedError;
    }

    const control = this.control();
    if (!this.submitted() && !control.touched) {
      return '';
    }

    if (control.hasError('required')) {
      return 'Password is required.';
    }

    if (control.hasError('minlength')) {
      return 'Password must be at least 8 characters.';
    }

    return '';
  });
}
