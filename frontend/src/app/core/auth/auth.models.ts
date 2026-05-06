import { CurrentUser, UserRole } from '@app/shared/models/user-role.model';

export interface AuthSession {
  readonly accessToken: string;
  readonly refreshToken?: string;
  readonly user: CurrentUser;
}

export interface LoginPayload {
  readonly email: string;
  readonly password: string;
  readonly role: UserRole;
}
