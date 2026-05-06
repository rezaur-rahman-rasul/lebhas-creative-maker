import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { BadgeComponent } from '@app/shared/components/badge/badge.component';
import { ButtonComponent } from '@app/shared/components/button/button.component';
import { CardComponent } from '@app/shared/components/card/card.component';
import { InputComponent } from '@app/shared/components/input/input.component';

@Component({
  selector: 'app-register-page',
  standalone: true,
  imports: [RouterLink, BadgeComponent, ButtonComponent, CardComponent, InputComponent],
  template: `
    <app-card>
      <div class="mb-6">
        <app-badge tone="blue">Admin workspace</app-badge>
        <h1 class="mt-4 text-2xl font-semibold tracking-normal text-ink">Create workspace</h1>
        <p class="mt-2 text-sm leading-6 text-muted">
          Prepare an account for your business or agency team.
        </p>
      </div>

      <form class="space-y-4">
        <app-input
          label="Workspace name"
          icon="building-2"
          placeholder="Dhaka Growth Studio"
          [value]="workspaceName()"
          (valueChange)="workspaceName.set($event)"
        />
        <app-input
          label="Owner email"
          type="email"
          icon="mail"
          autocomplete="email"
          placeholder="owner@company.com"
          [value]="email()"
          (valueChange)="email.set($event)"
        />
        <app-input
          label="Password"
          type="password"
          icon="lock-keyhole"
          autocomplete="new-password"
          placeholder="Create password"
          [value]="password()"
          (valueChange)="password.set($event)"
        />

        <app-button icon="user-plus" [fullWidth]="true">Create account</app-button>
      </form>

      <p class="mt-6 text-center text-sm text-muted">
        Already registered?
        <a routerLink="/login" class="font-medium text-brand-700 hover:text-brand-600">Sign in</a>
      </p>
    </app-card>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterPageComponent {
  protected readonly workspaceName = signal('');
  protected readonly email = signal('');
  protected readonly password = signal('');
}
