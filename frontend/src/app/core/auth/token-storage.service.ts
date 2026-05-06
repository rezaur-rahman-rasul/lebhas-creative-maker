import { Injectable } from '@angular/core';

import { CurrentUser } from '@app/shared/models/user-role.model';

const ACCESS_TOKEN_KEY = 'creative_saas.access_token';
const REFRESH_TOKEN_KEY = 'creative_saas.refresh_token';
const CURRENT_USER_KEY = 'creative_saas.current_user';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  getAccessToken(): string | null {
    return this.read(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return this.read(REFRESH_TOKEN_KEY);
  }

  getCurrentUser(): CurrentUser | null {
    const value = this.read(CURRENT_USER_KEY);
    if (!value) {
      return null;
    }

    try {
      return JSON.parse(value) as CurrentUser;
    } catch {
      this.clear();
      return null;
    }
  }

  setSession(accessToken: string, user: CurrentUser, refreshToken?: string): void {
    this.write(ACCESS_TOKEN_KEY, accessToken);
    this.write(CURRENT_USER_KEY, JSON.stringify(user));

    if (refreshToken) {
      this.write(REFRESH_TOKEN_KEY, refreshToken);
    }
  }

  clear(): void {
    if (!this.available()) {
      return;
    }

    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(CURRENT_USER_KEY);
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
