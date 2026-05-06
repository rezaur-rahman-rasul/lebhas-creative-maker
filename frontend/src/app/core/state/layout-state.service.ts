import { Injectable, computed, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LayoutStateService {
  private readonly sidebarOpenSignal = signal(false);
  private readonly sidebarCollapsedSignal = signal(false);
  private readonly themeSignal = signal<'light' | 'dark'>('light');

  readonly sidebarOpen = this.sidebarOpenSignal.asReadonly();
  readonly sidebarCollapsed = this.sidebarCollapsedSignal.asReadonly();
  readonly theme = this.themeSignal.asReadonly();
  readonly sidebarMode = computed(() => (this.sidebarCollapsed() ? 'compact' : 'expanded'));

  openSidebar(): void {
    this.sidebarOpenSignal.set(true);
  }

  closeSidebar(): void {
    this.sidebarOpenSignal.set(false);
  }

  toggleSidebar(): void {
    this.sidebarOpenSignal.update((open) => !open);
  }

  toggleCollapsed(): void {
    this.sidebarCollapsedSignal.update((collapsed) => !collapsed);
  }

  toggleTheme(): void {
    this.themeSignal.update((theme) => (theme === 'light' ? 'dark' : 'light'));
  }
}
