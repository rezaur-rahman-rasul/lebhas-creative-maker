export type UserRole = 'MASTER' | 'ADMIN' | 'CREW';

export type Permission =
  | 'USER_VIEW'
  | 'USER_CREATE'
  | 'USER_UPDATE'
  | 'USER_STATUS_UPDATE'
  | 'CREW_INVITE'
  | 'WORKSPACE_VIEW'
  | 'CREATIVE_GENERATE'
  | 'SESSION_MANAGE';

export type UserStatus = 'ACTIVE' | 'INVITED' | 'SUSPENDED' | 'DISABLED';

export interface WorkspaceContext {
  readonly id: string | null;
  readonly name: string | null;
}

export interface CurrentUserResponse {
  readonly id: string;
  readonly firstName: string;
  readonly lastName: string;
  readonly email: string;
  readonly phone: string | null;
  readonly role: UserRole;
  readonly status: UserStatus;
  readonly emailVerified: boolean;
  readonly lastLoginAt: string | null;
  readonly workspaceId: string | null;
  readonly createdAt: string;
  readonly updatedAt: string;
  readonly permissions: readonly Permission[];
}

export interface CurrentUser {
  readonly id: string;
  readonly firstName: string;
  readonly lastName: string;
  readonly name: string;
  readonly fullName: string;
  readonly email: string;
  readonly phone: string | null;
  readonly role: UserRole;
  readonly status: UserStatus;
  readonly emailVerified: boolean;
  readonly lastLoginAt: string | null;
  readonly createdAt: string;
  readonly updatedAt: string;
  readonly permissions: readonly Permission[];
  readonly workspaceId: string | null;
  readonly workspaceName: string | null;
  readonly workspace: WorkspaceContext;
}
