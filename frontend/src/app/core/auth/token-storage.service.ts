import { Injectable } from '@angular/core';

import { PersistedAuthSession } from './auth.types';

const ACCESS_TOKEN_KEY = 'creative_saas.access_token';
const REFRESH_TOKEN_KEY = 'creative_saas.refresh_token';
const ACCESS_TOKEN_EXPIRES_AT_KEY = 'creative_saas.access_token_expires_at';
const REFRESH_TOKEN_EXPIRES_AT_KEY = 'creative_saas.refresh_token_expires_at';
const ACTIVE_WORKSPACE_ID_KEY = 'creative_saas.active_workspace_id';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  getAccessToken(): string | null {
    return this.read(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return this.read(REFRESH_TOKEN_KEY);
  }

  getSession(): PersistedAuthSession | null {
    const accessToken = this.read(ACCESS_TOKEN_KEY);
    const refreshToken = this.read(REFRESH_TOKEN_KEY);
    const accessTokenExpiresAt = this.read(ACCESS_TOKEN_EXPIRES_AT_KEY);
    const refreshTokenExpiresAt = this.read(REFRESH_TOKEN_EXPIRES_AT_KEY);

    if (!accessToken || !refreshToken || !accessTokenExpiresAt || !refreshTokenExpiresAt) {
      return null;
    }

    return {
      accessToken,
      refreshToken,
      accessTokenExpiresAt,
      refreshTokenExpiresAt,
      activeWorkspaceId: this.read(ACTIVE_WORKSPACE_ID_KEY),
    };
  }

  setSession(session: PersistedAuthSession): void {
    this.write(ACCESS_TOKEN_KEY, session.accessToken);
    this.write(REFRESH_TOKEN_KEY, session.refreshToken);
    this.write(ACCESS_TOKEN_EXPIRES_AT_KEY, session.accessTokenExpiresAt);
    this.write(REFRESH_TOKEN_EXPIRES_AT_KEY, session.refreshTokenExpiresAt);

    if (session.activeWorkspaceId) {
      this.write(ACTIVE_WORKSPACE_ID_KEY, session.activeWorkspaceId);
    } else if (this.available()) {
      localStorage.removeItem(ACTIVE_WORKSPACE_ID_KEY);
    }
  }

  clear(): void {
    if (!this.available()) {
      return;
    }

    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(ACCESS_TOKEN_EXPIRES_AT_KEY);
    localStorage.removeItem(REFRESH_TOKEN_EXPIRES_AT_KEY);
    localStorage.removeItem(ACTIVE_WORKSPACE_ID_KEY);
  }

  private read(key: string): string | null {
    return this.available() ? localStorage.getItem(key) : null;
  }

  private write(key: string, value: string): void {
    if (this.available()) {
      localStorage.setItem(key, value);
    }
  }

  private available(): boolean {
    return typeof localStorage !== 'undefined';
  }
}
