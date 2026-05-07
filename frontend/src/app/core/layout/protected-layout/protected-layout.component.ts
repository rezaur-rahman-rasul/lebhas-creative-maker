import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { CurrentUserStore } from '@app/core/auth/current-user.store';
import { LayoutStateService } from '@app/core/state/layout-state.service';
import { LoadingStateService } from '@app/core/state/loading-state.service';
import { UserRole } from '@app/features/auth/models/user.models';
import { BadgeComponent } from '@app/shared/components/badge/badge.component';
import { IconComponent } from '@app/shared/components/icon/icon.component';
import { SidebarItemComponent } from '@app/shared/components/sidebar-item/sidebar-item.component';
import { SpinnerComponent } from '@app/shared/components/spinner/spinner.component';
import { UserProfileDropdownComponent } from '../user-profile-dropdown/user-profile-dropdown.component';

interface NavigationItem {
  readonly label: string;
  readonly icon: string;
  readonly route: string;
}

@Component({
  selector: 'app-protected-layout',
  standalone: true,
  imports: [
    RouterOutlet,
    BadgeComponent,
    IconComponent,
    SidebarItemComponent,
    SpinnerComponent,
    UserProfileDropdownComponent,
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
          <div class="rounded-lg border border-border bg-slate-50 px-3 py-3">
            @if (!layout.sidebarCollapsed()) {
              <div class="flex items-center justify-between gap-3">
                <div>
                  <p class="text-xs font-medium uppercase tracking-[0.12em] text-muted">Role scope</p>
                  <p class="mt-1 text-sm font-semibold text-ink">{{ auth.currentRole() }}</p>
                </div>
                <app-badge [tone]="roleBadgeTone()">{{ auth.currentRole() }}</app-badge>
              </div>
              @if (auth.activeWorkspaceId()) {
                <p class="mt-3 truncate text-xs text-muted">{{ auth.activeWorkspaceId() }}</p>
              }
            } @else {
              <div class="grid place-items-center">
                <app-icon name="shield-check" [size]="18" />
              </div>
            }
          </div>
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

          <div class="border-l border-border pl-3">
            <app-user-profile-dropdown />
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
  protected readonly auth = inject(CurrentUserStore);
  protected readonly loading = inject(LoadingStateService);

  private readonly masterNavigation: readonly NavigationItem[] = [
    { label: 'Dashboard', icon: 'layout-dashboard', route: '/dashboard' },
    { label: 'Clients', icon: 'folder-kanban', route: '/master/clients' },
    { label: 'Users', icon: 'users', route: '/master/users' },
    { label: 'Credits', icon: 'credit-card', route: '/master/credits' },
    { label: 'Payments', icon: 'credit-card', route: '/master/payments' },
    { label: 'Settings', icon: 'settings', route: '/master/settings' },
  ];

  private readonly adminNavigation: readonly NavigationItem[] = [
    { label: 'Dashboard', icon: 'layout-dashboard', route: '/dashboard' },
    { label: 'Workspace', icon: 'building-2', route: '/admin' },
    { label: 'Team', icon: 'users', route: '/admin/team' },
    { label: 'Brand Profile', icon: 'badge-check', route: '/admin/brand-profile' },
    { label: 'Assets', icon: 'image', route: '/admin/assets' },
    { label: 'Creatives', icon: 'wand-sparkles', route: '/admin/creatives' },
    { label: 'Billing', icon: 'credit-card', route: '/admin/billing' },
  ];

  private readonly crewNavigation: readonly NavigationItem[] = [
    { label: 'Dashboard', icon: 'layout-dashboard', route: '/dashboard' },
    { label: 'Assigned Work', icon: 'folder-kanban', route: '/crew' },
    { label: 'Generate Creative', icon: 'sparkles', route: '/crew/generate-creative' },
    { label: 'Submissions', icon: 'cloud-upload', route: '/crew/submissions' },
  ];

  protected readonly navigation = computed(() => {
    const role = this.auth.currentRole();
    switch (role) {
      case 'MASTER':
        return this.masterNavigation;
      case 'ADMIN':
        return this.adminNavigation;
      case 'CREW':
        return this.crewNavigation;
      default:
        return this.adminNavigation;
    }
  });

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

  protected roleBadgeTone(): 'brand' | 'blue' | 'red' | 'neutral' {
    const role = this.auth.currentRole();
    return role === 'MASTER' ? 'red' : role === 'CREW' ? 'blue' : 'brand';
  }
}
