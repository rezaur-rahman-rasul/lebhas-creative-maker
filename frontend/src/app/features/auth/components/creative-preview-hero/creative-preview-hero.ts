import { ChangeDetectionStrategy, Component } from '@angular/core';

import { IconComponent } from '@app/shared/components/icon/icon';

interface FloatingTag {
  readonly label: string;
  readonly classes: string;
}

@Component({
  selector: 'app-creative-preview-hero',
  standalone: true,
  imports: [IconComponent],
  templateUrl: './creative-preview-hero.html',
  styleUrl: './creative-preview-hero.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CreativePreviewHeroComponent {
  protected readonly productImageUrl =
    'https://images.unsplash.com/photo-1593030761757-71fae45fa0e7?auto=format&fit=crop&w=900&q=80';
  protected readonly campaignImageUrl =
    'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?auto=format&fit=crop&w=900&q=80';
  protected readonly finalPreviewImageUrl =
    'https://images.unsplash.com/photo-1507679799987-c73779587ccf?auto=format&fit=crop&w=900&q=80';

  protected readonly fileBubbles: readonly FloatingTag[] = [
    { label: 'PNG', classes: 'left-0 top-10 bg-white text-slate-700' },
    { label: 'JPG', classes: 'right-10 top-2 bg-brand-100 text-brand-700' },
    { label: 'SVG', classes: 'left-14 bottom-10 bg-slate-900 text-white' },
    { label: 'MP4', classes: 'right-0 bottom-24 bg-blue-100 text-blue-700' },
  ];

  protected readonly floatingBadges: readonly FloatingTag[] = [
    { label: 'AI Creative', classes: 'left-8 top-0' },
    { label: 'Ad Ready', classes: 'right-20 top-24' },
    { label: '16:45', classes: 'left-28 bottom-2' },
    { label: 'PNG', classes: 'right-8 bottom-8' },
    { label: 'JPG', classes: 'right-36 bottom-0' },
  ];
}
