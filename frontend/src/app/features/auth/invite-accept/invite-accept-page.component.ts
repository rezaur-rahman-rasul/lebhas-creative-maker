import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { BadgeComponent } from '@app/shared/components/badge/badge.component';
import { ButtonComponent } from '@app/shared/components/button/button.component';
import { CardComponent } from '@app/shared/components/card/card.component';
import { InputComponent } from '@app/shared/components/input/input.component';

@Component({
  selector: 'app-invite-accept-page',
  standalone: true,
  imports: [RouterLink, BadgeComponent, ButtonComponent, CardComponent, InputComponent],
  template: `
    <app-card>
      <div class="mb-6">
        <app-badge tone="brand">Team invite</app-badge>
        <h1 class="mt-4 text-2xl font-semibold tracking-normal text-ink">Accept invitation</h1>
        <p class="mt-2 text-sm leading-6 text-muted">
          Join the workspace using the invitation sent by your admin.
        </p>
      </div>

      <form class="space-y-4">
        <app-input
          label="Invite code"
          icon="badge-check"
          placeholder="INV-"
          [value]="inviteCode()"
          (valueChange)="inviteCode.set($event)"
        />
        <app-input
          label="Email"
          type="email"
          icon="mail"
          autocomplete="email"
          placeholder="you@company.com"
          [value]="email()"
          (valueChange)="email.set($event)"
        />

        <app-button icon="arrow-right" [fullWidth]="true">Continue</app-button>
      </form>

      <p class="mt-6 text-center text-sm text-muted">
        Have an account?
        <a routerLink="/login" class="font-medium text-brand-700 hover:text-brand-600">Sign in</a>
      </p>
    </app-card>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InviteAcceptPageComponent {
  protected readonly inviteCode = signal('');
  protected readonly email = signal('');
}
