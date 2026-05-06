export interface AppEnvironment {
  readonly production: boolean;
  readonly appName: string;
  readonly appVersion: string;
  readonly apiBaseUrl: string;
}
