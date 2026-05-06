import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { environment } from '@env/environment';
import { ApiResponse } from '@app/shared/models/api-response.model';
import { joinUrl } from '@app/shared/utils/join-url';

type QueryValue = string | number | boolean | readonly (string | number | boolean)[];
type QueryParams = Record<string, QueryValue | null | undefined>;

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  get<T>(path: string, params?: QueryParams) {
    return this.http.get<ApiResponse<T>>(this.url(path), { params: this.params(params) });
  }

  post<T, TBody = unknown>(path: string, body: TBody) {
    return this.http.post<ApiResponse<T>>(this.url(path), body);
  }

  put<T, TBody = unknown>(path: string, body: TBody) {
    return this.http.put<ApiResponse<T>>(this.url(path), body);
  }

  patch<T, TBody = unknown>(path: string, body: TBody) {
    return this.http.patch<ApiResponse<T>>(this.url(path), body);
  }

  delete<T>(path: string, params?: QueryParams) {
    return this.http.delete<ApiResponse<T>>(this.url(path), { params: this.params(params) });
  }

  private url(path: string): string {
    return joinUrl(this.baseUrl, path);
  }

  private params(params?: QueryParams): HttpParams {
    let httpParams = new HttpParams();

    for (const [key, value] of Object.entries(params ?? {})) {
      if (value === null || value === undefined) {
        continue;
      }

      if (Array.isArray(value)) {
        for (const item of value) {
          httpParams = httpParams.append(key, String(item));
        }
      } else {
        httpParams = httpParams.set(key, String(value));
      }
    }

    return httpParams;
  }
}
