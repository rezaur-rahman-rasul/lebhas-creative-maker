import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AuthService } from '@app/core/auth/auth.service';
import { UserRole } from '@app/shared/models/user-role.model';
import { BadgeComponent } from '@app/shared/components/badge/badge.component';
import { ButtonComponent } from '@app/shared/components/button/button.component';
import { CardComponent } from '@app/shared/components/card/card.component';
import { InputComponent } from '@app/shared/components/input/input.component';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [RouterLink, BadgeComponent, ButtonComponent, CardComponent, InputComponent],
  template: `
    <app-card>
      <div class="mb-6">
        <app-badge tone="brand">Workspace access</app-badge>
        <h1 class="mt-4 text-2xl font-semibold tracking-normal text-ink">Sign in</h1>
        <p class="mt-2 text-sm leading-6 text-muted">
          Access the creative operations workspace for your team.
        </p>
      </div>

      <form class="space-y-4" (submit)="continue($event)">
        <app-input
          label="Email"
          type="email"
          icon="mail"
          autocomplete="email"
          placeholder="you@company.com"
          [value]="email()"
          (valueChange)="email.set($event)"
        />

        <app-input
          label="Password"
          type="password"
          icon="lock-keyhole"
          autocomplete="current-password"
          placeholder="Enter password"
          [value]="password()"
          (valueChange)="password.set($event)"
        />

        <div>
          <p class="mb-2 text-sm font-medium text-ink">Workspace role</p>
          <div class="grid grid-cols-3 gap-2">
            @for (role of roles; track role) {
              <button
                type="button"
                [class]="roleButtonClasses(role)"
                (click)="selectedRole.set(role)"
              >
                {{ role }}
              </button>
            }
          </div>
        </div>

        <app-button type="submit" icon="arrow-right" [fullWidth]="true">
          Continue
        </app-button>
      </form>

      <p class="mt-6 text-center text-sm text-muted">
        New workspace?
        <a routerLink="/register" class="font-medium text-brand-700 hover:text-brand-600">Create account</a>
      </p>
    </app-card>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPageComponent {
  protected readonly email = signal('admin@creative-saas.local');
  protected readonly password = signal('');
  protected readonly selectedRole = signal<UserRole>('ADMIN');
  protected readonly roles: readonly UserRole[] = ['MASTER', 'ADMIN', 'CREW'];

  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly returnUrl = computed(
    () => this.route.snapshot.queryParamMap.get('returnUrl') || '/dashboard',
  );

  protected roleButtonClasses(role: UserRole): string {
    const selected = this.selectedRole() === role;
    return [
      'h-10 rounded-md border px-2 text-xs font-semibold transition',
      selected
        ? 'border-brand-600 bg-brand-50 text-brand-700'
        : 'border-border bg-white text-muted hover:bg-slate-50 hover:text-ink',
    ].join(' ');
  }

  protected continue(event: SubmitEvent): void {
    event.preventDefault();
    this.authService.startLocalSession(this.selectedRole());
    void this.router.navigateByUrl(this.returnUrl());
  }
}
