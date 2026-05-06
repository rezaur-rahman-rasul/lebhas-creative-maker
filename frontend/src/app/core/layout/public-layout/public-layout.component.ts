import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { IconComponent } from '@app/shared/components/icon/icon.component';
import { environment } from '@env/environment';

@Component({
  selector: 'app-public-layout',
  standalone: true,
  imports: [RouterOutlet, IconComponent],
  template: `
    <main class="min-h-screen bg-canvas">
      <div class="grid min-h-screen lg:grid-cols-[0.92fr_1.08fr]">
        <section class="hidden border-r border-border bg-slate-950 p-8 text-white lg:flex lg:flex-col">
          <div class="flex items-center gap-3">
            <div class="grid h-10 w-10 place-items-center rounded-lg bg-brand-500 text-white">
              <app-icon name="wand-sparkles" [size]="21" />
            </div>
            <div>
              <p class="text-sm font-semibold">{{ appName }}</p>
              <p class="text-xs text-slate-400">Bangladesh creative SaaS</p>
            </div>
          </div>

          <div class="mt-auto max-w-xl">
            <div class="mb-6 grid h-14 w-14 place-items-center rounded-lg bg-white/10">
              <app-icon name="megaphone" [size]="26" />
            </div>
            <h1 class="text-4xl font-semibold leading-tight tracking-normal">
              Campaign workspace for fast-moving growth teams.
            </h1>
            <p class="mt-4 text-base leading-7 text-slate-300">
              A clean foundation for teams creating multilingual ad creatives across Meta, TikTok,
              and LinkedIn.
            </p>

            <div class="mt-8 grid grid-cols-3 gap-3">
              @for (metric of metrics; track metric.label) {
                <div class="rounded-lg border border-white/10 bg-white/[0.06] p-4">
                  <p class="text-2xl font-semibold">{{ metric.value }}</p>
                  <p class="mt-1 text-xs text-slate-400">{{ metric.label }}</p>
                </div>
              }
            </div>
          </div>
        </section>

        <section class="flex min-h-screen items-center justify-center px-4 py-8 sm:px-6 lg:px-10">
          <div class="w-full max-w-md">
            <div class="mb-8 flex items-center gap-3 lg:hidden">
              <div class="grid h-10 w-10 place-items-center rounded-lg bg-brand-600 text-white">
                <app-icon name="wand-sparkles" [size]="21" />
              </div>
              <div>
                <p class="text-sm font-semibold text-ink">{{ appName }}</p>
                <p class="text-xs text-muted">Bangladesh creative SaaS</p>
              </div>
            </div>

            <router-outlet />
          </div>
        </section>
      </div>
    </main>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PublicLayoutComponent {
  protected readonly appName = environment.appName;
  protected readonly metrics = [
    { value: '2', label: 'Languages' },
    { value: '4', label: 'Ad channels' },
    { value: '3', label: 'Role tiers' },
  ];
}
