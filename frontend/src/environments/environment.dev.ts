import { AppEnvironment } from './environment.model';

export const environment: AppEnvironment = {
  production: false,
  appName: 'Creative SaaS',
  appVersion: '0.1.0-dev',
  apiBaseUrl: 'https://dev-api.example.com',
  authApiPrefix: '/api/v1/auth',
  workspaceHeaderName: 'X-Workspace-ID',
  correlationIdHeaderName: 'X-Correlation-ID',
};
