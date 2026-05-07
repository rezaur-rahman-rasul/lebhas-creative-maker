import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  HostListener,
  computed,
  inject,
  signal,
} from '@angular/core';

import { CurrentUserStore } from '@app/core/auth/current-user.store';
import { roleBadgeTone } from '@app/core/auth/permissions';
import { BadgeComponent } from '@app/shared/components/badge/badge.component';
import { AvatarComponent } from '@app/shared/components/avatar/avatar.component';
import { IconComponent } from '@app/shared/components/icon/icon.component';
import { AuthFacade } from '@app/features/auth/services/auth.facade';

@Component({
  selector: 'app-user-profile-dropdown',
  standalone: true,
  imports: [AvatarComponent, BadgeComponent, IconComponent],
  template: `
    <div class="relative">
      <button
        type="button"
        class="flex items-center gap-3 rounded-md border border-border bg-white px-2 py-1.5 transition hover:bg-slate-50"
        (click)="open.update((value) => !value)"
        aria-haspopup="menu"
        [attr.aria-expanded]="open()"
      >
        <app-avatar [name]="userStore.displayName()" />
        <div class="hidden min-w-0 text-left sm:block">
          <p class="truncate text-sm font-semibold text-ink">{{ userStore.displayName() }}</p>
          <p class="truncate text-xs text-muted">{{ userStore.currentUser()?.email }}</p>
        </div>
        <app-icon
          name="chevron-down"
          [size]="16"
          className="hidden text-slate-400 sm:block"
        />
      </button>

      @if (open()) {
        <div
          class="absolute right-0 top-[calc(100%+0.75rem)] z-40 w-72 rounded-lg border border-border bg-white p-4 shadow-soft"
          role="menu"
        >
          <div class="flex items-start gap-3">
            <app-avatar [name]="userStore.displayName()" />
            <div class="min-w-0 flex-1">
              <p class="truncate text-sm font-semibold text-ink">{{ userStore.displayName() }}</p>
              <p class="truncate text-sm text-muted">{{ userStore.currentUser()?.email }}</p>
            </div>
            <app-badge [tone]="badgeTone()">{{ userStore.currentRole() }}</app-badge>
          </div>

          <div class="mt-4 rounded-md border border-border bg-slate-50 px-3 py-3">
            <p class="text-xs font-medium uppercase tracking-[0.12em] text-muted">Workspace</p>
            <p class="mt-1 text-sm font-semibold text-ink">{{ workspaceName() }}</p>
            @if (userStore.activeWorkspaceId()) {
              <p class="mt-1 truncate text-xs text-muted">{{ userStore.activeWorkspaceId() }}</p>
            }
          </div>

          <button
            type="button"
            class="mt-4 flex h-10 w-full items-center justify-between rounded-md px-3 text-sm font-medium text-slate-600 transition hover:bg-slate-100 hover:text-ink"
            (click)="logout()"
          >
            <span>Logout</span>
            <app-icon name="log-out" [size]="17" />
          </button>
        </div>
      }
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserProfileDropdownComponent {
  protected readonly userStore = inject(CurrentUserStore);
  private readonly authFacade = inject(AuthFacade);
  private readonly elementRef = inject<ElementRef<HTMLElement>>(ElementRef);

  protected readonly open = signal(false);
  protected readonly badgeTone = computed(() => roleBadgeTone(this.userStore.currentRole()));
  protected readonly workspaceName = computed(
    () => this.userStore.currentUser()?.workspaceName ?? 'Workspace name pending',
  );

  @HostListener('document:click', ['$event'])
  protected onDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target as Node)) {
      this.open.set(false);
    }
  }

  protected async logout(): Promise<void> {
    this.open.set(false);
    await this.authFacade.logout({ notify: true });
  }
}
