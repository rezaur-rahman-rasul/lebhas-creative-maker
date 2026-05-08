import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-card',
  standalone: true,
  templateUrl: './card.html',
  styleUrl: './card.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CardComponent {
  readonly padded = input(true);
  readonly interactive = input(false);

  protected readonly classes = computed(() =>
    [
      'rounded-lg border border-border bg-white shadow-sm',
      this.padded() ? 'p-5' : '',
      this.interactive() ? 'transition hover:-translate-y-0.5 hover:shadow-soft' : '',
    ]
      .filter(Boolean)
      .join(' '),
  );
}
