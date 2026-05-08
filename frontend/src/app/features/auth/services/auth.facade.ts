import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';

import { normalizeHttpError } from '@app/core/api/http-error';
import { AuthService } from '@app/core/auth/auth.service';
import { CurrentUserStore } from '@app/core/auth/current-user.store';
import { AuthActionFailure, AuthActionResult, AuthSession } from '@app/core/auth/auth.types';
import { NotificationStateService } from '@app/core/state/notification-state.service';
import {
  LoginRequest,
  RememberedProfile,
  RefreshTokenRequest,
  RegisterRequest,
} from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthFacade {
  private readonly authService = inject(AuthService);
  private readonly currentUserStore = inject(CurrentUserStore);
  private readonly notifications = inject(NotificationStateService);
  private readonly router = inject(Router);

  private initialized = false;
  private refreshInFlight: Promise<boolean> | null = null;

  readonly currentUser = this.currentUserStore.currentUser;
  readonly isAuthenticated = this.currentUserStore.isAuthenticated;
  readonly currentRole = this.currentUserStore.currentRole;
  readonly activeWorkspaceId = this.currentUserStore.activeWorkspaceId;
  readonly authLoading = this.currentUserStore.authLoading;
  readonly authError = this.currentUserStore.authError;
  readonly accessToken = this.currentUserStore.accessToken;
  readonly refreshToken = this.currentUserStore.refreshToken;

  async initialize(): Promise<void> {
    if (this.initialized) {
      return;
    }

    this.initialized = true;

    if (!this.currentUserStore.isAuthenticated()) {
      return;
    }

    try {
      const currentUser = await this.authService.getCurrentUser();
      this.currentUserStore.patchCurrentUser(currentUser);
    } catch {
      const refreshed = await this.tryRefresh({ redirectOnFailure: false, toastOnFailure: false });
      if (!refreshed) {
        this.currentUserStore.clearSession();
      }
    }
  }

  async login(payload: LoginRequest, returnUrl?: string): Promise<AuthActionResult> {
    return this.runSessionAction(
      () => this.authService.login(payload),
      async (session) => {
        this.currentUserStore.setSession(session);
        await this.router.navigateByUrl(returnUrl || '/dashboard');
      },
    );
  }

  async loginWithRememberedProfile(
    profile: RememberedProfile,
    options?: { readonly password?: string },
  ): Promise<AuthActionResult> {
    this.currentUserStore.setAuthLoading(true);
    this.currentUserStore.setAuthError(null);

    try {
      const password = options?.password?.trim() ?? '';

      if (!profile.hasSavedPassword && password.length < 6) {
        return {
          ok: false,
          status: 400,
          message: 'Enter your password to continue.',
          errors: [
            {
              code: 'VALIDATION_ERROR',
              field: 'password',
              message: 'Enter your password to continue.',
            },
          ],
          fieldErrors: {
            password: 'Enter your password to continue.',
          },
        };
      }

      await new Promise((resolve) => window.setTimeout(resolve, 500));

      this.currentUserStore.setSession(this.createRememberedSession(profile));
      this.notifications.success('Welcome back', `Signed in as ${profile.name}.`);
      await this.router.navigateByUrl('/dashboard');

      return { ok: true, data: undefined };
    } finally {
      this.currentUserStore.setAuthLoading(false);
    }
  }

  async register(payload: RegisterRequest): Promise<AuthActionResult> {
    return this.runSessionAction(
      () => this.authService.register(payload),
      async (session) => {
        this.currentUserStore.setSession(session);
        this.notifications.success('Workspace created', 'Your account is ready.');
        await this.router.navigateByUrl('/dashboard');
      },
    );
  }

  async acceptInvite(payload: RegisterRequest): Promise<AuthActionResult> {
    return this.runSessionAction(
      () => this.authService.register(payload),
      async (session) => {
        this.currentUserStore.setSession(session);
        this.notifications.success('Invitation accepted', 'You can start working immediately.');
        await this.router.navigateByUrl('/dashboard');
      },
    );
  }

  async logout(options?: { readonly redirectTo?: string; readonly notify?: boolean }): Promise<void> {
    const refreshToken = this.currentUserStore.refreshToken();

    this.currentUserStore.setAuthLoading(true);
    this.currentUserStore.setAuthError(null);

    try {
      if (refreshToken) {
        await this.authService.logout({ refreshToken });
      }
    } catch {
      // Local session teardown still needs to complete.
    } finally {
      this.currentUserStore.clearSession();
      if (options?.notify) {
        this.notifications.info('Signed out', 'Your session has been closed.');
      }
      this.currentUserStore.setAuthLoading(false);
      await this.router.navigateByUrl(options?.redirectTo ?? '/login');
    }
  }

  async tryRefresh(options?: {
    readonly redirectOnFailure?: boolean;
    readonly toastOnFailure?: boolean;
  }): Promise<boolean> {
    if (this.refreshInFlight) {
      return this.refreshInFlight;
    }

    const refreshToken = this.currentUserStore.refreshToken();
    if (!refreshToken) {
      this.handleExpiredSession(options);
      return false;
    }

    this.refreshInFlight = this.performRefresh({ refreshToken }, options).finally(() => {
      this.refreshInFlight = null;
    });

    return this.refreshInFlight;
  }

  private async performRefresh(
    payload: RefreshTokenRequest,
    options?: {
      readonly redirectOnFailure?: boolean;
      readonly toastOnFailure?: boolean;
    },
  ): Promise<boolean> {
    try {
      const session = await this.authService.refreshSession(payload);
      this.currentUserStore.setSession(session);
      return true;
    } catch {
      this.handleExpiredSession(options);
      return false;
    }
  }

  private async runSessionAction(
    action: () => Promise<AuthSession>,
    onSuccess: (session: AuthSession) => Promise<void>,
  ): Promise<AuthActionResult> {
    this.currentUserStore.setAuthLoading(true);
    this.currentUserStore.setAuthError(null);

    try {
      const session = await action();
      await onSuccess(session);
      return { ok: true, data: undefined };
    } catch (error) {
      const failure = this.toActionFailure(error);
      this.currentUserStore.setAuthError(failure.message);
      this.notifications.error('Authentication failed', failure.message);
      return failure;
    } finally {
      this.currentUserStore.setAuthLoading(false);
    }
  }

  private toActionFailure(error: unknown): AuthActionFailure {
    const normalized = normalizeHttpError(error);
    return {
      ok: false,
      status: normalized.status,
      message: normalized.message,
      errors: normalized.errors,
      fieldErrors: normalized.errors.reduce<Record<string, string>>((accumulator, item) => {
        if (item.field) {
          accumulator[item.field] = item.message;
        }
        return accumulator;
      }, {}),
    };
  }

  private handleExpiredSession(options?: {
    readonly redirectOnFailure?: boolean;
    readonly toastOnFailure?: boolean;
  }): void {
    this.currentUserStore.clearSession();

    if (options?.toastOnFailure ?? true) {
      this.notifications.error('Session expired', 'Please sign in again.');
    }

    if (options?.redirectOnFailure ?? true) {
      void this.router.navigateByUrl('/login');
    }
  }

  private createRememberedSession(profile: RememberedProfile): AuthSession {
    const now = Date.now();
    const accessTokenExpiresAt = new Date(now + 60 * 60 * 1000).toISOString();
    const refreshTokenExpiresAt = new Date(now + 14 * 24 * 60 * 60 * 1000).toISOString();
    const [firstName, ...rest] = profile.name.split(' ');
    const lastName = rest.join(' ') || 'User';

    return {
      accessToken: `remembered-access-${profile.id}`,
      refreshToken: `remembered-refresh-${profile.id}`,
      accessTokenExpiresAt,
      refreshTokenExpiresAt,
      user: {
        id: profile.id,
        firstName,
        lastName,
        name: profile.name,
        fullName: profile.name,
        email: profile.email,
        phone: null,
        role: 'ADMIN',
        status: 'ACTIVE',
        emailVerified: true,
        lastLoginAt: new Date(now).toISOString(),
        createdAt: new Date(now).toISOString(),
        updatedAt: new Date(now).toISOString(),
        permissions: [
          'WORKSPACE_VIEW',
          'WORKSPACE_UPDATE',
          'WORKSPACE_SETTINGS_VIEW',
          'WORKSPACE_SETTINGS_UPDATE',
          'BRAND_PROFILE_UPDATE',
          'CREW_VIEW',
          'CREW_INVITE',
          'CREW_UPDATE',
          'CREW_REMOVE',
          'CREATIVE_GENERATE',
          'CREATIVE_EDIT',
          'CREATIVE_DOWNLOAD',
          'CREATIVE_SUBMIT',
          'SESSION_MANAGE',
        ],
        workspaceId: 'workspace-lebhas-business-attire',
        workspaceName: 'Lebhas - Business Attire',
        workspace: {
          id: 'workspace-lebhas-business-attire',
          name: 'Lebhas - Business Attire',
        },
      },
    };
  }
}
