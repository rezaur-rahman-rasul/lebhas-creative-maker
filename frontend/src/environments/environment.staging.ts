import { AppEnvironment } from './environment.model';

export const environment: AppEnvironment = {
  production: true,
  appName: 'Creative SaaS',
  appVersion: '0.1.0-staging',
  apiBaseUrl: 'https://staging-api.example.com',
  authApiPrefix: '/api/v1/auth',
  workspaceHeaderName: 'X-Workspace-ID',
  correlationIdHeaderName: 'X-Correlation-ID',
};
