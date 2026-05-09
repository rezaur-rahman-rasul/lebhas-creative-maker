import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { CurrentUserStore } from '@app/core/auth/current-user.store';
import { LayoutStateService } from '@app/core/state/layout-state.service';
import { LoadingStateService } from '@app/core/state/loading-state.service';
import { BadgeComponent } from '@app/shared/components/badge/badge';
import { IconComponent } from '@app/shared/components/icon/icon';
import { SidebarItemComponent } from '@app/shared/components/sidebar-item/sidebar-item';
import { SpinnerComponent } from '@app/shared/components/spinner/spinner';
import { UserProfileDropdownComponent } from '../user-profile-dropdown/user-profile-dropdown';

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
  templateUrl: './protected-layout.html',
  styleUrl: './protected-layout.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProtectedLayoutComponent {
  protected readonly layout = inject(LayoutStateService);
  protected readonly auth = inject(CurrentUserStore);
  protected readonly loading = inject(LoadingStateService);

  private readonly masterNavigation: readonly NavigationItem[] = [
    { label: 'Dashboard', icon: 'layout-dashboard', route: '/dashboard' },
    { label: 'Workspaces', icon: 'building-2', route: '/master' },
    { label: 'Workspace', icon: 'building-2', route: '/admin' },
    { label: 'Prompts', icon: 'wand-sparkles', route: '/admin/prompts' },
    { label: 'Assets', icon: 'image', route: '/admin/assets' },
    { label: 'Settings', icon: 'settings', route: '/admin/settings' },
    { label: 'Brand Profile', icon: 'badge-check', route: '/admin/brand-profile' },
    { label: 'Crew', icon: 'users', route: '/admin/crew' },
  ];

  private readonly adminNavigation: readonly NavigationItem[] = [
    { label: 'Dashboard', icon: 'layout-dashboard', route: '/dashboard' },
    { label: 'Workspace', icon: 'building-2', route: '/admin' },
    { label: 'Prompts', icon: 'wand-sparkles', route: '/admin/prompts' },
    { label: 'Assets', icon: 'image', route: '/admin/assets' },
    { label: 'Settings', icon: 'settings', route: '/admin/settings' },
    { label: 'Brand Profile', icon: 'badge-check', route: '/admin/brand-profile' },
    { label: 'Crew', icon: 'users', route: '/admin/crew' },
  ];

  private readonly crewNavigation: readonly NavigationItem[] = [
    { label: 'Dashboard', icon: 'layout-dashboard', route: '/dashboard' },
    { label: 'Workspace', icon: 'building-2', route: '/crew' },
    { label: 'Prompts', icon: 'wand-sparkles', route: '/crew/prompts' },
    { label: 'Assets', icon: 'image', route: '/crew/assets' },
  ];

  protected readonly navigation = computed(() => {
    const role = this.auth.currentRole();
    const permissions = this.auth.permissions();
    switch (role) {
      case 'MASTER':
        return this.masterNavigation;
      case 'ADMIN':
        return this.adminNavigation.filter((item) => {
          if (item.route === '/admin/assets') {
            return permissions.includes('ASSET_VIEW');
          }

          if (item.route === '/admin/prompts') {
            return hasPromptAccess(permissions);
          }

          return true;
        });
      case 'CREW':
        return this.crewNavigation.filter((item) => {
          if (item.route === '/crew/assets') {
            return permissions.includes('ASSET_VIEW');
          }

          if (item.route === '/crew/prompts') {
            return permissions.includes('PROMPT_INTELLIGENCE_USE');
          }

          return true;
        });
      default:
        return this.adminNavigation;
    }
  });
  protected readonly workspaceLabel = computed(
    () =>
      this.auth.currentUser()?.workspaceName ??
      this.auth.activeWorkspaceId() ??
      'Workspace not selected',
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

  protected roleBadgeTone(): 'brand' | 'blue' | 'red' | 'neutral' {
    const role = this.auth.currentRole();
    return role === 'MASTER' ? 'red' : role === 'CREW' ? 'blue' : 'brand';
  }
}

function hasPromptAccess(permissions: readonly string[]): boolean {
  return permissions.includes('PROMPT_INTELLIGENCE_USE')
    || permissions.includes('PROMPT_TEMPLATE_VIEW')
    || permissions.includes('PROMPT_TEMPLATE_MANAGE')
    || permissions.includes('PROMPT_HISTORY_VIEW');
}
