import { Injectable, inject } from '@angular/core';

import { AuthSession } from './auth.models';
import { AuthStateService } from '../state/auth-state.service';
import { UserRole } from '@app/shared/models/user-role.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly authState = inject(AuthStateService);

  startLocalSession(role: UserRole): AuthSession {
    const session: AuthSession = {
      accessToken: `local-preview-${role.toLowerCase()}-${Date.now()}`,
      user: {
        id: '00000000-0000-4000-8000-000000000001',
        name: role === 'MASTER' ? 'Platform Master' : role === 'ADMIN' ? 'Workspace Admin' : 'Creative Crew',
        email: `${role.toLowerCase()}@creative-saas.local`,
        role,
        workspaceId: '00000000-0000-4000-8000-000000000100',
        workspaceName: 'Dhaka Growth Studio',
        locale: 'en',
      },
    };

    this.authState.setSession(session);
    return session;
  }

  signOut(): void {
    this.authState.clearSession();
  }
}
