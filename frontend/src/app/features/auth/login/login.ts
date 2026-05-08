import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';

import { NotificationStateService } from '@app/core/state/notification-state.service';
import { IconComponent } from '@app/shared/components/icon/icon';
import { LoginFormComponent, LoginFormValue } from '../components/login-form/login-form';
import { RememberedPasswordDialogComponent } from '../components/remembered-password-dialog/remembered-password-dialog';
import { RememberedProfilesComponent } from '../components/remembered-profiles/remembered-profiles';
import { RemoveProfilesDialogComponent } from '../components/remove-profiles-dialog/remove-profiles-dialog';
import { RememberedProfile } from '../models/auth.models';
import { AuthFacade } from '../services/auth.facade';

const REMEMBERED_PROFILES_STORAGE_KEY = 'lebhas.auth.remembered-profiles';

const MOCK_REMEMBERED_PROFILES: readonly RememberedProfile[] = [
  {
    id: 'remembered-profile-rezaur-1',
    name: 'Md. Rezaur Rahman Rasul',
    avatarUrl: createAvatarDataUrl('MR', '#0f766e', '#2563eb'),
    email: 'rezaur@lebhas.com',
    hasSavedPassword: true,
  },
  {
    id: 'remembered-profile-rezaur-2',
    name: 'Rezaur Rahman Rasul',
    avatarUrl: createAvatarDataUrl('RR', '#7c3aed', '#ec4899'),
    email: 'rrr@lebhas.com',
    hasSavedPassword: false,
  },
];

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [
    IconComponent,
    LoginFormComponent,
    RememberedProfilesComponent,
    RememberedPasswordDialogComponent,
    RemoveProfilesDialogComponent,
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LoginPageComponent {
  private readonly auth = inject(AuthFacade);
  private readonly notifications = inject(NotificationStateService);
  private readonly initialRememberedProfiles = this.readRememberedProfiles();

  protected readonly rememberedProfiles = signal<readonly RememberedProfile[]>(
    this.initialRememberedProfiles,
  );
  protected readonly selectedProfile = signal<RememberedProfile | null>(null);
  protected readonly showPasswordDialog = signal(false);
  protected readonly showRemoveProfilesDialog = signal(false);
  protected readonly showLoginForm = signal(this.initialRememberedProfiles.length === 0);
  protected readonly loginFormError = signal('');
  protected readonly loginFieldErrors = signal<Readonly<Record<string, string>>>({});
  protected readonly passwordDialogError = signal('');

  protected readonly hasRememberedProfiles = computed(() => this.rememberedProfiles().length > 0);
  protected readonly showRememberedProfiles = computed(
    () => this.hasRememberedProfiles() && !this.showLoginForm(),
  );

  protected readonly authLoading = this.auth.authLoading;

  constructor() {
    effect(() => {
      if (typeof window === 'undefined') {
        return;
      }

      window.localStorage.setItem(
        REMEMBERED_PROFILES_STORAGE_KEY,
        JSON.stringify(this.rememberedProfiles()),
      );
    });
  }

  protected showManualLogin(): void {
    this.clearAllErrors();
    this.closePasswordDialog();
    this.showLoginForm.set(true);
  }

  protected showSavedProfiles(): void {
    if (!this.hasRememberedProfiles()) {
      return;
    }

    this.clearAllErrors();
    this.closePasswordDialog();
    this.showLoginForm.set(false);
  }

  protected async submitLoginForm(value: LoginFormValue): Promise<void> {
    this.loginFormError.set('');
    this.loginFieldErrors.set({});

    const result = await this.auth.login({
      email: value.identifier,
      password: value.password,
    });

    if (result.ok) {
      return;
    }

    const mappedFieldErrors = this.mapLoginFieldErrors(result.fieldErrors);
    this.loginFieldErrors.set(mappedFieldErrors);

    if (Object.keys(mappedFieldErrors).length === 0) {
      this.loginFormError.set(result.message);
    }
  }

  protected async selectProfile(profile: RememberedProfile): Promise<void> {
    this.clearAllErrors();

    if (profile.hasSavedPassword) {
      await this.auth.loginWithRememberedProfile(profile);
      return;
    }

    this.selectedProfile.set(profile);
    this.showPasswordDialog.set(true);
  }

  protected closePasswordDialog(): void {
    this.showPasswordDialog.set(false);
    this.selectedProfile.set(null);
    this.passwordDialogError.set('');
  }

  protected async submitRememberedPassword(password: string): Promise<void> {
    const profile = this.selectedProfile();
    if (!profile) {
      return;
    }

    this.passwordDialogError.set('');
    const result = await this.auth.loginWithRememberedProfile(profile, { password });

    if (result.ok) {
      return;
    }

    this.passwordDialogError.set(result.fieldErrors['password'] || result.message);
  }

  protected openRemoveProfilesDialog(): void {
    this.clearAllErrors();
    this.showRemoveProfilesDialog.set(true);
  }

  protected closeRemoveProfilesDialog(): void {
    this.showRemoveProfilesDialog.set(false);
  }

  protected removeRememberedProfile(profileId: string): void {
    const nextProfiles = this.rememberedProfiles().filter((profile) => profile.id !== profileId);
    const removedProfile = this.rememberedProfiles().find((profile) => profile.id === profileId);

    this.rememberedProfiles.set(nextProfiles);

    if (this.selectedProfile()?.id === profileId) {
      this.closePasswordDialog();
    }

    if (removedProfile) {
      this.notifications.info('Profile removed', `${removedProfile.name} was removed from this browser.`);
    }

    if (nextProfiles.length === 0) {
      this.showRemoveProfilesDialog.set(false);
      this.showLoginForm.set(true);
    }
  }

  protected showForgotPasswordHelp(): void {
    this.notifications.info(
      'Password recovery',
      'Use your workspace recovery flow or contact your account administrator.',
    );
  }

  private clearAllErrors(): void {
    this.loginFormError.set('');
    this.loginFieldErrors.set({});
    this.passwordDialogError.set('');
  }

  private mapLoginFieldErrors(fieldErrors: Readonly<Record<string, string>>): Record<string, string> {
    const identifierError =
      fieldErrors['identifier'] ||
      fieldErrors['email'] ||
      fieldErrors['mobile'] ||
      fieldErrors['phone'];

    return {
      ...(identifierError ? { identifier: identifierError } : {}),
      ...(fieldErrors['password'] ? { password: fieldErrors['password'] } : {}),
    };
  }

  private readRememberedProfiles(): readonly RememberedProfile[] {
    if (typeof window === 'undefined') {
      return MOCK_REMEMBERED_PROFILES;
    }

    const storedProfiles = window.localStorage.getItem(REMEMBERED_PROFILES_STORAGE_KEY);
    if (!storedProfiles) {
      return MOCK_REMEMBERED_PROFILES;
    }

    try {
      const parsed = JSON.parse(storedProfiles) as unknown;
      if (!Array.isArray(parsed)) {
        return MOCK_REMEMBERED_PROFILES;
      }

      return parsed.filter(isRememberedProfile);
    } catch {
      return MOCK_REMEMBERED_PROFILES;
    }
  }
}

function createAvatarDataUrl(initials: string, startColor: string, endColor: string): string {
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="160" height="160" viewBox="0 0 160 160" fill="none">
      <defs>
        <linearGradient id="lebhasAvatarGradient" x1="24" y1="16" x2="136" y2="144" gradientUnits="userSpaceOnUse">
          <stop stop-color="${startColor}" />
          <stop offset="1" stop-color="${endColor}" />
        </linearGradient>
      </defs>
      <rect width="160" height="160" rx="80" fill="url(#lebhasAvatarGradient)" />
      <circle cx="80" cy="80" r="78" stroke="rgba(255,255,255,0.18)" stroke-width="4" />
      <text
        x="50%"
        y="54%"
        fill="white"
        font-family="Inter, Arial, sans-serif"
        font-size="52"
        font-weight="700"
        letter-spacing="0"
        text-anchor="middle"
      >
        ${initials}
      </text>
    </svg>
  `;

  return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`;
}

function isRememberedProfile(value: unknown): value is RememberedProfile {
  if (!value || typeof value !== 'object') {
    return false;
  }

  const candidate = value as Partial<RememberedProfile>;

  return (
    typeof candidate.id === 'string' &&
    typeof candidate.name === 'string' &&
    typeof candidate.avatarUrl === 'string' &&
    typeof candidate.email === 'string' &&
    typeof candidate.hasSavedPassword === 'boolean'
  );
}
