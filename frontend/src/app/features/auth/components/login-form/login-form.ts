import { ChangeDetectionStrategy, Component, inject, input, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { AutofocusDirective } from '@app/shared/directives/autofocus.directive';
import { ButtonComponent } from '@app/shared/components/button/button';
import { PasswordFieldComponent } from '../password-field/password-field';

export interface LoginFormValue {
  readonly identifier: string;
  readonly password: string;
}

@Component({
  selector: 'app-login-form',
  standalone: true,
  imports: [RouterLink, ReactiveFormsModule, AutofocusDirective, ButtonComponent, PasswordFieldComponent],
  templateUrl: './login-form.html',
  styleUrl: './login-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginFormComponent {
  private readonly formBuilder = inject(FormBuilder).nonNullable;

  readonly loading = input(false);
  readonly errorMessage = input('');
  readonly fieldErrors = input<Readonly<Record<string, string>>>({});
  readonly submitted = output<LoginFormValue>();
  readonly forgotPassword = output<void>();

  protected readonly attemptedSubmit = signal(false);
  protected readonly form = this.formBuilder.group({
    identifier: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  protected submit(): void {
    this.attemptedSubmit.set(true);
    this.form.markAllAsTouched();

    if (this.form.invalid) {
      return;
    }

    const value = this.form.getRawValue();
    this.submitted.emit({
      identifier: value.identifier.trim(),
      password: value.password,
    });
  }

  protected identifierError(): string {
    const control = this.form.controls.identifier;
    const serverError = this.fieldErrors()['identifier'];

    if (serverError) {
      return serverError;
    }

    if (!this.attemptedSubmit() && !control.touched) {
      return '';
    }

    return control.hasError('required') ? 'Enter your email address.' : '';
  }

  protected passwordError(): string {
    const control = this.form.controls.password;
    const serverError = this.fieldErrors()['password'];

    if (serverError) {
      return serverError;
    }

    if (!this.attemptedSubmit() && !control.touched) {
      return '';
    }

    if (control.hasError('required')) {
      return 'Enter your password.';
    }

    if (control.hasError('minlength')) {
      return 'Password must be at least 8 characters.';
    }

    return '';
  }
}
