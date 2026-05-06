import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';

import { AuthService } from '@app/core/auth/auth.service';
import { AuthStateService } from '@app/core/state/auth-state.service';
import { LayoutStateService } from '@app/core/state/layout-state.service';
import { LoadingStateService } from '@app/core/state/loading-state.service';
import { UserRole } from '@app/shared/models/user-role.model';
import { AvatarComponent } from '@app/shared/components/avatar/avatar.component';
import { IconComponent } from '@app/shared/components/icon/icon.component';
import { SidebarItemComponent } from '@app/shared/components/sidebar-item/sidebar-item.component';
import { SpinnerComponent } from '@app/shared/components/spinner/spinner.component';

interface NavigationItem {
  readonly label: string;
  readonly icon: string;
  readonly route: string;
  readonly roles?: readonly UserRole[];
}

@Component({
  selector: 'app-protected-layout',
  standalone: true,
  imports: [
    RouterOutlet,
    AvatarComponent,
    IconComponent,
    SidebarItemComponent,
    SpinnerComponent,
  ],
  template: `
    <div class="min-h-screen bg-canvas text-ink">
      @if (layout.sidebarOpen()) {
        <button
          type="button"
          class="fixed inset-0 z-30 bg-slate-950/40 lg:hidden"
          aria-label="Close sidebar"
          (click)="layout.closeSidebar()"
        ></button>
      }

      <aside [class]="sidebarClasses()">
        <div class="flex h-16 items-center gap-3 border-b border-border px-4">
          <div class="grid h-9 w-9 place-items-center rounded-lg bg-brand-600 text-white">
            <app-icon name="wand-sparkles" [size]="20" />
          </div>
          @if (!layout.sidebarCollapsed()) {
            <div class="min-w-0">
              <p class="truncate text-sm font-semibold text-ink">Creative SaaS</p>
              <p class="truncate text-xs text-muted">{{ auth.currentUser()?.workspaceName }}</p>
            </div>
          }
        </div>

        <nav class="flex-1 space-y-1 p-3">
          @for (item of navigation(); track item.route) {
            <app-sidebar-item
              [label]="item.label"
              [icon]="item.icon"
              [route]="item.route"
              [compact]="layout.sidebarCollapsed()"
            />
          }
        </nav>

        <div class="border-t border-border p-3">
          <button
            type="button"
            class="flex h-10 w-full items-center gap-3 rounded-md px-3 text-sm font-medium text-slate-600 transition hover:bg-slate-100 hover:text-ink"
            (click)="signOut()"
          >
            <app-icon name="log-out" [size]="18" />
            @if (!layout.sidebarCollapsed()) {
              <span>Sign out</span>
            }
          </button>
        </div>
      </aside>

      <div [class]="contentClasses()">
        <header class="sticky top-0 z-20 flex h-16 items-center gap-3 border-b border-border bg-white/90 px-4 backdrop-blur lg:px-6">
          <button
            type="button"
            class="rounded-md p-2 text-muted hover:bg-slate-100 hover:text-ink lg:hidden"
            (click)="layout.openSidebar()"
            aria-label="Open sidebar"
          >
            <app-icon name="menu" [size]="20" />
          </button>

          <button
            type="button"
            class="hidden rounded-md p-2 text-muted hover:bg-slate-100 hover:text-ink lg:inline-flex"
            (click)="layout.toggleCollapsed()"
            aria-label="Toggle sidebar"
          >
            <app-icon name="panel-left" [size]="20" />
          </button>

          <div class="relative hidden min-w-0 flex-1 md:block">
            <app-icon
              name="search"
              className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"
              [size]="17"
            />
            <input
              class="h-10 w-full max-w-xl rounded-md border border-border bg-slate-50 pl-10 pr-3 text-sm outline-none transition focus:border-brand-500 focus:bg-white focus:ring-2 focus:ring-brand-100"
              placeholder="Search workspace"
              type="search"
            />
          </div>

          @if (loading.isLoading()) {
            <app-spinner size="sm" />
          }

          <button
            type="button"
            class="rounded-md p-2 text-muted hover:bg-slate-100 hover:text-ink"
            aria-label="Notifications"
          >
            <app-icon name="bell" [size]="19" />
          </button>

          <div class="flex items-center gap-3 border-l border-border pl-3">
            <app-avatar [name]="auth.currentUser()?.name ?? 'User'" />
            <div class="hidden leading-tight sm:block">
              <p class="text-sm font-semibold text-ink">{{ auth.currentUser()?.name }}</p>
              <p class="text-xs text-muted">{{ auth.currentUser()?.role }}</p>
            </div>
          </div>
        </header>

        <main class="p-4 lg:p-6">
          @defer {
            <router-outlet />
          } @placeholder {
            <div class="grid min-h-[20rem] place-items-center">
              <app-spinner size="lg" />
            </div>
          }
        </main>
      </div>
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProtectedLayoutComponent {
  protected readonly layout = inject(LayoutStateService);
  protected readonly auth = inject(AuthStateService);
  protected readonly loading = inject(LoadingStateService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  private readonly items: readonly NavigationItem[] = [
    { label: 'Dashboard', icon: 'layout-dashboard', route: '/dashboard' },
    { label: 'Master', icon: 'shield-check', route: '/master', roles: ['MASTER'] },
    { label: 'Admin', icon: 'building-2', route: '/admin', roles: ['ADMIN', 'MASTER'] },
    { label: 'Crew', icon: 'users', route: '/crew', roles: ['CREW', 'ADMIN', 'MASTER'] },
  ];

  protected readonly navigation = computed(() =>
    this.items.filter((item) => !item.roles || this.auth.hasAnyRole(item.roles)),
  );

  protected readonly sidebarClasses = computed(() =>
    [
      'fixed inset-y-0 left-0 z-40 flex flex-col border-r border-border bg-white transition-all duration-200 lg:translate-x-0',
      this.layout.sidebarCollapsed() ? 'lg:w-[4.75rem]' : 'lg:w-64',
      this.layout.sidebarOpen() ? 'w-64 translate-x-0' : 'w-64 -translate-x-full',
    ].join(' '),
  );

  protected readonly contentClasses = computed(() =>
    [
      'min-h-screen transition-[padding] duration-200',
      this.layout.sidebarCollapsed() ? 'lg:pl-[4.75rem]' : 'lg:pl-64',
    ].join(' '),
  );

  protected signOut(): void {
    this.authService.signOut();
    void this.router.navigateByUrl('/login');
  }
}
