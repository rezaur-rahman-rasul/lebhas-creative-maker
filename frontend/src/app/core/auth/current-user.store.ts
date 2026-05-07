import { Injectable, computed, inject, signal } from '@angular/core';

import { CurrentUser, Permission, UserRole } from '@app/features/auth/models/user.models';
import { AuthSession, StoredAuthSession } from './auth.types';
import { TokenStorageService } from './token-storage.service';
import { hasAnyRole, hasPermission } from './permissions';

@Injectable({ providedIn: 'root' })
export class CurrentUserStore {
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly restoredSession = this.tokenStorage.getSession();

  private readonly accessTokenSignal = signal<string | null>(
    this.restoredSession?.accessToken ?? null,
  );
  private readonly refreshTokenSignal = signal<string | null>(
    this.restoredSession?.refreshToken ?? null,
  );
  private readonly accessTokenExpiresAtSignal = signal<string | null>(
    this.restoredSession?.accessTokenExpiresAt ?? null,
  );
  private readonly refreshTokenExpiresAtSignal = signal<string | null>(
    this.restoredSession?.refreshTokenExpiresAt ?? null,
  );
  private readonly currentUserSignal = signal<CurrentUser | null>(this.restoredSession?.user ?? null);
  private readonly activeWorkspaceIdSignal = signal<string | null>(
    this.restoredSession?.activeWorkspaceId ?? this.restoredSession?.user.workspace.id ?? null,
  );
  private readonly authLoadingSignal = signal(false);
  private readonly authErrorSignal = signal<string | null>(null);

  readonly accessToken = this.accessTokenSignal.asReadonly();
  readonly refreshToken = this.refreshTokenSignal.asReadonly();
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly activeWorkspaceId = this.activeWorkspaceIdSignal.asReadonly();
  readonly authLoading = this.authLoadingSignal.asReadonly();
  readonly authError = this.authErrorSignal.asReadonly();

  readonly isAuthenticated = computed(
    () => Boolean(this.accessTokenSignal() && this.refreshTokenSignal() && this.currentUserSignal()),
  );
  readonly currentRole = computed(() => this.currentUserSignal()?.role ?? null);
  readonly permissions = computed(() => this.currentUserSignal()?.permissions ?? []);
  readonly displayName = computed(() => this.currentUserSignal()?.fullName ?? 'User');

  setSession(session: AuthSession): void {
    const activeWorkspaceId = session.user.workspace.id;
    const storedSession: StoredAuthSession = {
      ...session,
      activeWorkspaceId,
    };

    this.tokenStorage.setSession(storedSession);
    this.accessTokenSignal.set(session.accessToken);
    this.refreshTokenSignal.set(session.refreshToken);
    this.accessTokenExpiresAtSignal.set(session.accessTokenExpiresAt);
    this.refreshTokenExpiresAtSignal.set(session.refreshTokenExpiresAt);
    this.currentUserSignal.set(session.user);
    this.activeWorkspaceIdSignal.set(activeWorkspaceId);
    this.authErrorSignal.set(null);
  }

  patchCurrentUser(user: CurrentUser): void {
    this.currentUserSignal.set(user);
    if (!this.activeWorkspaceIdSignal() && user.workspace.id) {
      this.activeWorkspaceIdSignal.set(user.workspace.id);
    }
    this.persist();
  }

  setActiveWorkspaceId(workspaceId: string | null): void {
    this.activeWorkspaceIdSignal.set(workspaceId);
    this.persist();
  }

  setAuthLoading(isLoading: boolean): void {
    this.authLoadingSignal.set(isLoading);
  }

  setAuthError(message: string | null): void {
    this.authErrorSignal.set(message);
  }

  clearSession(): void {
    this.tokenStorage.clear();
    this.accessTokenSignal.set(null);
    this.refreshTokenSignal.set(null);
    this.accessTokenExpiresAtSignal.set(null);
    this.refreshTokenExpiresAtSignal.set(null);
    this.currentUserSignal.set(null);
    this.activeWorkspaceIdSignal.set(null);
    this.authLoadingSignal.set(false);
    this.authErrorSignal.set(null);
  }

  hasAnyRole(roles: readonly UserRole[]): boolean {
    return hasAnyRole(this.currentRole(), roles);
  }

  hasPermission(permission: Permission): boolean {
    return hasPermission(this.permissions(), permission);
  }

  private persist(): void {
    const accessToken = this.accessTokenSignal();
    const refreshToken = this.refreshTokenSignal();
    const accessTokenExpiresAt = this.accessTokenExpiresAtSignal();
    const refreshTokenExpiresAt = this.refreshTokenExpiresAtSignal();
    const user = this.currentUserSignal();

    if (!accessToken || !refreshToken || !accessTokenExpiresAt || !refreshTokenExpiresAt || !user) {
      this.tokenStorage.clear();
      return;
    }

    this.tokenStorage.setSession({
      accessToken,
      refreshToken,
      accessTokenExpiresAt,
      refreshTokenExpiresAt,
      user,
      activeWorkspaceId: this.activeWorkspaceIdSignal(),
    });
  }
}
