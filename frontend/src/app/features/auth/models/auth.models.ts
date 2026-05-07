import { CurrentUserResponse } from './user.models';

export interface LoginRequest {
  readonly email: string;
  readonly password: string;
  readonly workspaceId?: string | null;
}

export interface RegisterRequest {
  readonly firstName: string;
  readonly lastName: string;
  readonly email: string;
  readonly phone: string | null;
  readonly password: string;
  readonly workspaceId?: string | null;
  readonly invitationToken?: string | null;
}

export interface RefreshTokenRequest {
  readonly refreshToken: string;
}

export interface LogoutRequest {
  readonly refreshToken: string;
}

export interface AuthSessionResponse {
  readonly accessToken: string;
  readonly accessTokenExpiresAt: string;
  readonly refreshToken: string;
  readonly refreshTokenExpiresAt: string;
  readonly user: CurrentUserResponse;
}

export type LoginResponse = AuthSessionResponse;
export type RegisterResponse = AuthSessionResponse;
export type RefreshTokenResponse = AuthSessionResponse;
