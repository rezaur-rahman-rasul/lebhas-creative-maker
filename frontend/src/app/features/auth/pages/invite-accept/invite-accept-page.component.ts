import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { AutofocusDirective } from '@app/shared/directives/autofocus.directive';
import { matchingFieldsValidator } from '@app/shared/validators/matching-fields.validator';
import { ButtonComponent } from '@app/shared/components/button/button.component';
import { AuthCardComponent } from '../../components/auth-card/auth-card.component';
import { AuthHeaderComponent } from '../../components/auth-header/auth-header.component';
import { PasswordFieldComponent } from '../../components/password-field/password-field.component';
import { AuthFacade } from '../../services/auth.facade';

@Component({
  selector: 'app-invite-accept-page',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    AutofocusDirective,
    ButtonComponent,
    AuthCardComponent,
    AuthHeaderComponent,
    PasswordFieldComponent,
  ],
  template: `
    <app-auth-card>
      <app-auth-header
        badgeLabel="Team invitation"
        badgeTone="brand"
        title="Accept workspace invitation"
        description="Create your crew credentials and join the workspace using the invite token in your link."
        icon="badge-check"
      />

      <div class="mb-5 rounded-md border border-border bg-slate-50 px-4 py-3">
        <p class="text-xs font-medium uppercase tracking-[0.12em] text-muted">Invitation token</p>
        <p class="mt-1 truncate text-sm font-semibold text-ink">{{ invitationToken() || 'Missing token' }}</p>
      </div>

      @if (auth.authError()) {
        <div class="mb-4 rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {{ auth.authError() }}
        </div>
      }

      <form class="space-y-4" [formGroup]="form" (ngSubmit)="submit()">
        <div class="grid gap-4 sm:grid-cols-2">
          <label class="block">
            <span class="mb-2 block text-sm font-medium text-ink">First name</span>
            <input
              appAutofocus
              type="text"
              formControlName="firstName"
              autocomplete="given-name"
              placeholder="Rezaur"
              class="h-11 w-full rounded-md border border-border bg-white px-3 text-sm text-ink shadow-sm outline-none transition placeholder:text-slate-400 focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
            />
            @if (fieldError('firstName')) {
              <span class="mt-2 block text-sm text-alert-600">{{ fieldError('firstName') }}</span>
            }
          </label>

          <label class="block">
            <span class="mb-2 block text-sm font-medium text-ink">Last name</span>
            <input
              type="text"
              formControlName="lastName"
              autocomplete="family-name"
              placeholder="Rasul"
              class="h-11 w-full rounded-md border border-border bg-white px-3 text-sm text-ink shadow-sm outline-none transition placeholder:text-slate-400 focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
            />
            @if (fieldError('lastName')) {
              <span class="mt-2 block text-sm text-alert-600">{{ fieldError('lastName') }}</span>
            }
          </label>
        </div>

        <label class="block">
          <span class="mb-2 block text-sm font-medium text-ink">Work email</span>
          <input
            type="email"
            formControlName="email"
            autocomplete="email"
            placeholder="you@company.com"
            class="h-11 w-full rounded-md border border-border bg-white px-3 text-sm text-ink shadow-sm outline-none transition placeholder:text-slate-400 focus:border-brand-500 focus:ring-2 focus:ring-brand-100"
          />
          @if (fieldError('email')) {
            <span class="mt-2 block text-sm text-alert-600">{{ fieldError('email') }}</span>
          }
        </label>

        <app-password-field
          label="Password"
          autocomplete="new-password"
          placeholder="Create a strong password"
          [control]="form.controls.password"
          [submitted]="submitted()"
          [externalError]="fieldError('password')"
        />

        <app-password-field
          label="Confirm password"
          autocomplete="new-password"
          placeholder="Repeat the password"
          [control]="form.controls.confirmPassword"
          [submitted]="submitted()"
          [externalError]="confirmPasswordError()"
        />

        <app-button
          type="submit"
          icon="arrow-right"
          [fullWidth]="true"
          [loading]="auth.authLoading()"
          [disabled]="!invitationToken()"
        >
          Accept invitation
        </app-button>
      </form>

      <p class="mt-6 text-center text-sm text-muted">
        Already have access?
        <a routerLink="/login" class="font-medium text-brand-700 hover:text-brand-600">Sign in</a>
      </p>
    </app-auth-card>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InviteAcceptPageComponent {
  private readonly formBuilder = inject(FormBuilder).nonNullable;
  private readonly route = inject(ActivatedRoute);
  protected readonly auth = inject(AuthFacade);

  protected readonly submitted = signal(false);
  protected readonly invitationToken = computed(
    () =>
      this.route.snapshot.paramMap.get('token') ||
      this.route.snapshot.queryParamMap.get('token') ||
      '',
  );

  protected readonly form = this.formBuilder.group(
    {
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]],
    },
    {
      validators: matchingFieldsValidator('password', 'confirmPassword'),
    },
  );

  protected fieldError(
    controlName: 'firstName' | 'lastName' | 'email' | 'password',
  ): string {
    const control = this.form.controls[controlName];
    if (!this.submitted() && !control.touched) {
      return '';
    }

    const serverError = control.errors?.['server'] as string | undefined;
    if (serverError) {
      return serverError;
    }

    if (control.hasError('required')) {
      return 'This field is required.';
    }

    if (controlName === 'email' && control.hasError('email')) {
      return 'Enter a valid email address.';
    }

    if (controlName === 'password' && control.hasError('minlength')) {
      return 'Password must be at least 8 characters.';
    }

    return '';
  }

  protected confirmPasswordError(): string {
    const control = this.form.controls.confirmPassword;
    if (!this.submitted() && !control.touched) {
      return '';
    }

    const serverError = control.errors?.['server'] as string | undefined;
    if (serverError) {
      return serverError;
    }

    if (control.hasError('required')) {
      return 'Confirm your password.';
    }

    if (this.form.hasError('mismatch')) {
      return 'Passwords do not match.';
    }

    return '';
  }

  protected async submit(): Promise<void> {
    this.submitted.set(true);
    this.form.markAllAsTouched();
    this.clearServerErrors();

    if (this.form.invalid || !this.invitationToken()) {
      return;
    }

    const { confirmPassword: _confirmPassword, ...payload } = this.form.getRawValue();
    const result = await this.auth.acceptInvite({
      ...payload,
      phone: null,
      invitationToken: this.invitationToken(),
    });

    if (!result.ok) {
      this.applyServerErrors(result.fieldErrors);
    }
  }

  private applyServerErrors(fieldErrors: Readonly<Record<string, string>>): void {
    for (const [field, message] of Object.entries(fieldErrors)) {
      const control = this.form.get(field);
      if (control) {
        control.setErrors({ ...(control.errors ?? {}), server: message });
      }
    }
  }

  private clearServerErrors(): void {
    for (const control of Object.values(this.form.controls)) {
      if (control.errors?.['server']) {
        const { server: _server, ...remaining } = control.errors;
        control.setErrors(Object.keys(remaining).length > 0 ? remaining : null);
      }
    }
  }
}
