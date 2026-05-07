import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';

import { normalizeHttpError } from '@app/core/api/http-error';
import { AuthService } from '@app/core/auth/auth.service';
import { CurrentUserStore } from '@app/core/auth/current-user.store';
import { AuthActionFailure, AuthActionResult, AuthSession } from '@app/core/auth/auth.types';
import { NotificationStateService } from '@app/core/state/notification-state.service';
import {
  LoginRequest,
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
}
