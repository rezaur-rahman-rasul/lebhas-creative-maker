import { Injectable, computed, inject, signal } from '@angular/core';

import { AuthSession } from '../auth/auth.models';
import { TokenStorageService } from '../auth/token-storage.service';
import { CurrentUser, UserRole } from '@app/shared/models/user-role.model';

@Injectable({ providedIn: 'root' })
export class AuthStateService {
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly accessTokenSignal = signal<string | null>(this.tokenStorage.getAccessToken());
  private readonly currentUserSignal = signal<CurrentUser | null>(this.tokenStorage.getCurrentUser());

  readonly accessToken = this.accessTokenSignal.asReadonly();
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => Boolean(this.accessToken() && this.currentUser()));
  readonly currentRole = computed(() => this.currentUser()?.role ?? null);
  readonly workspaceId = computed(() => this.currentUser()?.workspaceId ?? null);

  hasAnyRole(roles: readonly UserRole[]): boolean {
    const role = this.currentRole();
    return Boolean(role && roles.includes(role));
  }

  setSession(session: AuthSession): void {
    this.tokenStorage.setSession(session.accessToken, session.user, session.refreshToken);
    this.accessTokenSignal.set(session.accessToken);
    this.currentUserSignal.set(session.user);
  }

  clearSession(): void {
    this.tokenStorage.clear();
    this.accessTokenSignal.set(null);
    this.currentUserSignal.set(null);
  }
}
