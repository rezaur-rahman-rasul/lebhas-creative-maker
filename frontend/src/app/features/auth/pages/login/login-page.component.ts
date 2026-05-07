import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { AutofocusDirective } from '@app/shared/directives/autofocus.directive';
import { ButtonComponent } from '@app/shared/components/button/button.component';
import { AuthCardComponent } from '../../components/auth-card/auth-card.component';
import { AuthHeaderComponent } from '../../components/auth-header/auth-header.component';
import { PasswordFieldComponent } from '../../components/password-field/password-field.component';
import { AuthFacade } from '../../services/auth.facade';

@Component({
  selector: 'app-login-page',
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
        badgeLabel="Workspace access"
        title="Sign in to your workspace"
        description="Use your team credentials to continue into the creative operations console."
        icon="shield-check"
      />

      @if (auth.authError()) {
        <div class="mb-4 rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {{ auth.authError() }}
        </div>
      }

      <form class="space-y-4" [formGroup]="form" (ngSubmit)="submit()">
        <label class="block">
          <span class="mb-2 block text-sm font-medium text-ink">Email</span>
          <input
            appAutofocus
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
          autocomplete="current-password"
          [control]="form.controls.password"
          [submitted]="submitted()"
          [externalError]="fieldError('password')"
        />

        <div class="flex items-center justify-between gap-3 text-sm">
          <button type="button" class="font-medium text-muted hover:text-ink">
            Forgot password
          </button>
          <a routerLink="/register" class="font-medium text-brand-700 hover:text-brand-600">
            Create account
          </a>
        </div>

        <app-button
          type="submit"
          icon="arrow-right"
          [fullWidth]="true"
          [loading]="auth.authLoading()"
        >
          Sign in
        </app-button>
      </form>
    </app-auth-card>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPageComponent {
  private readonly formBuilder = inject(FormBuilder).nonNullable;
  private readonly route = inject(ActivatedRoute);
  protected readonly auth = inject(AuthFacade);

  protected readonly submitted = signal(false);

  protected readonly form = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  protected readonly returnUrl = computed(
    () => this.route.snapshot.queryParamMap.get('returnUrl') || '/dashboard',
  );

  protected fieldError(controlName: 'email' | 'password'): string {
    const control = this.form.controls[controlName];
    if (!this.submitted() && !control.touched) {
      return '';
    }

    const serverError = control.errors?.['server'] as string | undefined;
    if (serverError) {
      return serverError;
    }

    if (control.hasError('required')) {
      return controlName === 'email' ? 'Email is required.' : 'Password is required.';
    }

    if (controlName === 'email' && control.hasError('email')) {
      return 'Enter a valid email address.';
    }

    if (controlName === 'password' && control.hasError('minlength')) {
      return 'Password must be at least 8 characters.';
    }

    return '';
  }

  protected async submit(): Promise<void> {
    this.submitted.set(true);
    this.form.markAllAsTouched();
    this.clearServerErrors();

    if (this.form.invalid) {
      return;
    }

    const result = await this.auth.login(this.form.getRawValue(), this.returnUrl());
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
