import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

type BadgeTone = 'neutral' | 'brand' | 'blue' | 'red';

@Component({
  selector: 'app-badge',
  standalone: true,
  template: `<span [class]="classes()"><ng-content /></span>`,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BadgeComponent {
  readonly tone = input<BadgeTone>('neutral');

  protected readonly classes = computed(() => {
    const tones: Record<BadgeTone, string> = {
      neutral: 'bg-slate-100 text-slate-700',
      brand: 'bg-brand-50 text-brand-700',
      blue: 'bg-blue-50 text-blue-700',
      red: 'bg-red-50 text-red-700',
    };

    return `inline-flex h-6 items-center rounded-full px-2.5 text-xs font-medium ${tones[this.tone()]}`;
  });
}
