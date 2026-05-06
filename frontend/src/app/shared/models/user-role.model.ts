export type UserRole = 'MASTER' | 'ADMIN' | 'CREW';

export interface CurrentUser {
  readonly id: string;
  readonly name: string;
  readonly email: string;
  readonly role: UserRole;
  readonly workspaceId: string;
  readonly workspaceName: string;
  readonly locale: 'en' | 'bn';
}
