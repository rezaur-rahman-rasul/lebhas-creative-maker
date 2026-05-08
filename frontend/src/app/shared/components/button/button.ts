import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

import { IconComponent } from '../icon/icon';
import { SpinnerComponent } from '../spinner/spinner';

type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger';
type ButtonSize = 'sm' | 'md' | 'lg';

@Component({
  selector: 'app-button',
  standalone: true,
  imports: [IconComponent, SpinnerComponent],
  templateUrl: './button.html',
  styleUrl: './button.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ButtonComponent {
  readonly type = input<'button' | 'submit' | 'reset'>('button');
  readonly variant = input<ButtonVariant>('primary');
  readonly size = input<ButtonSize>('md');
  readonly icon = input<string | null>(null);
  readonly loading = input(false);
  readonly disabled = input(false);
  readonly fullWidth = input(false);

  protected readonly iconSize = computed(() => (this.size() === 'sm' ? 16 : 18));

  protected readonly classes = computed(() => {
    const base =
      'inline-flex items-center justify-center gap-2 rounded-md font-medium tracking-normal transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand-500 focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-60';
    const sizes: Record<ButtonSize, string> = {
      sm: 'h-9 px-3 text-sm',
      md: 'h-10 px-4 text-sm',
      lg: 'h-11 px-5 text-base',
    };
    const variants: Record<ButtonVariant, string> = {
      primary: 'bg-brand-600 text-white shadow-sm hover:bg-brand-700',
      secondary: 'border border-border bg-white text-ink hover:bg-slate-50',
      ghost: 'text-muted hover:bg-slate-100 hover:text-ink',
      danger: 'bg-alert-600 text-white hover:bg-alert-500',
    };

    return [base, sizes[this.size()], variants[this.variant()], this.fullWidth() ? 'w-full' : '']
      .filter(Boolean)
      .join(' ');
  });
}
